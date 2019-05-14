package aion.dashboard.service;

import aion.dashboard.blockchain.AionService;
import aion.dashboard.domainobject.Graphing;
import aion.dashboard.domainobject.Metrics;
import aion.dashboard.domainobject.ParserState;
import aion.dashboard.exception.AionApiException;
import org.aion.api.type.BlockDetails;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

public class RollingBlockMeanImpl implements RollingBlockMean {
    private Set<BlockDetails> blockHistory;
    private Set<StrippedTransaction> transactionHistory;
    private final int blockTimeWindow;
    private final int blockMaxSize;
    private final long transactionTimeWindow;
    private final long blockcountwindow;

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
     * @param blockTimeWindow the start of the period in which values are computed
     * @param transactionTimeWindow the size of the transaction window that should be maintained in seconds
     * @param blockCountWindow  the size of the block window to use for the RT statistics
     */
    RollingBlockMeanImpl(long meanPointer, long transactionPointer, long dbPointer, AionService service, int blockMaxSize, int blockTimeWindow, long transactionTimeWindow, long blockCountWindow) throws AionApiException {
        //This constructor is ok because it is only accessed through a factory method

        blockcountwindow = blockCountWindow;

        service.reconnect();


        this.transactionTimeWindow = transactionTimeWindow;
        this.blockMaxSize = blockMaxSize;
        this.blockTimeWindow = blockTimeWindow;
        blockHistory = new ConcurrentSkipListSet<>(Comparator.comparingLong(BlockDetails::getNumber));
        blockHistory.addAll(getBlockDetailsInRange(meanPointer, dbPointer, service));
        transactionHistory = getBlockDetailsInRange(transactionPointer, dbPointer, service).parallelStream()
                .filter(b -> !b.getTxDetails().isEmpty())
                .map(b -> new StrippedTransaction(b.getTimestamp(), b.getNumber(), b.getTxDetails().size()))
                .sorted(Comparator.comparing(t-> t.blockNumber))
                .collect(Collectors.toCollection(ConcurrentSkipListSet::new));


    }

    private List<BlockDetails> getBlockDetailsInRange(long start, long end, AionService service) throws AionApiException {
        long startPointer = start;
        long requestSize = (end - startPointer) <= 1000 ? start - end : 1000;

        List<BlockDetails> temp = new ArrayList<>();

        if (end > 0) {
            while (requestSize > 0){
                long endPointer  = startPointer + requestSize-1 ;// get range inclusive of request pointer
                temp.addAll(service.getBlockDetailsByRange(startPointer, endPointer));
                startPointer =temp.get(temp.size() -1).getNumber()+1;
                requestSize = (end - startPointer+1) <= 1000 ? end - startPointer+1: 1000;
            }
        }
        return temp;
    }


    @Override
    public void add(BlockDetails blockDetails) {
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

    }

    @Override
    public synchronized void reorg(long consistentBlock) {

        blockHistory.removeIf(b -> b.getNumber() >= consistentBlock);
        transactionHistory.removeIf(st -> st.blockNumber>= consistentBlock);
    }


    @Override
    public long getStartOfBlockWindow() {
        return blockHistory.parallelStream().mapToLong(BlockDetails::getNumber).min().orElse(1);
    }

    @Override
    public long getStartOfTransactionWindow() {
        return transactionHistory.parallelStream().mapToLong(tx -> tx.blockNumber).min().orElse(getStartOfBlockWindow());
    }

    @Override
    public Optional<Metrics> computeStableMetricsFrom(long blockNumber) {
        if (blockNumber<1) return Optional.empty();
        else {
            Optional<BlockDetails> detailsAtNum = blockHistory.parallelStream()
                    .unordered()
                    .filter( b -> b.getNumber() == blockNumber)
                    .findAny();

            if (detailsAtNum.isEmpty()) return Optional.empty();
            else {
                long endTime = detailsAtNum.get().getTimestamp();

                List<BlockDetails> blockDetails = blockHistory.parallelStream()
                        .filter(e ->
                                detailsAtNum.get().getNumber() >= e.getNumber()
                                        && Math.abs(endTime - e.getTimestamp()) <= blockTimeWindow * 60// eliminate any blocks that were after the last block
                        )
                        .sorted(Comparator.comparing(BlockDetails::getNumber))
                        .collect(Collectors.toUnmodifiableList());

                return doMetricsComputation(blockDetails, Metrics.STABLE_ID);
            }
        }
    }

    @Override
    public Optional<Metrics> computeRTMetricsFrom(long blockNumber) {

        if (blockNumber<1) return Optional.empty();
        else {

            Optional<BlockDetails> detailsAtNum = blockHistory.parallelStream()
                    .unordered()
                    .filter( b -> b.getNumber() == blockNumber)
                    .findAny();
            if (!detailsAtNum.isPresent()) return Optional.empty();
            final long startBlock = detailsAtNum.get().getNumber() - blockcountwindow;

            List<BlockDetails> blockDetails = blockHistory.parallelStream()
                    .filter(b -> b.getNumber() > startBlock)//get range exclusive of the start block
                    .collect(Collectors.toList());

            return doMetricsComputation(blockDetails, Metrics.RT_ID);
        }

    }


    /**
     * TODO consider switching this to a loop
     * Computes the metrics to be sent to the dashboard and alerts
     * @param blockDetails
     * @return
     */
    private Optional<Metrics> doMetricsComputation(List<BlockDetails> blockDetails, int id) {
        if (blockDetails.isEmpty()) return Optional.empty();

        final BigDecimal averageDifficulty = blockDetails.parallelStream()
                .map(b -> new BigDecimal(b.getDifficulty()))//Get the difficulty of each block
                .reduce(BigDecimal.ZERO, BigDecimal::add)//Accumulate the difficulties
                .divide(BigDecimal.valueOf(blockDetails.size()), MathContext.DECIMAL64);//Find the average


        final BigDecimal averageBlockTime = blockDetails.parallelStream()
                .map(b -> BigDecimal.valueOf(b.getBlockTime()))//get the block time of each block
                .reduce(BigDecimal.ZERO, BigDecimal::add)//Accumulate the time
                .divide(BigDecimal.valueOf(blockDetails.size()), MathContext.DECIMAL64);// find the average over the period


        final BigInteger transactionsOver24Hrs = transactionHistory.parallelStream()
                .map(t -> BigInteger.valueOf(t.numberTransactions))
                .reduce(BigInteger.ZERO,BigInteger::add);

        final long PeakTransactions= transactionHistory.parallelStream()
                .mapToLong(t -> t.numberTransactions)
                .max()
                .orElse(0);
        final BigDecimal averageHashPower = new BigDecimal(blockDetails.get(blockDetails.size() - 1).getDifficulty())//assumption the last block has the greatest block number
                .divide(averageBlockTime, MathContext.DECIMAL64);// find the hash power by dividing the difficulty and the time for each block

        final BigDecimal averageNrgLimit =  blockDetails.parallelStream()
                .unordered()//disregard order... This is a performance optimization
                .map(b -> BigDecimal.valueOf(b.getNrgLimit()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)//sum up the nrg limit
                .divide(BigDecimal.valueOf(blockDetails.size()), MathContext.DECIMAL64);//find the average

        final BigDecimal averageNrgConsumed = blockDetails.parallelStream()
                .unordered()//disregard order... This is a performance optimization
                .map(b-> b.getTxDetails().stream().map(t-> new BigDecimal(t.getNrgConsumed())).reduce(BigDecimal.ZERO,BigDecimal::add))
                .reduce(BigDecimal.ZERO,BigDecimal::add)
                .divide(BigDecimal.valueOf(blockDetails.size()), MathContext.DECIMAL128);

        //noinspection OptionalGetWithoutIsPresent
        final long StartTimeStamp = blockDetails.parallelStream().mapToLong(BlockDetails::getTimestamp).min().getAsLong();
        //noinspection OptionalGetWithoutIsPresent
        final long EndTimeStamp = blockDetails.parallelStream().mapToLong(BlockDetails::getTimestamp).max().getAsLong();
        final BigDecimal averageTxnsPerSecond = blockDetails.parallelStream()
                .map(b -> BigDecimal.valueOf(b.getTxDetails().size()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(EndTimeStamp - StartTimeStamp), MathContext.DECIMAL64);


        return Optional.of(
                new Metrics.MetricsBuilder()
                        .setAverageBlockTime(averageBlockTime)
                        .setAverageDifficulty(averageDifficulty)
                        .setAverageHashPower(averageHashPower)
                        .setAverageNrgConsumed(averageNrgConsumed)
                        .setAverageNrgLimit(averageNrgLimit)
                        .setStartTimeStamp(StartTimeStamp)
                        .setEndTimeStamp(EndTimeStamp)
                        .setEndBlock(blockDetails.parallelStream().mapToLong(BlockDetails::getNumber).max().getAsLong())
                        .setStartBlock(blockDetails.parallelStream().mapToLong(BlockDetails::getNumber).min().getAsLong())
                        .setPeakTransactionsPerBlock(PeakTransactions)
                        .setTotalTransactions(transactionsOver24Hrs)
                        .setTransactionsPerSecond(averageTxnsPerSecond)
                        .setId(id)
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
