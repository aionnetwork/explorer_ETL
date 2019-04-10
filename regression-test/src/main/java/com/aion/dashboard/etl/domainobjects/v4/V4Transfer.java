package com.aion.dashboard.etl.domainobjects.v4;

import com.aion.dashboard.etl.domainobjects.base.AbstractTokenTransfer;
import com.aion.dashboard.etl.domainobjects.v5.V5TokenTransfer;

import java.math.BigDecimal;
import java.util.Objects;

public class V4Transfer extends AbstractTokenTransfer {

    private BigDecimal tokenValue;
    private long transactionId;
    private long block_Number;

    @Override
    public boolean compare(AbstractTokenTransfer tokenTransfer) {
        if(tokenTransfer instanceof V5TokenTransfer) {
            V5TokenTransfer v5TokenTransfer = (V5TokenTransfer) tokenTransfer;
            return super.compare(v5TokenTransfer) &&
                    block_Number == v5TokenTransfer.getBlockNumber() &&
                    tokenValue.longValueExact() == new BigDecimal(v5TokenTransfer.getRawValue()).longValueExact();
        } else return this.equals(tokenTransfer);
    }

    public V4Transfer(String operator, String toAddress, String fromAddress, BigDecimal tokenValue, String contractAddress, long transactionId, long block_Number, long transferTimestamp) {
        this.operator = operator;
        this.toAddress = toAddress;
        this.fromAddress = fromAddress;
        this.tokenValue = tokenValue;
        this.contractAddress = contractAddress;
        this.transactionId = transactionId;
        this.block_Number = block_Number;
        this.transferTimestamp = transferTimestamp;
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
        return block_Number;
    }

    public long getTransferTimestamp() {
        return transferTimestamp;
    }

    public String getOperator() {
        return operator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof V4Transfer)) return false;
        V4Transfer v4Transfer = (V4Transfer) o;
        return getTransactionId() == v4Transfer.getTransactionId() &&
                getBlockNumber() == v4Transfer.getBlockNumber() &&
                getTransferTimestamp() == v4Transfer.getTransferTimestamp() &&
                Objects.equals(getToAddress(), v4Transfer.getToAddress()) &&
                Objects.equals(getFromAddress(), v4Transfer.getFromAddress()) &&
                Objects.equals(getTokenValue(), v4Transfer.getTokenValue()) &&
                Objects.equals(getContractAddress(), v4Transfer.getContractAddress()) &&
                Objects.equals(getOperator(), v4Transfer.getOperator());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getToAddress(), getFromAddress(), getTokenValue(), getContractAddress(), getTransactionId(), getBlockNumber(), getTransferTimestamp(), getFromAddress());
    }

    public static class TransferBuilder {
        private String operator;
        private String toAddress;
        private String fromAddress;
        private BigDecimal tokenValue;
        private String contractAddress;
        private long transactionId;
        private long block_Number;
        private long transactionTimestamp;


        public TransferBuilder() {
        }

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

        public TransferBuilder setBlockNumber(long block_Number) {
            this.block_Number = block_Number;
            return this;
        }

        public TransferBuilder setTransferTimestamp(long transactionTimestamp) {
            this.transactionTimestamp = transactionTimestamp;
            return this;
        }


        public V4Transfer build() {
            return new V4Transfer(operator, toAddress, fromAddress, tokenValue, contractAddress, transactionId, block_Number, transactionTimestamp);
        }

        public TransferBuilder setOperator(String operator) {
            this.operator = operator;
            return this;
        }
    }
}
