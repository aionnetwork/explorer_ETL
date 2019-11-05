package aion.dashboard.stats;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.db.SharedDBLocks;
import aion.dashboard.domainobject.AccountStats;
import aion.dashboard.domainobject.ParserState;
import aion.dashboard.domainobject.Transaction;
import aion.dashboard.domainobject.TransactionStats;
import aion.dashboard.service.*;
import aion.dashboard.util.Utils;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TransactionStatsTask implements Runnable {

    public static final int NUM_BLOCKS_IN_DAY = 60480;//calculate the stats for a week
    public static final int FREQUENCY = 4320;//update every 12 hours
    private static final SharedDBLocks sharedDbLocks = SharedDBLocks.getInstance();
    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");
    private final ParserStateService parserStateService = new ParserStateServiceImpl();
    private final TransactionService transactionService = TransactionServiceImpl.getInstance();
    private final TransactionStatsService transactionStatsService = new TransactionStatsServiceImpl();
    private final AccountStatsService accountStatsService = new AccountStatsServiceImpl();
    private final ExecutorService es = Executors.newSingleThreadExecutor();

    public void start() {
        es.submit(this);
    }

    public void stop() {
        es.shutdownNow();
    }

    @Override
    public void run() {
        Thread.currentThread().setName("account-stats");
        do {
            long curr = parserStateService.readAccountInfoState().getBlockNumber().longValue() + 1;
            try {
                while (Utils.trySleep(1000)//allow the reorg task to get the lock if it was requested
                        && sharedDbLocks.tryLockDbWrite()) {
                    try {
                        long end = curr + NUM_BLOCKS_IN_DAY - 1;
                        GENERAL.info("Computing Accounts Stats at block number {}", end);
                        if (canUpdate(curr)) {// run the check within the critical section since we
                            // cannot check if we get the right number of transactions from the db
                            Stopwatch stopwatch = Stopwatch.createStarted();
                            List<Transaction> transactions = transactionService
                                    .getTransactionsInRange(Math.max(1, end - NUM_BLOCKS_IN_DAY + 1), end);
                            stopwatch.stop();
                            GENERAL.info("Read {} transactions in {}s.", transactions.size(), stopwatch.elapsed(TimeUnit.SECONDS));
                            computeAndWrite(curr,end, transactions);
                            curr += FREQUENCY;
                        } else break;
                    } finally {
                        sharedDbLocks.unlockDBWrite();
                    }
                }
            } catch (Exception e) {
                GENERAL.warn("Unable to compute transactions stats at block number: {}", curr);
            }
        } while (Utils.trySleep(60_000L) && !Thread.currentThread().isInterrupted());//run this every 1 minute
    }

    private void computeAndWrite(long curr, long blockNumber, List<Transaction> transactions)
            throws SQLException {
        if (!transactions.isEmpty()) {//we do not store any data if this this transaction list is empty
            write(computeAccountStats(transactions, blockNumber), computeTransactionsStats(transactions, blockNumber),
                    new ParserState.ParserStateBuilder().id(ParserStateServiceImpl.ACCOUNT_STATS_INFO)
                            .blockNumber(BigInteger.valueOf(curr+FREQUENCY -1)).build());
        }
    }

    private boolean canUpdate(long curr) {
        return curr + NUM_BLOCKS_IN_DAY - 1 < parserStateService.readDBState().getBlockNumber()
                .longValue();
    }

    private List<AccountStats> computeAccountStats(List<Transaction> transactions, long curr) {
        Map<String, AccountStats> stringAccountsStatsMap = new HashMap<>();
        long timestamp = transactions.stream()// get the max timestamp of these stats
                .map(Transaction::getBlockTimestamp)
                .max(Long::compareTo)
                .orElseThrow();
        for (Transaction transaction : transactions) {// iterate through all transactions over the last day
            if (!transaction.getToAddr().isEmpty() &&
                    !stringAccountsStatsMap.containsKey(transaction.getToAddr())) {
                stringAccountsStatsMap.put(transaction.getToAddr(), new AccountStats(transaction.getToAddr(), timestamp, curr));
            }
            if (!transaction.getContractAddr().isEmpty() &&
                    !stringAccountsStatsMap.containsKey(transaction.getContractAddr())) {
                stringAccountsStatsMap
                        .put(transaction.getContractAddr(), new AccountStats(transaction.getContractAddr(), timestamp, curr));
            }
            if (!stringAccountsStatsMap.containsKey(transaction.getFromAddr())) {
                stringAccountsStatsMap.put(transaction.getFromAddr(), new AccountStats(transaction.getFromAddr(), timestamp, curr));
            }

            if (!transaction.getToAddr().isEmpty()) {
                stringAccountsStatsMap.get(transaction.getToAddr())
                        .in(transaction.getValue());// accumulate value transferred into the account
            }

            if (!transaction.getContractAddr().isEmpty()) {
                stringAccountsStatsMap.get(transaction.getContractAddr())
                        .in(transaction.getValue());// accumulate value transferred into the account
            }
            stringAccountsStatsMap.get(transaction.getFromAddr())
                    .out(transaction.getValue());// accumulate value transferred out of the account
        }
        return List.copyOf(stringAccountsStatsMap.values());
    }

    private TransactionStats computeTransactionsStats(List<Transaction> transactions, long curr) {
        Set<String> activeAddress = new HashSet<>();
        long timestamp = transactions.stream()// get the max timestamp of these stats
                .map(Transaction::getBlockTimestamp)
                .max(Long::compareTo)
                .orElseThrow();
        BigDecimal totalSpent = transactions.stream()
                .map(Transaction::getValue)//calculate the total amount of aion transferred
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        for (Transaction t : transactions) {//find all the unique addresses
            activeAddress.add(t.getFromAddr());
            if (!t.getToAddr().isEmpty()) {
                activeAddress.add(t.getToAddr());
            }
        }
        return new TransactionStats(transactions.size(), curr, timestamp, activeAddress.size(), totalSpent);
    }

    public void write(List<AccountStats> accountStats, TransactionStats transactionStats,
                      ParserState ps)
            throws SQLException {
        Connection con = DbConnectionPool.getConnection();
        try (
                PreparedStatement parserState = parserStateService.prepare(con, Collections.singletonList(ps));
                PreparedStatement accountsStats = accountStatsService.prepare(con, accountStats);
                PreparedStatement transactionsStats = transactionStatsService.prepare(con, transactionStats)) {
            parserState.executeBatch();
            accountsStats.executeBatch();
            transactionsStats.executeBatch();
            con.commit();
        } catch (Exception e) {
            con.rollback();
        } finally {
            con.close();
        }
    }
}
