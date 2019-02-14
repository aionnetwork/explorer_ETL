package aion.dashboard.domainobject;

import java.math.BigDecimal;
import java.util.Objects;

public class Transfer {

    private String operator;
    private String toAddress;
    private String fromAddress;
    private BigDecimal tokenValue;
    private String contractAddress;
    private long transactionId;
    private long blockNumber;
    private long transactionTimestamp;

    private Transfer(String operator, String toAddress, String fromAddress, BigDecimal tokenValue, String contractAddress, long transactionId, long blockNumber, long transactionTimestamp) {
        //This constructor is private and so can use more than 7 params
        this.operator = operator;
        this.toAddress = toAddress;
        this.fromAddress = fromAddress;
        this.tokenValue = tokenValue;
        this.contractAddress = contractAddress;
        this.transactionId = transactionId;
        this.blockNumber = blockNumber;
        this.transactionTimestamp = transactionTimestamp;
    }

    public String getToAddress() {
        return toAddress;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public BigDecimal getTokenValue() {
        return tokenValue;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public long getTransactionTimestamp() {
        return transactionTimestamp;
    }

    public String getOperator() {
        return operator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transfer)) return false;
        Transfer transfer = (Transfer) o;
        return getTransactionId() == transfer.getTransactionId() &&
                getBlockNumber() == transfer.getBlockNumber() &&
                getTransactionTimestamp() == transfer.getTransactionTimestamp() &&
                Objects.equals(getToAddress(), transfer.getToAddress()) &&
                Objects.equals(getFromAddress(), transfer.getFromAddress()) &&
                Objects.equals(getTokenValue(), transfer.getTokenValue()) &&
                Objects.equals(getContractAddress(), transfer.getContractAddress()) &&
                Objects.equals(getOperator(), transfer.getOperator());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getToAddress(), getFromAddress(), getTokenValue(), getContractAddress(), getTransactionId(), getBlockNumber(), getTransactionTimestamp(), getFromAddress());
    }

    public static class TransferBuilder {
        private String operator;
        private String toAddress;
        private String fromAddress;
        private BigDecimal tokenValue;
        private String contractAddress;
        private long transactionId;
        private long blockNumber;
        private long transactionTimestamp;



        public TransferBuilder setToAddress(String toAddress) {
            this.toAddress = toAddress;
            return this;
        }

        public TransferBuilder setFromAddress(String fromAddress) {
            this.fromAddress = fromAddress;
            return this;
        }

        public TransferBuilder setTokenValue(BigDecimal tokenValue) {
            this.tokenValue = tokenValue;
            return this;
        }

        public TransferBuilder setContractAddress(String contractAddress) {
            this.contractAddress = contractAddress;
            return this;
        }

        public TransferBuilder setTransactionId(long transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public TransferBuilder setBlockNumber(long blockNumber) {
            this.blockNumber = blockNumber;
            return this;
        }

        public TransferBuilder setTransactionTimestamp(long transactionTimestamp) {
            this.transactionTimestamp = transactionTimestamp;
            return this;
        }


        public Transfer build() {
            return new Transfer(operator, toAddress, fromAddress, tokenValue, contractAddress, transactionId, blockNumber, transactionTimestamp);
        }

        public TransferBuilder setOperator(String operator) {
            this.operator = operator;
            return this;
        }
    }
}
