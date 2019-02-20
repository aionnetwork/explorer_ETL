package com.aion.dashboard.etl.domainobjects.v5;

import com.aion.dashboard.etl.domainobjects.base.AbstractTokenTransfer;
import com.aion.dashboard.etl.domainobjects.v4.V4Transfer;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;

import static com.aion.dashboard.etl.util.Utils.getZDT;

public class V5TokenTransfer extends AbstractTokenTransfer {
    private String rawValue;
    private String transactionHash;
    private BigDecimal scaledValue;
    private BigDecimal granularity;
    private long blockNumber;
    private int tokenDecimal;
    private int blockYear;
    private int blockMonth;
    private int blockDay;

    @Override
    public boolean compare(AbstractTokenTransfer tokenTransfer) {
        if(tokenTransfer instanceof V4Transfer) {
            V4Transfer v4Transfer = (V4Transfer) tokenTransfer;
            return super.compare(v4Transfer) &&
                    blockNumber == v4Transfer.getBlockNumber() &&
                    new BigDecimal(rawValue).longValueExact() == v4Transfer.getTokenValue().longValueExact();
        } else return this.equals(tokenTransfer);
    }

    public V5TokenTransfer(String operator, String toAddress, String fromAddress, BigDecimal value, String contractAddress, String transactionHash, String rawValue, int tokenDecimal, BigDecimal granularity, long blockNumber, long transferTimestamp) {
        this.operator = operator;
        this.toAddress = toAddress;
        this.fromAddress = fromAddress;
        this.scaledValue = value;
        this.contractAddress = contractAddress;
        this.transactionHash = transactionHash;
        this.rawValue = rawValue;
        this.tokenDecimal = tokenDecimal;
        this.granularity = granularity;
        this.blockNumber = blockNumber;
        this.transferTimestamp = transferTimestamp;

        ZonedDateTime zdt = getZDT(transferTimestamp);
        blockDay = zdt.getDayOfMonth();
        blockMonth = zdt.getMonthValue();
        blockYear = zdt.getYear();
    }

    public String getToAddress() {
        return toAddress;
    }

    public String getRawValue() {
        return rawValue;
    }

    public int getTokenDecimal() {
        return tokenDecimal;
    }

    public BigDecimal getGranularity() {
        return granularity;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public BigDecimal getScaledValue() {
        return scaledValue;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public long getBlockNumber() {
        return blockNumber;
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
        if (!(o instanceof V5TokenTransfer)) return false;
        V5TokenTransfer v5TokenTransfers = (V5TokenTransfer) o;
        return getTransactionHash() == v5TokenTransfers.getTransactionHash() &&
                getBlockNumber() == v5TokenTransfers.getBlockNumber() &&
                getTransferTimestamp() == v5TokenTransfers.getTransferTimestamp() &&
                Objects.equals(getToAddress(), v5TokenTransfers.getToAddress()) &&
                Objects.equals(getFromAddress(), v5TokenTransfers.getFromAddress()) &&
                Objects.equals(getScaledValue(), v5TokenTransfers.getScaledValue()) &&
                Objects.equals(getContractAddress(), v5TokenTransfers.getContractAddress()) &&
                Objects.equals(getOperator(), v5TokenTransfers.getOperator());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getToAddress(), getFromAddress(), getScaledValue(), getContractAddress(), getTransactionHash(), getBlockNumber(), getTransferTimestamp(), getFromAddress());
    }
    public int getBlockYear() {
        return blockYear;
    }


    public int getBlockMonth() {
        return blockMonth;
    }

    public int getBlockDay() {
        return blockDay;
    }
    public static class TokenTransferBuilder {
        private String operator;
        private String toAddress;
        private String fromAddress;
        private BigDecimal tokenValue;
        private String contractAddress;
        private String transactionHash;
        private long blockNumber;
        private long transactionTimestamp;
        private String rawValue;
        private int tokendecimal;
        private BigDecimal granularity;

        public TokenTransferBuilder setRawValue(String rawValue) {
            this.rawValue = rawValue;
            return this;
        }

        public TokenTransferBuilder setTokenDecimal(int tokendecimal) {
            this.tokendecimal = tokendecimal;
            return this;
        }

        public TokenTransferBuilder setGranularity(BigDecimal granularity) {
            this.granularity = granularity;
            return this;
        }

        public TokenTransferBuilder() {
        }

        public TokenTransferBuilder setToAddress(String toAddress) {
            this.toAddress = toAddress;
            return this;
        }

        public TokenTransferBuilder setFromAddress(String fromAddress) {
            this.fromAddress = fromAddress;
            return this;
        }

        public TokenTransferBuilder setScaledTokenValue(BigDecimal tokenValue) {
            this.tokenValue = tokenValue;
            return this;
        }

        public TokenTransferBuilder setContractAddress(String contractAddress) {
            this.contractAddress = contractAddress;
            return this;
        }

        public TokenTransferBuilder setTransactionHash(String transactionHash) {
            this.transactionHash = transactionHash;
            return this;
        }

        public TokenTransferBuilder setBlockNumber(long blockNumber) {
            this.blockNumber = blockNumber;
            return this;
        }

        public TokenTransferBuilder setTransactionTimestamp(long transactionTimestamp) {
            this.transactionTimestamp = transactionTimestamp;
            return this;
        }


        public V5TokenTransfer build() {
            return new V5TokenTransfer(operator, toAddress, fromAddress, tokenValue, contractAddress, transactionHash, rawValue, tokendecimal, granularity, blockNumber, transactionTimestamp);
        }

        public TokenTransferBuilder setOperator(String operator) {
            this.operator = operator;
            return this;
        }
    }
}
