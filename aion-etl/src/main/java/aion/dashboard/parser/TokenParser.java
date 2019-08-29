package aion.dashboard.parser;

import aion.dashboard.blockchain.ATSTokenImpl;
import aion.dashboard.blockchain.AionService;
import aion.dashboard.blockchain.interfaces.ATSToken;
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
import org.aion.api.type.BlockDetails;
import org.aion.base.type.AionAddress;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static aion.dashboard.util.Utils.granularityToTknDec;
import static aion.dashboard.util.Utils.truncate;

public class TokenParser extends IdleProducer<TokenBatch, ContractEvent> {

    private final AionService apiService;
    private final ContractService contractService;
    private final TokenService service;
    private final CacheManager<String, ATSToken> atsTokenCacheManager = CacheManager.getManager(CacheManager.Cache.ATS_TOKEN);
    private final CacheManager<String, Token> tokenCacheManager = CacheManager.getManager(CacheManager.Cache.TOKEN);
    private final CacheManager<String, Contract> contractCacheManager = CacheManager.getManager(CacheManager.Cache.CONTRACT);
    private final ExecutorService workers = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*2);

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
    protected List<TokenBatch> doTask(List<Message<ContractEvent>> records) throws SQLException, AionApiException {
        Thread.currentThread().setName("token-parser");
        GENERAL.info("Starting token parser.");
        Set<String> holdersSet = new HashSet<>();
        TokenBatch res = new TokenBatch();
        long lastBlockNumber = -1;
        for (var msg : records) {
            if (GENERAL.isTraceEnabled()) {
                GENERAL.trace("Starting request for token with address: {}", getResponse(ContractEvent.class, msg.getItem()).map(ContractEvent::getAddress).orElse(""));
            }

            res = res.merge(readTokenEvent(msg, holdersSet));

            if (lastBlockNumber < msg.getBlockDetails().getNumber()) {
                lastBlockNumber = msg.getBlockDetails().getNumber();
            }
        }

        List<TokenBatch> ret;

        if (res.getTokenHolders().isEmpty() && res.getTokens().isEmpty() && res.getTransfers().isEmpty()) {
            ret = Collections.emptyList();
            GENERAL.error("Failed to transform the event at: {}", lastBlockNumber);
        } else {
            res.setState(new ParserState.ParserStateBuilder()
                    .blockNumber(BigInteger.valueOf(lastBlockNumber))
                    .id(ParserStateServiceImpl.DB_ID).build());
            ret = Collections.singletonList(res);
        }
        return ret;
    }


    private TokenBatch readTokenEvent(Message<ContractEvent> msg, Set<String> holdersSet) throws SQLException, AionApiException {
        var contractAddr = msg.getItem().get(0).getAddress();
        var res = new TokenBatch();
        try {
            Contract contract;

            if (contractCacheManager.contains(contractAddr)) contract = contractCacheManager.getIfPresent(contractAddr);
            else {
                contract = contractService.findContract(contractAddr).orElseThrow( () -> new NullPointerException("Failed to find the contract in the database."));
                registerContract(contract);
            }

            ATSToken atsToken = getATSToken(contractAddr);
            final Token token;
            if (atsToken ==  null){// The token does not exist in the database.
                atsToken = new ATSTokenImpl(contractAddr, contract.getContractType());
                registerATSToken(contractAddr,atsToken);
                token = atsToken.getDetails(contract).orElseThrow();
                res.addToken(token);
            }
            else if (Parsers.isSupplyUpdate(msg.getItem())) {//The liquid supply on the token has changed
                token = atsToken.updateDetails(contract).orElseThrow();
                res.addToken(token);
                updateCache(contractAddr, token, contract);
            }
            else {//the token is in the cache
                token = atsToken.getDetails(contract).orElseThrow();
            }


            res.addTransfers(Parsers.getTokenTransfers(msg.getItem(), msg.getBlockDetails(), msg.getTxDetails(), token));
            res.addHolders(getHolders(msg.getItem(), atsToken, msg.getBlockDetails(), holdersSet));

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

    private List<TokenHolders> getHolders(List<ContractEvent> events, ATSToken token, BlockDetails b, Set<String> holdersSet) throws AionApiException {

        List<TokenHolders> res = new ArrayList<>();

        for (var event : events) {

            List<String> inputs = Parsers.getInputs(event, "to","from", "operator", "localSender","localRecipient", "tokenHolder");

            for (var input: inputs){
                if(holdersSet.add(input)) {// check if the holder was previously read in this batch
                    createHolder(input, b,token).ifPresent(res::add);
                }
            }
        }

        return res;
    }


    private Optional<TokenHolders> createHolder(String address, BlockDetails b, ATSToken token) throws AionApiException {
        if (!address.isBlank() && org.aion.base.util.Utils.isValidAddress(address)) {
            return token.getHolderDetails(address, b);
        }
        else {
            return Optional.empty();
        }
    }

    private ATSToken getATSToken(String contractAddr) throws SQLException {
        if (atsTokenCacheManager.contains(contractAddr)){
            return atsTokenCacheManager.getIfPresent(contractAddr);//check the cache for the token and return if it is found
        }
        else {
            var token = service.getByContractAddr(contractAddr);//otherwise check the database for the token and build the ats token
            // if it was found

            if (token == null){
                return null;
            }
            else {
                Contract contract = contractCacheManager.getIfPresent(contractAddr);
                var atsToken= new ATSTokenImpl(token,contract.getContractType());
                registerATSToken(contractAddr,atsToken);
                return atsToken;
            }
        }
    }


    private void registerATSToken(String contractAddr, ATSToken token){
        atsTokenCacheManager.putIfAbsent(contractAddr, token);
    }
    private <T> Optional<T> getResponse(Class<T> clazz, List res) {
        if (res != null && !res.isEmpty() && clazz.equals(res.get(0).getClass())) {
            return Optional.ofNullable(clazz.cast(res.get(0)));
        } else {
            return Optional.empty();
        }
    }




}
