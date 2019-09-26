package aion.dashboard.service;

import aion.dashboard.blockchain.interfaces.Web3Service;
import aion.dashboard.blockchain.type.APIBlockDetails;
import aion.dashboard.config.Config;
import aion.dashboard.domainobject.Metrics;
import aion.dashboard.domainobject.ParserState;
import aion.dashboard.exception.Web3ApiException;
import aion.dashboard.util.MetricsCalc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static aion.dashboard.blockchain.type.APIBlock.SealType.POS;
import static aion.dashboard.blockchain.type.APIBlock.SealType.POW;
import static aion.dashboard.util.MetricsCalc.*;

public class RollingBlockMeanImpl implements RollingBlockMean {
    private Set<APIBlockDetails> blockHistory;
    private Set<StrippedTransaction> transactionHistory;
    private final int blockStableWindow;
    private final int blockMaxSize;
    private final long transactionTimeWindow;
    private final long blockRTWindow;
    private final AtomicReference<BigInteger> blockReward = new AtomicReference<>(BigInteger.ZERO);
    private final Web3Service apiService;
    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");
    private class StrippedTransaction implements Comparable<StrippedTransaction>{
        StrippedTransaction(long timeStamp, long blockNumber, long numberTransactions) {
            this.timeStamp = timeStamp;
            this.blockNumber = blockNumber;
            this.numberTransactions = numberTransactions;
        }

        final long timeStamp;//epochSeconds
        final long blockNumber;
        final long numberTransactions;

        @Override
        public int hashCode() {
            return Objects.hashCode(blockNumber);
        }

        @Override
        public int compareTo(StrippedTransaction o) {
            return Long.compare(this.blockNumber, o.blockNumber);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof StrippedTransaction
            && this.blockNumber == ((StrippedTransaction) obj).blockNumber;
        }
    }

    /**
     * Creates a new instance with the initial size of the window populated.
     * @param meanPointer the start of the window
     * @param dbPointer
     * @param service used to populate the Window
     * @param blockMaxSize the max size of the window
     * @param blockStableWindow the start of the period in which values are computed
     * @param transactionTimeWindow the size of the transaction window that should be maintained in seconds
     * @param blockCountWindow  the size of the block window to use for the RT statistics
     */
    RollingBlockMeanImpl(long meanPointer, long transactionPointer, long dbPointer, Web3Service service, int blockMaxSize, int blockStableWindow, long transactionTimeWindow, long blockCountWindow) throws Web3ApiException {
        //This constructor is ok because it is only accessed through a factory method

        blockRTWindow = blockCountWindow;
        Thread.currentThread().setName("rolling-mean");
        apiService = service;
        this.transactionTimeWindow = transactionTimeWindow;
        this.blockMaxSize = blockMaxSize;
        this.blockStableWindow = blockStableWindow;
        blockHistory = new ConcurrentSkipListSet<>(Comparator.comparingLong(APIBlockDetails::getNumber));
        blockHistory.addAll(getBlockDetailsInRange(meanPointer, dbPointer, service));
        transactionHistory = getBlockDetailsInRange(transactionPointer, dbPointer, service).parallelStream()
                .filter(b -> !b.getTxDetails().isEmpty())
                .map(b -> new StrippedTransaction(b.getTimestamp(), b.getNumber(), b.getTxDetails().size()))
                .sorted(Comparator.comparing(t-> t.blockNumber))
                .collect(Collectors.toCollection(ConcurrentSkipListSet::new));


    }

    private List<APIBlockDetails> getBlockDetailsInRange(long start, long end, Web3Service service) throws Web3ApiException {
        long startPointer = start;
        long maxSize = Config.getInstance().getBlockQueryRange();
        long requestSize = (end - startPointer) <= maxSize ? start - end : maxSize;

        List<APIBlockDetails> temp = new ArrayList<>();

        if (end > 0) {
            while (requestSize > 0){
                long endPointer  = startPointer + requestSize-1 ;// get range inclusive of request pointer
                temp.addAll(service.getBlockDetailsInRange(startPointer, endPointer));
                startPointer =temp.get(temp.size() -1).getNumber()+1;
                requestSize = Math.min((end - startPointer + 1), maxSize);
            }
        }
        return temp;
    }

    @Override
    public void add(APIBlockDetails blockDetails) {
//Avoid duplicates
        blockHistory.remove(blockDetails);
        blockHistory.add(blockDetails);

        if (!blockDetails.getTxDetails().isEmpty()) {
            var tx =new StrippedTransaction(blockDetails.getTimestamp(),
                    blockDetails.getNumber(),
                    blockDetails.getTxDetails().size());
            transactionHistory.remove(tx);
            transactionHistory.add(tx);
        }
        final long minBlock = blockDetails.getNumber() - blockMaxSize;

        blockHistory.removeIf(b -> b.getNumber() <= minBlock);
        long now = blockDetails.getTimestamp();
        long weekAgo = now - (transactionTimeWindow * 60);

        transactionHistory.removeIf(st -> st.timeStamp < weekAgo);
        //update the block reward
        this.blockReward.set(blockDetails.getBlockReward());
    }

    public synchronized void reorg(long consistentBlock) {

        blockHistory.removeIf(b -> b.getNumber() >= consistentBlock);
        transactionHistory.removeIf(st -> st.blockNumber>= consistentBlock);
    }


    @Override
    public long getStartOfBlockWindow() {
        return blockHistory.parallelStream().mapToLong(APIBlockDetails::getNumber).min().orElse(1);
    }

    @Override
    public long getStartOfTransactionWindow() {
        return transactionHistory.parallelStream().mapToLong(tx -> tx.blockNumber).min().orElse(getStartOfBlockWindow());
    }

    @Override
    public Optional<Metrics> computeStableMetricsFrom(long blockNumber) {
        return getMetrics(blockNumber, blockStableWindow, Metrics.STABLE_ID);
    }

    @Override
    public Optional<Metrics> computeRTMetricsFrom(long blockNumber) {
        return getMetrics(blockNumber, blockRTWindow, Metrics.RT_ID);

    }

    private Optional<Metrics> getMetrics(long blockNumber, long windowSize, int stableId) {
        try {
            if (blockNumber < 1) return Optional.empty();
            else {
                List<APIBlockDetails> blockDetails = findBlocksInRange(blockNumber - windowSize, blockNumber );
                if (blockDetails.isEmpty()) return Optional.empty();
                else return doMetricsComputation(blockDetails, stableId);
            }
        } catch (Exception e) {
            GENERAL.debug("Encountered an error while computing metrics at block number {} and window size {}", blockNumber, windowSize);
            GENERAL.debug("Error:",e);
            return Optional.empty();
        }
    }

    List<APIBlockDetails> findBlocksInRange(long startBlockExclusive, long endBlockInclusive) {
        final long blockNumberToUse = Math.max(startBlockExclusive, 0);// no block before genesis
        return blockHistory.parallelStream()
                .unordered()
                .filter( b->b.getNumber() > blockNumberToUse && b.getNumber()<=endBlockInclusive) // find the block that matches this block number
                .collect(Collectors.toList());
    }


    /**
     * TODO consider switching this to a loop
     * Computes the metrics to be sent to the dashboard and alerts
     * @param blockDetails
     * @return
     */
    private Optional<Metrics> doMetricsComputation(List<APIBlockDetails> blockDetails, int id) {
        if (blockDetails.isEmpty()) return Optional.empty();

        final List<APIBlockDetails> posBlocks = blockDetails.stream()
                .filter(b->b.getSealType().equals(POS))
                .collect(Collectors.toUnmodifiableList());
        final List<APIBlockDetails> powBlocks = blockDetails.stream()
                .filter(b->b.getSealType().equals(POW))
                .collect(Collectors.toUnmodifiableList());

        final BigDecimal averageNetworkDifficulty = blockDetails.parallelStream()
                .map(b -> new BigDecimal(b.getDifficulty()))//Get the difficulty of each block
                .reduce(BigDecimal.ZERO, BigDecimal::add)//Accumulate the difficulties
                .divide(BigDecimal.valueOf(blockDetails.size()).max(BigDecimal.ONE), MathContext.DECIMAL64);//Find the average


        final BigDecimal averageNetworkBlockTime = blockDetails.parallelStream()
                .map(b -> BigDecimal.valueOf(b.getBlockTime()))//get the block time of each block
                .reduce(BigDecimal.ZERO, BigDecimal::add)//Accumulate the time
                .divide(BigDecimal.valueOf(blockDetails.size()).max(BigDecimal.ONE), MathContext.DECIMAL64);// find the average over the period


        final BigInteger transactionsOver24Hrs = transactionHistory.parallelStream()
                .map(t -> BigInteger.valueOf(t.numberTransactions))
                .reduce(BigInteger.ZERO,BigInteger::add);

        final long PeakTransactions= transactionHistory.parallelStream()
                .mapToLong(t -> t.numberTransactions)
                .max()
                .orElse(0);

        final BigDecimal averageNrgLimit =  blockDetails.parallelStream()
                .unordered()//disregard order... This is a performance optimization
                .map(b -> BigDecimal.valueOf(b.getNrgLimit()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)//sum up the nrg limit
                .divide(BigDecimal.valueOf(blockDetails.size()), MathContext.DECIMAL64);//find the average

        final BigDecimal averageNrgConsumed = blockDetails.parallelStream()
                .unordered()//disregard order... This is a performance optimization
                .map(b-> b.getTxDetails().stream().map(t-> new BigDecimal(t.getNrgUsed())).reduce(BigDecimal.ZERO,BigDecimal::add))
                .reduce(BigDecimal.ZERO,BigDecimal::add)
                .divide(BigDecimal.valueOf(blockDetails.size()), MathContext.DECIMAL128);

        //noinspection OptionalGetWithoutIsPresent
        final long StartTimeStamp = blockDetails.parallelStream().mapToLong(APIBlockDetails::getTimestamp).min().getAsLong();
        //noinspection OptionalGetWithoutIsPresent
        final long EndTimeStamp = blockDetails.parallelStream().mapToLong(APIBlockDetails::getTimestamp).max().getAsLong();
        final BigDecimal averageTxnsPerSecond = blockDetails.parallelStream()
                .map(b -> BigDecimal.valueOf(b.getTxDetails().size()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(EndTimeStamp - StartTimeStamp).max(BigDecimal.ONE), MathContext.DECIMAL64);
        final long endBlock =blockDetails.parallelStream().mapToLong(APIBlockDetails::getNumber).max().getAsLong();
        final BigDecimal networkStake = MetricsCalc.networkStake(endBlock);
        return Optional.of(
                new Metrics.MetricsBuilder()
                        .setAverageBlockTime(averageNetworkBlockTime)
                        .setAverageDifficulty(averageNetworkDifficulty)
                        .setAverageHashPower(hashRate(powBlocks))
                        .setAverageNrgConsumed(averageNrgConsumed)
                        .setAverageNrgLimit(averageNrgLimit)
                        .setStartTimeStamp(StartTimeStamp)
                        .setEndTimeStamp(EndTimeStamp)
                        .setEndBlock(endBlock)
                        .setStartBlock(blockDetails.parallelStream().mapToLong(APIBlockDetails::getNumber).min().orElseThrow())
                        .setPeakTransactionsPerBlock(PeakTransactions)
                        .setTotalTransactions(transactionsOver24Hrs)
                        .setTransactionsPerSecond(averageTxnsPerSecond)
                        .setId(id)
                        .setLastBlockReward(blockReward.get())
                        .setPosBlockDifficulty(avgDifficulty(posBlocks))
                        .setPowBlockDifficulty(avgDifficulty(powBlocks))
                        .setPosBlockTime(avgBlockTime(posBlocks))
                        .setPowBlockTime(avgBlockTime(powBlocks))
                        .setAveragePOSIssuance(avgIssuance(posBlocks))
                        .setTotalStake(networkStake)
                        .setPercentageOfNetworkStaking(networkStakingPercentage(Instant.ofEpochSecond(EndTimeStamp), networkStake).orElse(BigDecimal.ZERO))
                        .build()
        );
    }

    @Override
    public List<ParserState> getStates() {
        var builder = new ParserState.ParserStateBuilder();

        return List.of(
                builder.id(ParserStateServiceImpl.BLOCK_MEAN_ID).blockNumber(BigInteger.valueOf(getStartOfBlockWindow())).build(),
                builder.id(ParserStateServiceImpl.TRANSACTION_MEAN_ID).blockNumber(BigInteger.valueOf(getStartOfTransactionWindow())).build()
        );
    }


}
