package aion.dashboard.domainobject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

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
    private BigDecimal powBlockDifficulty;
    private BigDecimal posBlockDifficulty;
    private BigDecimal powBlockTime;
    private BigDecimal posBlockTime;
    private BigDecimal averagePOSIssuance;
    private BigDecimal percentageOfNetworkStaking;
    private BigDecimal totalStake;


    //This constructor is accessed only through a builder as such it is only accessed through the vpn
    private Metrics(MetricsBuilder builder) {
        this.totalTransactions = builder.totalTransactions;
        this.transactionsPerSecond = builder.transactionsPerSecond;
        this.peakTransactionsPerBlock = builder.peakTransactionsPerBlock;
        this.startBlock = builder.startBlock;
        this.endBlock = builder.endBlock;
        this.averageNrgConsumed = builder.averageNrgConsumed;
        this.averageNrgLimit = builder.averageNrgLimit;
        this.averageBlockTime = builder.averageBlockTime;
        this.averageDifficulty = builder.averageDifficulty;
        this.endTimeStamp = builder.endTimeStamp;
        this.startTimeStamp = builder.startTimeStamp;
        this.averageHashPower = builder.averageHashPower;
        this.id = builder.id;
        this.lastBlockReward = new BigDecimal(builder.lastBlockReward);
        this.powBlockDifficulty = builder.powBlockDifficulty;
        this.posBlockDifficulty = builder.posBlockDifficulty;
        this.powBlockTime = builder.powBlockTime;
        this.posBlockTime = builder.posBlockTime;
        this.averagePOSIssuance = builder.averagePOSIssuance;
        this.percentageOfNetworkStaking = builder.percentageOfNetworkStaking;
        this.totalStake = builder.totalStake;
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

    public BigDecimal getPowBlockDifficulty() {
        return powBlockDifficulty;
    }

    public BigDecimal getPosBlockDifficulty() {
        return posBlockDifficulty;
    }

    public BigDecimal getPowBlockTime() {
        return powBlockTime;
    }

    public BigDecimal getPosBlockTime() {
        return posBlockTime;
    }

    public BigDecimal getAveragePOSIssuance() {
        return averagePOSIssuance;
    }

    public BigDecimal getPercentageOfNetworkStaking() {
        return percentageOfNetworkStaking;
    }

    public BigDecimal getTotalStake() {
        return totalStake;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Metrics)) return false;
        Metrics metrics = (Metrics) o;
        return endBlock == metrics.endBlock &&
                id == metrics.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(endBlock, id);
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
        private BigDecimal powBlockDifficulty;
        private BigDecimal posBlockDifficulty;
        private BigDecimal powBlockTime;
        private BigDecimal posBlockTime;
        private BigDecimal averagePOSIssuance;
        private BigDecimal percentageOfNetworkStaking;
        private BigDecimal totalStake;


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

        public MetricsBuilder setPowBlockDifficulty(BigDecimal powBlockDifficulty) {
            this.powBlockDifficulty = powBlockDifficulty;
            return this;
        }

        public MetricsBuilder setPosBlockDifficulty(BigDecimal posBlockDifficulty) {
            this.posBlockDifficulty = posBlockDifficulty;
            return this;
        }

        public MetricsBuilder setPowBlockTime(BigDecimal powBlockTime) {
            this.powBlockTime = powBlockTime;
            return this;
        }

        public MetricsBuilder setPosBlockTime(BigDecimal posBlockTime) {
            this.posBlockTime = posBlockTime;
            return this;
        }


        public MetricsBuilder setAveragePOSIssuance(BigDecimal averagePOSIssuance) {
            this.averagePOSIssuance = averagePOSIssuance;
            return this;
        }

        public MetricsBuilder setPercentageOfNetworkStaking(BigDecimal percentageOfNetworkStaking) {
            this.percentageOfNetworkStaking = percentageOfNetworkStaking;
            return this;
        }

        public MetricsBuilder setTotalStake(BigDecimal totalStake) {
            this.totalStake = totalStake;
            return this;
        }

        public Metrics build(){
            return new Metrics(this);
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
