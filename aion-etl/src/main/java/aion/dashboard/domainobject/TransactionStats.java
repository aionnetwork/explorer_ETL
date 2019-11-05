package aion.dashboard.domainobject;

import java.math.BigDecimal;

public class TransactionStats {

    private final long numberOfTransactions;
    private final long blockNumber;
    private final long timestamp;
    private final long numberOfActiveAddresses;
    private final BigDecimal totalSpent;

    public TransactionStats(long numberOfTransactions, long blockNumber, long timestamp,
                            long numberOfActiveAddresses, BigDecimal totalSpent) {
        this.numberOfTransactions = numberOfTransactions;
        this.blockNumber = blockNumber;
        this.timestamp = timestamp;
        this.numberOfActiveAddresses = numberOfActiveAddresses;
        this.totalSpent = totalSpent;
    }

    public long getNumberOfTransactions() {
        return numberOfTransactions;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getNumberOfActiveAddresses() {
        return numberOfActiveAddresses;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }
}
