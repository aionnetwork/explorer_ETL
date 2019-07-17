package aion.dashboard.service;

import aion.dashboard.blockchain.AionService;
import aion.dashboard.blockchain.interfaces.Web3Service;
import aion.dashboard.blockchain.type.APIBlock;
import aion.dashboard.config.Config;
import aion.dashboard.domainobject.Account;
import aion.dashboard.domainobject.Block;
import aion.dashboard.domainobject.Token;
import aion.dashboard.domainobject.TokenHolders;
import aion.dashboard.exception.AionApiException;
import aion.dashboard.exception.ReorganizationLimitExceededException;
import aion.dashboard.task.AbstractGraphingTask;
import aion.dashboard.parser.events.SolABIDefinitions;
import aion.dashboard.util.TimeLogger;
import org.aion.api.IContract;
import org.aion.api.sol.IAddress;
import org.aion.base.type.AionAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Used to perform a reorg of the block chain
 */
public class ReorgServiceImpl implements ReorgService {

    private static final TimeLogger TIME_LOGGER = new TimeLogger(ReorgService.class.getName());

    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");
    private final Web3Service web3Service;

    private TokenHoldersService tokenHoldersService;
    private ParserStateService stateService;
    private AionService borrowedAionService;
    private AccountService accountService;
    private BlockService blockService;
    private long reorgLimit;

    public ReorgServiceImpl(AionService aionService, ParserStateService stateService, Web3Service web3Service) {
        this.web3Service = web3Service;
        accountService = AccountServiceImpl.getInstance();
        blockService = BlockServiceImpl.getInstance();
        tokenHoldersService = TokenHoldersServiceImpl.getInstance();
        this.borrowedAionService = aionService;
        this.stateService = stateService;

        reorgLimit = Config.getInstance().getBlockReorgLimit();
    }

    @Override
    public boolean reorg() throws Exception {
        if (!borrowedAionService.isConnected()) borrowedAionService.reconnect();
        long blkChainPointer = borrowedAionService.getBlockNumber();
        long dbPointer = stateService.readDBState().getBlockNumber().longValue();
        long consistentBlockPointer;
        if (dbPointer == -1) {
            return false;
        } else {
            if (blkChainPointer >= dbPointer) consistentBlockPointer = checkDepth(dbPointer, reorgLimit);
            else consistentBlockPointer = checkDepth(blkChainPointer, reorgLimit);

            long requestedReorgSize = dbPointer - consistentBlockPointer;

            if (requestedReorgSize == 0) {
                GENERAL.trace("Reorg OK. ETL consistent at {}", consistentBlockPointer);
                return false;
            } else if (requestedReorgSize < 0) {
                GENERAL.debug("Reorg: Negative re-org range requested: {}", requestedReorgSize);
                throw new IllegalStateException();
            } else {
                return performReorg(consistentBlockPointer);
            }
        }
    }




    private long checkDepth(long blockNumber, long depth) throws Exception {
        var res = doCheck(blockNumber, depth);
        if (res>0){
            return res;
        }
        else {
            return blockNumber;
        }
    }


    private long doCheck(final long blockNumber, final long depth) throws Exception {
        final long blockToCheck = blockNumber - depth;
        if (depth <= 0 || blockToCheck < 0){
            return -1;
        }
        else {
            APIBlock apiBlock = web3Service.getBlock(blockToCheck);
            Block dbBlock = blockService.getByBlockNumber(blockToCheck);

            var comparison = dbBlock !=null && apiBlock !=null && apiBlock.compareHash(dbBlock);

            if (!comparison){
                var dbHash = dbBlock == null ? "null" : dbBlock.getBlockHash();
                var apiHash = apiBlock == null ? "null" : apiBlock.getHash();
                GENERAL.error("Chain inconsistent at depth {}. DBBlock=[{}] != APIBlock=[{}]", blockToCheck, dbHash, apiHash );
                return blockNumber - depth;
            }
            else {
                GENERAL.trace("Chain consistent at depth {}. DBBlock=[{}] == APIBlock=[{}]", blockToCheck, dbBlock.getBlockHash(), apiBlock.getHash() );

                return doCheck(blockNumber, depth-1);
            }
        }
    }


    /**
     * Performs the reorg using the block service's delete method. Also saves all balances affected by the reorg.
     * @param consistentBlock
     * @return
     * @throws SQLException
     * @throws AionApiException
     */
    public boolean performReorg(long consistentBlock) throws SQLException, AionApiException {

        GENERAL.debug("Starting Reorg...");

        AbstractGraphingTask task = AbstractGraphingTask.getInstance(Config.getInstance().getTaskType());


        if (!Objects.isNull(task.getFuture()))
            task.getFuture().cancel(true);


        TIME_LOGGER.start();
        //Update the accounts and token accounts that must remain in the DB
        List<Account> accounts = accountService.getByBlockNumber(consistentBlock);
        List<TokenHolders> tokenHolders = tokenHoldersService.getTokensByBlockNumber(consistentBlock);

        accounts = getAccountDetails(accounts);
        tokenHolders = getTokenBalances(tokenHolders);

        try {
            blockService.deleteFromAndUpdate(consistentBlock, tokenHolders, accounts);
        }
        catch (SQLException exception){
            GENERAL.debug("Threw an exception while performing reorg: ", exception);
            throw exception;
        }

        if(task.getFuture().isCancelled()){
            task.scheduleNow();
        }

        TIME_LOGGER.logTime("performReorg() completed in: {}");
        GENERAL.debug("Completed Reorg.");

        return true;
    }

    private List<TokenHolders> getTokenBalances(List<TokenHolders> tokenHolders) throws SQLException, AionApiException {
        Set<String> tokensContractAddr = tokenHolders.stream().map(TokenHolders::getContractAddress).collect(Collectors.toSet());
        Map<String, IContract> contracts = new HashMap<>();
        Map<String, Token> tokenMap = new HashMap<>();


        for (var contractAddr : tokensContractAddr) {
            Token token = TokenServiceImpl.getInstance().getByContractAddr(contractAddr);
            tokenMap.put(contractAddr, token);
            contracts.put(contractAddr, borrowedAionService.getContract(AionAddress.wrap(token.getCreatorAddress()),
                    AionAddress.wrap(token.getContractAddress()),
                    SolABIDefinitions.getInstance().getJSONString(SolABIDefinitions.ATS_CONTRACT)));
        }

        List<TokenHolders> res = new ArrayList<>();

        for (var tknBalance: tokenHolders){
            try {
                borrowedAionService.reconnect();
                BigDecimal balance = new BigDecimal( (BigInteger) borrowedAionService
                        .callContractFunction(contracts.get(tknBalance.getContractAddress()), "balanceOf", IAddress.copyFrom(tknBalance.getHolderAddress())).get(0))
                        .divide(new BigDecimal(tokenMap.get(tknBalance.getContractAddress()).getGranularity()), MathContext.DECIMAL128);

                res.add(new TokenHolders.TokenBalanceBuilder()
                        .setBlockNumber(borrowedAionService.getBlockNumber())
                        .setRawBalance(balance.toPlainString())
                        .setScaledBalance(balance.scaleByPowerOfTen(-1 * tokenMap.get(tknBalance.getContractAddress()).getTokenDecimal()))
                        .setTokenGranularity(tokenMap.get(tknBalance.getContractAddress()).getGranularity())
                        .setTokenDecimal(tokenMap.get(tknBalance.getContractAddress()).getTokenDecimal())
                        .setHolderAddress(tknBalance.getHolderAddress())
                        .setContractAddress(tknBalance.getContractAddress())
                        .build());
            } catch (AionApiException e) {
                GENERAL.debug("Aion service threw an exception", e);
                throw e;
            }
        }

        return res.stream()
                .filter(tknBalance -> tknBalance.getScaledBalance().compareTo(BigDecimal.ZERO) != 0)//Remove all holders that have an empty balance
                .collect(Collectors.toList());

    }

    private List<Account> getAccountDetails(final List<Account> accounts) throws AionApiException {
        List<Account> res = new ArrayList<>();


        Account.AccountBuilder accountBuilder = new Account.AccountBuilder();
        for (var bal: accounts) {//get updated accounts for these accounts
            accountBuilder.address(bal.getAddress())
                    .contract(bal.getContract())
                    .lastBlockNumber(borrowedAionService.getBlockNumber())
                    .balance(new BigDecimal(borrowedAionService.getBalance(bal.getAddress())))
                    .transactionHash(bal.getTransactionHash())
                    .nonce(borrowedAionService.getNonce(bal.getAddress()).toString(16));
            res.add(accountBuilder.build());
        }

        return res;
    }
}