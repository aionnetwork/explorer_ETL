package aion.dashboard.blockchain.type;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.aion.util.bytes.ByteUtil;

import java.math.BigInteger;
import java.util.List;

import static aion.dashboard.util.Utils.*;

@JsonDeserialize(builder = APITxDetails.Builder.class)
public class APITxDetails {

    private String blockHash;
    private BigInteger nrgPrice;
    private String logsBloom;
    private BigInteger nrgUsed;
    private String contractAddress;
    private int transactionIndex;
    private String transactionHash;
    private long blockNumber;
    private String root;
    private String from;
    private String to;
    private List<APITransactionLog> logs;
    private String status;
    private String input;
    private long timestamp;
    private String error;
    private BigInteger nonce;
    private byte type;
    private BigInteger value;
    private boolean hasInternalTransactions;
    private long nrgLimit;

    public APITxDetails(Builder builder) {
        blockHash = builder.blockHash;
        nrgPrice = builder.nrgPrice;
        logsBloom = builder.logsBloom;
        nrgUsed = builder.nrgUsed;
        contractAddress = builder.contractAddress == null ? "" : builder.contractAddress;
        transactionIndex = builder.transactionIndex;
        transactionHash = builder.transactionHash;
        blockNumber = builder.blockNumber;
        root = builder.root;
        from = builder.from;
        to = builder.to;
        logs = builder.logs;
        status = builder.status;
        input = builder.input;
        timestamp = builder.timestamp;
        error = builder.error;
        nonce = builder.nonce;
        type  = builder.type;
        value = builder.value;
        hasInternalTransactions = builder.hasInternalTransactions;
        nrgLimit = builder.nrg;
    }

    public boolean hasInternalTransactions(){
        return hasInternalTransactions;
    }
    public String getBlockHash() {
        return blockHash;
    }

    public BigInteger getNrgPrice() {
        return nrgPrice;
    }

    public String getLogsBloom() {
        return logsBloom;
    }

    public BigInteger getNrgUsed() {
        return nrgUsed;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public int getTransactionIndex() {
        return transactionIndex;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public String getRoot() {
        return root;
    }


    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public List<APITransactionLog> getLogs() {
        return logs;
    }


    public String getStatus() {
        return status;
    }

    public String getInput() {
        return input;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getError() {
        return error;
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public byte getType() {
        return type;
    }

    public BigInteger getValue() {
        return value;
    }

    public long getNrgLimit() {
        return nrgLimit;
    }

    @JsonPOJOBuilder(buildMethodName = "create", withPrefix = "set")
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    public static class Builder {
        private String blockHash;
        private BigInteger nrgPrice;
        private String logsBloom;
        private BigInteger nrgUsed;
        private String contractAddress;
        private int transactionIndex;
        private String transactionHash;
        private BigInteger gasLimit;
        private BigInteger cumulativeNrgUsed;
        private BigInteger gasUsed;
        private long blockNumber;
        private String root;
        private BigInteger cumulativeGasUsed;
        private String from;
        private String to;
        private List<APITransactionLog> logs;
        private BigInteger gasPrice;
        private String status;
        private String input;
        private long timestamp;
        private String error;
        private BigInteger nonce;
        private byte type;
        private BigInteger value;
        private boolean hasInternalTransactions;
        private long nrg;
        private long gas;

        private Builder() {
        }

        public Builder setNrg(long nrg) {
            this.nrg = nrg;
            return this;
        }

        public Builder setGas(long gas) {
            this.gas = gas;
            return this;
        }

        public Builder setBlockHash(String blockHash) {
            this.blockHash = blockHash;
            return this;
        }

        public Builder setValue(String value) {
            this.value = bigIntegerFromHex(value);
            return this;
        }

        public Builder setNrgPrice(String nrgPrice) {
            this.nrgPrice = bigIntegerFromHex(nrgPrice);
            return this;
        }

        public Builder setLogsBloom(String logsBloom) {
            this.logsBloom = logsBloom;
            return this;
        }

        public Builder setNrgUsed(String nrgUsed) {
            this.nrgUsed = bigIntegerFromHex(nrgUsed);
            return this;
        }

        public Builder setContractAddress(String contractAddress) {
            this.contractAddress = contractAddress;
            return this;
        }

        public Builder setTransactionIndex(String transactionIndex) {
            this.transactionIndex = intFromHexString(transactionIndex);
            return this;
        }

        public Builder setHash(String transactionHash) {
            this.transactionHash = transactionHash;
            return this;
        }

        public Builder setGasLimit(String gasLimit) {
            this.gasLimit = bigIntegerFromHex(gasLimit);
            return this;
        }

        public Builder setCumulativeNrgUsed(String cumulativeNrgUsed) {
            this.cumulativeNrgUsed = bigIntegerFromHex(cumulativeNrgUsed);
            return this;
        }

        public Builder setGasUsed(String gasUsed) {
            this.gasUsed = bigIntegerFromHex(gasUsed);
            return this;
        }

        public Builder setBlockNumber(String blockNumber) {
            this.blockNumber = longFromHexString(blockNumber);
            return this;
        }

        public Builder setRoot(String root) {
            this.root = root;
            return this;
        }

        public Builder setCumulativeGasUsed(String cumulativeGasUsed) {
            this.cumulativeGasUsed = bigIntegerFromHex(cumulativeGasUsed);
            return this;
        }

        public Builder setFrom(String from) {
            this.from = from;
            return this;
        }

        public Builder setTo(String to) {
            this.to = to;
            return this;
        }

        public Builder setLogs(List<APITransactionLog> logs) {
            this.logs = logs;
            return this;
        }

        public Builder setGasPrice(String gasPrice) {
            this.gasPrice = bigIntegerFromHex(gasPrice);
            return this;
        }

        public Builder setStatus(String status) {
            this.status = status;
            return this;
        }

        public Builder setInput(String input) {
            this.input = input;
            return this;
        }

        public Builder setTimestamp(String timestamp) {
            this.timestamp = longFromHexString(timestamp);
            return this;
        }

        public Builder setError(String error) {
            this.error = error;
            return this;
        }

        public Builder setHasInternalTransactions(boolean hasInternalTransactions){
            this.hasInternalTransactions = hasInternalTransactions;
            return this;
        }

        public Builder setNonce(String nonce) {
            this.nonce = bigIntegerFromHex(nonce);
            return this;
        }

        public Builder setType(String type) {
            this.type = (byte)intFromHexString(type);
            return this;
        }

        public APITxDetails create() {
            return new APITxDetails(this);
        }
    }


}
