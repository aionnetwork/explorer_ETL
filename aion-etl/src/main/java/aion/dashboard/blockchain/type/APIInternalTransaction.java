package aion.dashboard.blockchain.type;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.math.BigInteger;

@JsonDeserialize(builder = APIInternalTransaction.APIInternalTransactionBuilder.class)
public class APIInternalTransaction {

    private BigInteger nrgPrice;
    private BigInteger nrgLimit;
    private String data;
    private boolean rejected;
    private String kind;
    private String from;
    private String to;
    private BigInteger nonce;
    private BigInteger value;
    private String contractAddress;

    private APIInternalTransaction(APIInternalTransactionBuilder builder){
        this.nrgPrice = builder.nrgPrice;
        this.nrgLimit = builder.nrgLimit;
        this.data = builder.data;
        this.rejected = builder.rejected;
        this.kind = builder.kind;
        this.from = builder.from;
        this.to = builder.to;
        this.nonce = builder.nonce;
        this.value = builder.value;
        this.contractAddress = builder.contractAddress;
    }

    public BigInteger getNrgPrice() {
        return nrgPrice;
    }

    public BigInteger getNrgLimit() {
        return nrgLimit;
    }

    public String getData() {
        return data;
    }

    public boolean isRejected() {
        return rejected;
    }

    public String getKind() {
        return kind;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public BigInteger getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "APIInternalTransaction{" +
                "nrgPrice=" + nrgPrice +
                ", nrgLimit=" + nrgLimit +
                ", data='" + data + '\'' +
                ", rejected=" + rejected +
                ", kind='" + kind + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", nonce=" + nonce +
                ", value=" + value +
                ", contractAddress='" + contractAddress + '\'' +
                '}';
    }

    public String getContractAddress() {
        return contractAddress;
    }

    @JsonPOJOBuilder(buildMethodName = "create", withPrefix = "set")
    public static class APIInternalTransactionBuilder {
        private BigInteger nrgPrice;
        private BigInteger nrgLimit;
        private String data;
        private boolean rejected;
        private String kind;
        private String from;
        private String to;
        private BigInteger nonce;
        private BigInteger value;
        private String contractAddress;


        public APIInternalTransactionBuilder setNrgPrice(BigInteger nrgPrice) {
            this.nrgPrice = nrgPrice;
            return this;
        }

        public APIInternalTransactionBuilder setNrgLimit(BigInteger nrgLimit) {
            this.nrgLimit = nrgLimit;
            return this;
        }

        public APIInternalTransactionBuilder setData(String data) {
            this.data = data;
            return this;
        }

        public APIInternalTransactionBuilder setRejected(boolean rejected) {
            this.rejected = rejected;
            return this;
        }

        public APIInternalTransactionBuilder setKind(String kind) {
            this.kind = kind;
            return this;
        }

        public APIInternalTransactionBuilder setFrom(String from) {
            this.from = from;
            return this;
        }

        public APIInternalTransactionBuilder setTo(String to) {
            this.to = to;
            return this;
        }

        public APIInternalTransactionBuilder setNonce(BigInteger nonce) {
            this.nonce = nonce;
            return this;
        }

        public APIInternalTransactionBuilder setValue(BigInteger value) {
            this.value = value;
            return this;
        }

        public APIInternalTransaction create(){
            return new APIInternalTransaction(this);
        }

        public APIInternalTransactionBuilder setContractAddress(String contractAddress) {
            this.contractAddress = contractAddress;
            return this;
        }
    }
}
