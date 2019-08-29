package aion.dashboard.domainobject;

import aion.dashboard.blockchain.type.APIInternalTransaction;
import aion.dashboard.util.Utils;
import jdk.jshell.execution.Util;

import java.math.BigDecimal;

public class InternalTransaction {

    private static ThreadLocal<Builder> builder = ThreadLocal.withInitial(Builder::new);
    private BigDecimal nrgPrice;
    private BigDecimal nrgLimit;
    private String data;
    private String kind;
    private String fromAddr;
    private String toAddr;
    private BigDecimal nonce;
    private BigDecimal value;
    private long blockTimestamp;
    private long blockNumber;
    private String transactionHash;
    private boolean rejected;
    private int internalTransactionIndex;
    private String contractAddress;

    private InternalTransaction(Builder builder) {
        this.nrgPrice = builder.nrgPrice;
        this.nrgLimit = builder.nrgLimit;
        this.data = builder.data;
        this.kind = builder.kind;
        this.fromAddr = builder.fromAddr;
        this.toAddr = builder.toAddr;
        this.nonce = builder.nonce;
        this.value = builder.value;
        this.blockTimestamp = builder.blockTimestamp;
        this.blockNumber = builder.blockNumber;
        this.transactionHash = builder.transactionHash;
        this.internalTransactionIndex = builder.internalTransactionIndex;
        this.rejected = builder.rejected;
        this.contractAddress = builder.contractAddress;
    }

    public static InternalTransaction from(APIInternalTransaction apiInternalTransaction, String txHash, int index, long blockNumber, long blockTimestamp) {
        return builder()
                .setBlockNumber(blockNumber)
                .setTransactionHash(Utils.sanitizeHex(txHash))
                .setInternalTransactionIndex(index)
                .setData(Utils.sanitizeHex(apiInternalTransaction.getData()))
                .setFromAddr(Utils.sanitizeHex(apiInternalTransaction.getFrom()))
                .setToAddr(Utils.sanitizeHex(apiInternalTransaction.getTo()))
                .setKind(apiInternalTransaction.getKind())
                .setNonce(new BigDecimal(apiInternalTransaction.getNonce()))
                .setNrgLimit(new BigDecimal(apiInternalTransaction.getNrgLimit()))
                .setNrgPrice(new BigDecimal(apiInternalTransaction.getNrgPrice()))
                .setValue(new BigDecimal(apiInternalTransaction.getValue()))
                .setRejected(apiInternalTransaction.isRejected())
                .setBlockTimestamp(blockTimestamp)
                .setContractAddress(Utils.sanitizeHex(apiInternalTransaction.getContractAddress()))
                .create();
    }

    public static Builder builder() {
        return builder.get();
    }

    public BigDecimal getNrgPrice() {
        return nrgPrice;
    }

    public BigDecimal getNrgLimit() {
        return nrgLimit;
    }

    public String getData() {
        return data;
    }

    public String getKind() {
        return kind;
    }

    public String getFromAddr() {
        return fromAddr;
    }

    public String getToAddr() {
        return toAddr;
    }

    public BigDecimal getNonce() {
        return nonce;
    }

    public BigDecimal getValue() {
        return value;
    }

    public long getBlockTimestamp() {
        return blockTimestamp;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public int getInternalTransactionIndex() {
        return internalTransactionIndex;
    }

    public boolean isRejected() {
        return rejected;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    @Override
    public String toString() {
        return "InternalTransaction{" +
                "nrgPrice=" + nrgPrice +
                ", nrgLimit=" + nrgLimit +
                ", data='" + data + '\'' +
                ", kind='" + kind + '\'' +
                ", fromAddr='" + fromAddr + '\'' +
                ", toAddr='" + toAddr + '\'' +
                ", nonce=" + nonce +
                ", value=" + value +
                ", blockTimestamp=" + blockTimestamp +
                ", blockNumber=" + blockNumber +
                ", transactionHash='" + transactionHash + '\'' +
                ", rejected=" + rejected +
                ", internalTransactionIndex=" + internalTransactionIndex +
                ", contractAddress='" + contractAddress + '\'' +
                '}';
    }

    private static class Builder {
        BigDecimal nrgPrice;
        BigDecimal nrgLimit;
        String data;
        String kind;
        String fromAddr;
        String toAddr;
        BigDecimal nonce;
        BigDecimal value;
        long blockTimestamp;
        long blockNumber;
        String transactionHash;
        int internalTransactionIndex;
        private boolean rejected;
        private String contractAddress;

        public Builder setContractAddress(String contractAddress) {
            this.contractAddress = contractAddress;
            return this;
        }

        public Builder setRejected(boolean rejected) {
            this.rejected = rejected;
            return this;
        }

        public Builder setNrgPrice(BigDecimal nrgPrice) {
            this.nrgPrice = nrgPrice;
            return this;
        }

        public Builder setNrgLimit(BigDecimal nrgLimit) {
            this.nrgLimit = nrgLimit;
            return this;
        }

        public Builder setData(String data) {
            this.data = data;
            return this;
        }

        public Builder setKind(String kind) {
            this.kind = kind;
            return this;
        }

        public Builder setFromAddr(String fromAddr) {
            this.fromAddr = fromAddr;
            return this;
        }

        public Builder setToAddr(String toAddr) {
            this.toAddr = toAddr;
            return this;
        }

        public Builder setNonce(BigDecimal nonce) {
            this.nonce = nonce;
            return this;
        }

        public Builder setValue(BigDecimal value) {
            this.value = value;
            return this;
        }

        public Builder setBlockTimestamp(long blockTimestamp) {
            this.blockTimestamp = blockTimestamp;
            return this;
        }

        public Builder setBlockNumber(long blockNumber) {
            this.blockNumber = blockNumber;
            return this;
        }

        public Builder setTransactionHash(String transactionHash) {
            this.transactionHash = transactionHash;
            return this;
        }

        public Builder setInternalTransactionIndex(int internalTransactionIndex) {
            this.internalTransactionIndex = internalTransactionIndex;
            return this;
        }

        public InternalTransaction create() {
            return new InternalTransaction(this);
        }

    }
}
