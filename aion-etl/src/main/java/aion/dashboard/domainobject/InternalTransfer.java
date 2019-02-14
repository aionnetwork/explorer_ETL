package aion.dashboard.domainobject;

import java.math.BigDecimal;
import java.util.Objects;

import static aion.dashboard.util.Utils.approximate;

public class InternalTransfer {
    private String fromAddr;//contractAddr
    private String toAddr;//The recipient
    private String transactionHash;//the hash this transfer occured
    private BigDecimal valueTransferred; // the amount transferred
    private long blockNumber; //block number
    private long timestamp;// timestamp of transfer
    private int transferCount;// the number of this transfer in the txlog... added to handle unlikely duplicates
    private double approxValue;

    public InternalTransfer(String fromAddr, String toAddr, String transactionHash, BigDecimal valueTransferred, long blockNumber, long timestamp, int transferCount) {
        this.fromAddr = fromAddr;
        this.toAddr = toAddr;
        this.transactionHash = transactionHash;
        this.valueTransferred = valueTransferred;
        this.blockNumber = blockNumber;
        this.timestamp = timestamp;
        this.transferCount = transferCount;

        approxValue = approximate(valueTransferred, 18);
    }

    public String getFromAddr() {
        return fromAddr;
    }

    public String getToAddr() {
        return toAddr;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public BigDecimal getValueTransferred() {
        return valueTransferred;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getTransferCount(){
        return transferCount;
    }

    public double getApproxValue() {
        return approxValue;
    }

    public static class InternalTransferBuilder{

        private String fromAddr;
        private String toAddr;
        private String transactionHash;
        private BigDecimal valueTransferred;
        private long blockNumber;
        private long timestamp;
        private int transferCount;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof InternalTransferBuilder)) return false;
            InternalTransferBuilder that = (InternalTransferBuilder) o;
            return blockNumber == that.blockNumber &&
                    timestamp == that.timestamp &&
                    transferCount == that.transferCount &&
                    Objects.equals(fromAddr, that.fromAddr) &&
                    Objects.equals(toAddr, that.toAddr) &&
                    Objects.equals(transactionHash, that.transactionHash) &&
                    Objects.equals(valueTransferred, that.valueTransferred);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fromAddr, toAddr, transactionHash, valueTransferred, blockNumber, timestamp, transferCount);
        }

        public InternalTransferBuilder setFromAddr(String fromAddr) {
            this.fromAddr = fromAddr;
            return this;
        }

        public InternalTransferBuilder setToAddr(String toAddr) {
            this.toAddr = toAddr;
            return this;
        }

        public InternalTransferBuilder setTransactionHash(String transactionHash) {
            this.transactionHash = transactionHash;
            return this;
        }


        public InternalTransferBuilder setValueTransferred(BigDecimal valueTransferred) {
            this.valueTransferred = valueTransferred;
            return this;
        }

        public InternalTransferBuilder setBlockNumber(long blockNumber) {
            this.blockNumber = blockNumber;
            return this;
        }

        public InternalTransferBuilder setTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }


        public InternalTransfer build(){
            return new InternalTransfer(fromAddr, toAddr, transactionHash, valueTransferred, blockNumber, timestamp, transferCount);
        }

        public InternalTransferBuilder setTransferCount(int transferCount) {
            this.transferCount = transferCount;
            return this;
        }
    }
}

