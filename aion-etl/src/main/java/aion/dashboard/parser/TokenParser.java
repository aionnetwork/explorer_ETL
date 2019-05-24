package aion.dashboard.parser;

import aion.dashboard.blockchain.AionService;
import aion.dashboard.cache.CacheManager;
import aion.dashboard.domainobject.Contract;
import aion.dashboard.domainobject.ParserState;
import aion.dashboard.domainobject.Token;
import aion.dashboard.domainobject.TokenHolders;
import aion.dashboard.exception.AionApiException;
import aion.dashboard.parser.type.Message;
import aion.dashboard.parser.type.TokenBatch;
import aion.dashboard.service.ContractService;
import aion.dashboard.service.ParserStateServiceImpl;
import aion.dashboard.service.TokenService;
import aion.dashboard.parser.events.SolABIDefinitions;
import aion.dashboard.parser.events.ContractEvent;
import org.aion.api.IContract;
import org.aion.api.sol.IAddress;
import org.aion.api.type.BlockDetails;
import org.aion.base.type.AionAddress;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import static aion.dashboard.util.Utils.granularityToTknDec;
import static aion.dashboard.util.Utils.truncate;

public class TokenParser extends IdleProducer<TokenBatch, ContractEvent> {

    private final AionService apiService;
    private final ContractService contractService;
    private final TokenService service;
    private final CacheManager<String, Token> tokenCacheManager = CacheManager.getManager(CacheManager.Cache.TOKEN);
    private final CacheManager<String, Contract> contractCacheManager = CacheManager.getManager(CacheManager.Cache.CONTRACT);

    void registerContract(Contract contract){
        contractCacheManager.putIfAbsent(contract.getContractAddr(), contract);
    }

    public TokenParser(BlockingQueue<List<TokenBatch>> queue, BlockingQueue<List<Message<ContractEvent>>> workQueue, AionService apiService, ContractService contractService, TokenService service) {
        super(queue, workQueue);
        this.apiService = apiService;
        this.contractService = contractService;
        this.service = service;
    }

    @Override
    protected List<TokenBatch> task() throws Exception {
        Thread.currentThread().setName("token-parser");
        var records = getMessage();
        Set<String> holdersSet = new HashSet<>();
        TokenBatch res = new TokenBatch();
        if (!records.isEmpty()) {
            long lastBlockNumber =-1;
            for (var msg : records) {
                if (GENERAL.isTraceEnabled()) {
                    GENERAL.trace("Starting request for account: {}", getResponse(ContractEvent.class, msg.getItem()).map(ContractEvent::getAddress).orElse(""));
                }

                if (msg.getItem().stream().anyMatch(ContractEvent::isAvm)){
                    //leave empty for now
                }
                else {
                    res= res.merge(readTokenEvent(msg, holdersSet));
                }
                if (lastBlockNumber<msg.getBlockDetails().getNumber()){
                    lastBlockNumber=msg.getBlockDetails().getNumber();
                }
            }

            List<TokenBatch> ret;

            if (res.getTokenHolders().isEmpty() && res.getTokens().isEmpty() && res.getTransfers().isEmpty()){
                ret = Collections.emptyList();
                GENERAL.error("Failed to transform the event at: {}", lastBlockNumber);
            }else {
                res.setState(new ParserState.ParserStateBuilder()
                        .blockNumber(BigInteger.valueOf(lastBlockNumber))
                        .id(ParserStateServiceImpl.DB_ID).build());
                ret=Collections.singletonList(res);
            }
            consumeMessage();
            return ret;
        } else {
            return Collections.emptyList();
        }


    }

    private TokenBatch readTokenEvent(Message<ContractEvent> msg, Set<String> holdersSet) throws SQLException, AionApiException {
        var contractAddr = msg.getItem().get(0).getAddress();
        var res = new TokenBatch();
        try {


            Token token = tokenCacheManager.contains(contractAddr) ?
                    tokenCacheManager.getIfPresent(contractAddr) :
                    service.getByContractAddr(contractAddr);
            Contract contract;
            if (contractCacheManager.contains(contractAddr)) contract = contractCacheManager.getIfPresent(contractAddr);
            else {
                contract = Objects.requireNonNull(contractService.selectContractsByContractAddr(contractAddr));
                registerContract(contract);
            }


            if (token == null || Parsers.isSupplyUpdate(msg.getItem())) {
                // get token
                token = createToken(contract);
                res.addToken(token);
                updateCache(contractAddr, token, contract);
            }


            res.addTransfers(Parsers.getTokenTransfers(msg.getItem(), msg.getBlockDetails(), msg.getTxDetails(), token));
            res.addHolders(getHolders(msg.getItem(), contract, token, msg.getBlockDetails(), holdersSet));

        } catch (NullPointerException e){
            GENERAL.info("Failed to load contract from the database;");
            throw e;
        }
        catch (NoSuchElementException | IllegalStateException e) {
            GENERAL.info("Falsely identified token: {}", contractAddr);
        }

        return res;
    }


    private void updateCache(String contractAddr, Token token, Contract contract) {
        tokenCacheManager.putIfAbsent(contractAddr, token);
        contractCacheManager.putIfAbsent(contractAddr, contract);
    }

    private List<TokenHolders> getHolders(List<ContractEvent> events, Contract contract, Token token, BlockDetails b, Set<String> holdersSet) throws AionApiException {

        final SolABIDefinitions solAbiDefinitions = SolABIDefinitions.getInstance();
        IContract blockChainInterface = apiService.getContract(AionAddress.wrap(contract.getContractCreatorAddr()), AionAddress.wrap(contract.getContractAddr()), solAbiDefinitions.getJSONString(SolABIDefinitions.ATS_CONTRACT));
        List<TokenHolders> res = new ArrayList<>();

        for (var event : events) {

            List<String> inputs = Parsers.getInputs(event, "to","from", "operator", "localSender","localRecipient", "tokenHolder");

            for (var input: inputs){
                if(holdersSet.add(input)) {// check if the holder was previously read in this batch
                    createHolder(input, b,token,blockChainInterface).ifPresent(res::add);
                }
            }
        }

        return res;
    }


    private Optional<TokenHolders> createHolder(String address, BlockDetails b, Token token, IContract blockChainInterface) throws AionApiException {
        if (!address.isBlank() && org.aion.base.util.Utils.isValidAddress(address)) {

            var balance = getResponse(BigInteger.class, apiService.callContractFunction(blockChainInterface, "balanceOf", IAddress.copyFrom(address))).filter(Parsers.NUM_RESPONSE_EXISTS);
            return balance.map(bigInteger -> (TokenHolders.from(address, b, token, bigInteger)));
        }
        else {
            return Optional.empty();
        }

    }

    private Token createToken(Contract contract) throws AionApiException {
        final SolABIDefinitions solAbiDefinitions = SolABIDefinitions.getInstance();
        IContract blockChainInterface = apiService.getContract(AionAddress.wrap(contract.getContractCreatorAddr()), AionAddress.wrap(contract.getContractAddr()), solAbiDefinitions.getJSONString(SolABIDefinitions.ATS_CONTRACT));


        //get token details from the chain
        var granularity = getResponse(BigInteger.class, apiService.callContractFunction(blockChainInterface, "granularity"))
                .filter(Parsers.NUM_RESPONSE_EXISTS)// if the api returns 0 the token does not exist
                .orElseThrow();
        var name = truncate(getResponse(String.class, apiService.callContractFunction(blockChainInterface, "name"))
                .filter(Parsers.STRING_RESPONSE_EXISTS)//if the api returns an empty string the token does not exist
                .orElseThrow());
        var symbol = truncate(getResponse(String.class, apiService.callContractFunction(blockChainInterface, "symbol"))
                .filter(Parsers.STRING_RESPONSE_EXISTS)
                .orElseThrow());
        var totalSupply = getResponse(BigInteger.class, apiService.callContractFunction(blockChainInterface, "totalSupply"))
                .filter(Parsers.NUM_RESPONSE_EXISTS)
                .orElseThrow();
        var liquidSupply = getResponse(BigInteger.class, apiService.callContractFunction(blockChainInterface, "liquidSupply"))
                .orElseThrow();

        //build the token
        return Token.getBuilder().contractAddress(contract.getContractAddr())
                .creatorAddress(contract.getContractCreatorAddr())
                .transactionHash(contract.getContractTxHash())
                .name(name)
                .symbol(symbol)
                .granularity(granularity)
                .totalSupply(totalSupply)
                .totalLiquidSupply(liquidSupply)
                .timestamp(contract.getTimestamp())
                .setTokenDecimal(granularityToTknDec(granularity))
                .build();
    }


    private <T> Optional<T> getResponse(Class<T> clazz, List res) {
        if (res != null && !res.isEmpty() && clazz.equals(res.get(0).getClass())) {
            return Optional.ofNullable(clazz.cast(res.get(0)));
        } else {
            return Optional.empty();
        }
    }




}
