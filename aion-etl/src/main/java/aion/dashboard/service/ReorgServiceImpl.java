package aion.dashboard.service;

import aion.dashboard.blockchain.ATSTokenImpl;
import aion.dashboard.blockchain.interfaces.ATSToken;
import aion.dashboard.blockchain.interfaces.Web3Service;
import aion.dashboard.blockchain.type.APIAccountDetails;
import aion.dashboard.blockchain.type.APIBlock;
import aion.dashboard.cache.CacheManager;
import aion.dashboard.config.Config;
import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.db.DbQuery;
import aion.dashboard.domainobject.*;
import aion.dashboard.exception.AionApiException;
import aion.dashboard.exception.Web3ApiException;
import aion.dashboard.stats.AbstractGraphingTask;
import aion.dashboard.util.TimeLogger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Used to perform a reorg of the block chain
 */
public class ReorgServiceImpl implements ReorgService {

    private static final TimeLogger TIME_LOGGER = new TimeLogger(ReorgService.class.getName());

    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");
    private final Web3Service web3Service;
    private final CacheManager<String, ATSToken> atsTokenCacheManager = CacheManager.getManager(CacheManager.Cache.ATS_TOKEN);
    private TokenHoldersService tokenHoldersService;
    private ParserStateService stateService;
    private AccountService accountService;
    private BlockService blockService;
    private long reorgLimit;
    private List<String> affectedAddresses;
    private long transactionNum;
    private ObjectWriter writer = new ObjectMapper().writer();

    public ReorgServiceImpl(ParserStateService stateService, Web3Service web3Service) {
        this.web3Service = web3Service;
        accountService = AccountServiceImpl.getInstance();
        blockService = BlockServiceImpl.getInstance();
        tokenHoldersService = TokenHoldersServiceImpl.getInstance();
        this.stateService = stateService;
        transactionNum = 0;
        affectedAddresses = new ArrayList<>();
        reorgLimit = Config.getInstance().getBlockReorgLimit();
    }

    private void cleanUp(){
        transactionNum = 0;
        if (!affectedAddresses.isEmpty()) {
            affectedAddresses.clear();
        }
    }

    @Override
    public boolean reorg() throws Exception {
        try {
            long blkChainPointer = web3Service.getBlockNumber();

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
                    storeDetails(consistentBlockPointer, (int)Math.min(requestedReorgSize, Integer.MAX_VALUE));
                    return performReorg(consistentBlockPointer);
                }
            }
        }finally {
            cleanUp();
        }
    }

    void storeDetails(long blockNumber, int depth){
        try{
            String addressString = writer.writeValueAsString(this.affectedAddresses);
            doStore(blockNumber, depth, addressString, transactionNum);
        } catch (JsonProcessingException | SQLException | RuntimeException e) {
            GENERAL.error("Failed to store reorg details in the database.");
            GENERAL.error("Details: block_number={} \ndepth={}\naddresses={}\ntransaction_num:{}", blockNumber, depth, affectedAddresses, transactionNum);
        }
    }

    static void doStore(long blockNumber, int depth, String addressString, long transactionNum) throws SQLException {
        try(Connection con = DbConnectionPool.getConnection();
            PreparedStatement ps = con.prepareStatement(DbQuery.InsertReorgDetails)) {
            try {
                ps.setLong(1, blockNumber);
                ps.setTimestamp(2, Timestamp.from(Instant.now()));
                ps.setInt(3, depth);
                ps.setString(4, addressString);
                ps.setLong(5, transactionNum);
                ps.execute();
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
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

    // TODO: 9/13/19 Replace the this recursive function with a loop
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
                if (apiBlock != null){
                    transactionNum += apiBlock.getTransactions().size();
                }
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
    public boolean performReorg(long consistentBlock) throws Exception {

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

        if(task.getFuture() != null && task.getFuture().isCancelled()){
            task.scheduleNow();
        }

        TIME_LOGGER.logTime("performReorg() completed in: {}");
        GENERAL.debug("Completed Reorg.");

        return true;
    }

    private List<TokenHolders> getTokenBalances(List<TokenHolders> tokenHolders) throws Exception {
        Set<String> tokensContractAddr = tokenHolders.stream().map(TokenHolders::getContractAddress).collect(Collectors.toSet());

        for (var contractAddr : tokensContractAddr) {
            if (!atsTokenCacheManager.contains(contractAddr)){
                Token token = TokenServiceImpl.getInstance().getByContractAddr(contractAddr);
                Contract contract = ContractServiceImpl.getInstance().findContract(contractAddr).orElseThrow();
                atsTokenCacheManager.putIfAbsent(contractAddr, new ATSTokenImpl(token, contract.getContractType()));
            }
        }

        List<TokenHolders> res = new ArrayList<>();

        for (var tknBalance: tokenHolders){
            try {
                ATSToken atsToken = atsTokenCacheManager.getIfPresent(tknBalance.getContractAddress());
                BigInteger balance = atsToken.getBalance(tknBalance.getHolderAddress());
                Token token = atsToken.details();
                res.add(new TokenHolders.TokenBalanceBuilder()
                        .setBlockNumber(web3Service.getBlockNumber())
                        .setRawBalance(balance.toString())
                        .setScaledBalance(new BigDecimal(balance).scaleByPowerOfTen(-1 * token.getTokenDecimal()))
                        .setTokenGranularity(token.getGranularity())
                        .setTokenDecimal(token.getTokenDecimal())
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

    private List<Account> getAccountDetails(final List<Account> accounts) throws Web3ApiException {
        List<Account> res = new ArrayList<>();
        Account.AccountBuilder accountBuilder = new Account.AccountBuilder();
        for (var bal: accounts) {//get updated accounts for these accounts
            APIAccountDetails details = web3Service.getAccountDetails(bal.getAddress());
            affectedAddresses.add(bal.getAddress());
            accountBuilder.address(bal.getAddress())
                    .contract(bal.getContract())
                    .lastBlockNumber(details.getBlockNumber())
                    .balance(new BigDecimal(details.getBalance()))
                    .transactionHash(bal.getTransactionHash())
                    .nonce(details.getNonce().toString(16));
            res.add(accountBuilder.build());
        }

        return res;
    }
}