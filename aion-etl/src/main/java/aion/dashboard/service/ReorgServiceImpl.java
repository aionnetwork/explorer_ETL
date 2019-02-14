package aion.dashboard.service;

import aion.dashboard.blockchain.AionService;
import aion.dashboard.config.Config;
import aion.dashboard.domainobject.Account;
import aion.dashboard.domainobject.Block;
import aion.dashboard.domainobject.Token;
import aion.dashboard.domainobject.TokenHolders;
import aion.dashboard.exception.AionApiException;
import aion.dashboard.exception.ReorganizationLimitExceededException;
import aion.dashboard.task.GraphingTask;
import aion.dashboard.util.ABIDefinitions;
import aion.dashboard.util.TimeLogger;
import org.aion.api.IContract;
import org.aion.api.sol.IAddress;
import org.aion.base.type.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
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

    private TokenHoldersService tokenHoldersService;
    private ParserStateService stateService;
    private AionService borrowedAionService;
    private AccountService accountService;
    private BlockService blockService;
    private long reorgLimit;

    public ReorgServiceImpl(AionService aionService, ParserStateService stateService) {
        accountService = AccountServiceImpl.getInstance();
        blockService = BlockServiceImpl.getInstance();
        tokenHoldersService = TokenHoldersServiceImpl.getInstance();
        this.borrowedAionService = aionService;
        this.stateService = stateService;

        reorgLimit = Config.getInstance().getBlockReorgLimit();
    }

    @Override
    public boolean reorg() throws AionApiException, ReorganizationLimitExceededException, SQLException {
        if (!borrowedAionService.isConnected()) borrowedAionService.reconnect();
        long blkChainPointer = borrowedAionService.getBlockNumber();
        long dbPointer = stateService.readDBState().getBlockNumber().longValue();
        long consistentBlockPointer;
        if (dbPointer == -1) return false;

        if (blkChainPointer >= dbPointer) consistentBlockPointer = findConsistentBlock(dbPointer);
        else consistentBlockPointer = findConsistentBlock(blkChainPointer);

        long requestedReorgSize = dbPointer - consistentBlockPointer;

        if (requestedReorgSize == 0){
            GENERAL.trace("Reorg OK. ETL consistent at {}", consistentBlockPointer);
            return false;
        } else if (requestedReorgSize < 0) {
            GENERAL.debug("Reorg: Negative re-org range requested: {}", requestedReorgSize);
            throw new IllegalStateException();
        } else if (requestedReorgSize > reorgLimit) {
            GENERAL.debug("Reorg: Requested reorg greater than limit: {}", requestedReorgSize);
            throw new ReorganizationLimitExceededException();
        }

        return performReorg(consistentBlockPointer);
    }

    /**
     * Find the last block at which the database matches the kernel
     * @param startingBlock The block at which the search should start
     * @return the mactching block number
     * @throws AionApiException
     * @throws ReorganizationLimitExceededException
     * @throws SQLException
     */
    private long findConsistentBlock(long startingBlock) throws AionApiException, ReorganizationLimitExceededException, SQLException {
        long consistentBlock = startingBlock;


        while (true) {
            Block dbBlock = blockService.getByBlockNumber(consistentBlock);
            String dbHash = dbBlock == null? null : dbBlock.getBlockHash();
            String chainHash = borrowedAionService.getBlockHashbyNumber(consistentBlock);


            if (dbHash != null && dbHash.equals(chainHash)) {
                GENERAL.trace("Chain consistent at height {}" + "BC[{}] == DB[{}]", consistentBlock, chainHash, dbHash);
                break;
            } else
                GENERAL.debug("Chain inconsistent at height {}" + "BC[{}] != DB[{}]", consistentBlock, chainHash, dbHash);
            if (consistentBlock <=1) {
                GENERAL.debug("Reorg: Requested reorganization past genesis. Not allowed.");
                throw new ReorganizationLimitExceededException();
            } else if (startingBlock - consistentBlock >= reorgLimit) {
                GENERAL.debug("Reorg: Requested reorganization exceeded limit.");
                throw new ReorganizationLimitExceededException();

            }

            consistentBlock --;
        }

        return consistentBlock;
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

        GraphingTask task = GraphingTask.getInstance();

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
            contracts.put(contractAddr, borrowedAionService.getContract(Address.wrap(token.getCreatorAddress()),
                    Address.wrap(token.getContractAddress()),
                    ABIDefinitions.getInstance().getJSONString(ABIDefinitions.ATS_CONTRACT)));
        }

        List<TokenHolders> res = new ArrayList<>();

        for (var tknBalance: tokenHolders){
            try {
                borrowedAionService.reconnect();
                BigDecimal balance = BigDecimal.valueOf((long) borrowedAionService
                        .callContractFunction(contracts.get(tknBalance.getContractAddress()), "balanceOf", IAddress.copyFrom(tknBalance.getHolderAddress())).get(0))
                        .divide(new BigDecimal(tokenMap.get(tknBalance.getContractAddress()).getGranularity()), MathContext.DECIMAL128);

                res.add(new TokenHolders.TokenBalanceBuilder()
                        .setBlockNumber(borrowedAionService.getBlockNumber())
                        .setScaledBalance(balance)
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