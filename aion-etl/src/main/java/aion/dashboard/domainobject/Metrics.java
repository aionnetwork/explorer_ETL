package aion.dashboard.domainobject;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Metrics {


    public static final int RT_ID = 2;
    public static final int STABLE_ID = 1;
    private BigInteger totalTransactions;// last 24 hrs
    private BigDecimal transactionsPerSecond;//last hr
    private long peakTransactionsPerBlock;// last 24 hrs
    private long startBlock;// start of block metrics
    private long endBlock;// end of block metrics
    private BigDecimal averageNrgConsumed;// last hr
    private BigDecimal averageNrgLimit;//last hr
    private BigDecimal averageBlockTime;
    private BigDecimal averageDifficulty;
    private long endTimeStamp;
    private long startTimeStamp;
    private BigDecimal averageHashPower;
    private int id;
    private BigDecimal lastBlockReward;


    //This constructor is accessed only through a builder as such it is only accessed through the vpn
    private Metrics(BigInteger totalTransactions,
                    BigDecimal transactionsPerSecond,
                    long peakTransactionsPerBlock,
                    long startBlock,
                    long endBlock,
                    BigDecimal averageNrgConsumed,
                    BigDecimal averageNrgLimit,
                    BigDecimal averageBlockTime,
                    BigDecimal averageDifficulty,
                    long endTimeStamp,
                    long startTimeStamp,
                    BigDecimal averageHashPower,
                    int id,
                    BigDecimal lastBlockReward) {
        this.totalTransactions = totalTransactions;
        this.transactionsPerSecond = transactionsPerSecond;
        this.peakTransactionsPerBlock = peakTransactionsPerBlock;
        this.startBlock = startBlock;
        this.endBlock = endBlock;
        this.averageNrgConsumed = averageNrgConsumed;
        this.averageNrgLimit = averageNrgLimit;
        this.averageBlockTime = averageBlockTime;
        this.averageDifficulty = averageDifficulty;
        this.endTimeStamp = endTimeStamp;
        this.startTimeStamp = startTimeStamp;
        this.averageHashPower = averageHashPower;
        this.id = id;
        this.lastBlockReward = lastBlockReward;
    }

    public BigInteger getTotalTransactions() {
        return totalTransactions;
    }

    public BigDecimal getTransactionsPerSecond() {
        return transactionsPerSecond;
    }

    public long getPeakTransactionsPerBlock() {
        return peakTransactionsPerBlock;
    }

    public long getStartBlock() {
        return startBlock;
    }

    public long getEndBlock() {
        return endBlock;
    }

    public BigDecimal getAverageNrgConsumed() {
        return averageNrgConsumed;
    }

    public BigDecimal getAverageNrgLimit() {
        return averageNrgLimit;
    }

    public BigDecimal getAverageBlockTime() {
        return averageBlockTime;
    }

    public BigDecimal getAverageDifficulty() {
        return averageDifficulty;
    }

    public long getEndTimeStamp() {
        return endTimeStamp;
    }

    public long getStartTimeStamp() {
        return startTimeStamp;
    }

    public BigDecimal getAverageHashPower() {
        return averageHashPower;
    }

    public int getId() {
        return id;
    }

    public BigDecimal getLastBlockReward() {
        return lastBlockReward;
    }

    public static class MetricsBuilder{

        private BigInteger totalTransactions;
        private BigDecimal transactionsPerSecond;
        private long peakTransactionsPerBlock;
        private long startBlock;
        private long endBlock;
        private BigDecimal averageNrgConsumed;
        private BigDecimal averageNrgLimit;
        private BigDecimal averageBlockTime;
        private BigDecimal averageDifficulty;
        private BigDecimal averageHashPower;
        private long endTimeStamp;
        private long startTimeStamp;
        private int id;
        private BigInteger lastBlockReward;


        public MetricsBuilder setTotalTransactions(BigInteger totalTransactions) {
            this.totalTransactions = totalTransactions;
            return this;
        }

        public MetricsBuilder setTransactionsPerSecond(BigDecimal transactionsPerSecond) {
            this.transactionsPerSecond = transactionsPerSecond;
            return this;
        }

        public MetricsBuilder setPeakTransactionsPerBlock(long peakTransactionsPerBlock) {
            this.peakTransactionsPerBlock = peakTransactionsPerBlock;
            return this;
        }

        public MetricsBuilder setStartBlock(long startBlock) {
            this.startBlock = startBlock;
            return this;
        }

        public MetricsBuilder setEndBlock(long endBlock) {
            this.endBlock = endBlock;
            return this;
        }

        public MetricsBuilder setAverageNrgConsumed(BigDecimal averageNrgConsumed) {
            this.averageNrgConsumed = averageNrgConsumed;
            return this;
        }

        public MetricsBuilder setAverageNrgLimit(BigDecimal averageNrgLimit) {
            this.averageNrgLimit = averageNrgLimit;
            return this;
        }

        public MetricsBuilder setAverageBlockTime(BigDecimal averageBlockTime) {
            this.averageBlockTime = averageBlockTime;
            return this;
        }

        public MetricsBuilder setAverageDifficulty(BigDecimal averageDifficulty) {
            this.averageDifficulty = averageDifficulty;
            return this;
        }

        public MetricsBuilder setEndTimeStamp(long endTimeStamp) {
            this.endTimeStamp = endTimeStamp;
            return this;
        }

        public MetricsBuilder setStartTimeStamp(long startTimeStamp) {
            this.startTimeStamp = startTimeStamp;
            return this;
        }

        public MetricsBuilder setAverageHashPower(BigDecimal averageHashPower) {
            this.averageHashPower = averageHashPower;
            return this;
        }


        public Metrics build(){
            if (isValid(totalTransactions) && isValid(transactionsPerSecond) && isValid(peakTransactionsPerBlock)
                    && isValid(startBlock) && isValid(endBlock) && isValid(averageNrgConsumed) && isValid(averageNrgLimit)
                    && isValid(averageBlockTime) && isValid(averageDifficulty) && isValid(endTimeStamp) && isValid(startTimeStamp)
                    && isValid(averageHashPower) && (id == RT_ID || id == STABLE_ID)) {
                return new Metrics(totalTransactions,
                        transactionsPerSecond,
                        peakTransactionsPerBlock,
                        startBlock,
                        endBlock,
                        averageNrgConsumed,
                        averageNrgLimit,
                        averageBlockTime,
                        averageDifficulty,
                        endTimeStamp,
                        startTimeStamp,
                        averageHashPower,
                        id, new BigDecimal(lastBlockReward));
            }
            else throw new IllegalStateException("Failed to build metrics: " + this.toString());
        }

        @Override
        public String toString() {
            return "MetricsBuilder{" +
                    "totalTransactions=" + totalTransactions +
                    ", transactionsPerSecond=" + transactionsPerSecond +
                    ", peakTransactionsPerBlock=" + peakTransactionsPerBlock +
                    ", startBlock=" + startBlock +
                    ", endBlock=" + endBlock +
                    ", averageNrgConsumed=" + averageNrgConsumed +
                    ", averageNrgLimit=" + averageNrgLimit +
                    ", averageBlockTime=" + averageBlockTime +
                    ", averageDifficulty=" + averageDifficulty +
                    ", endTimeStamp=" + endTimeStamp +
                    ", startTimeStamp=" + startTimeStamp +
                    '}';
        }

        private static boolean isValid(BigDecimal bigDecimal){
            return bigDecimal !=null && bigDecimal.compareTo(BigDecimal.ZERO) >= 0;
        }

        private static boolean isValid(long along){
            return along>=0;
        }

        private static boolean isValid (BigInteger bigInteger){
            return bigInteger !=null && bigInteger.compareTo(BigInteger.ZERO) >= 0;

        }


        public MetricsBuilder setId(int id) {
            this.id = id;
            return this;
        }

        public MetricsBuilder setLastBlockReward(BigInteger lastBlockReward) {
            this.lastBlockReward = lastBlockReward;
            return this;
        }
    }
}
