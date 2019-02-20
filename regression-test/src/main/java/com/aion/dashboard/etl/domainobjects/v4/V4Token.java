package com.aion.dashboard.etl.domainobjects.v4;

import com.aion.dashboard.etl.domainobjects.base.AbstractToken;
import com.aion.dashboard.etl.domainobjects.v5.V5Token;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public class V4Token extends AbstractToken {
    private BigInteger totalLiquidSupply;
    private BigDecimal granularity;
    private long timestamp;

    @Override
    public boolean compare(AbstractToken token) {
        if(token instanceof V5Token) {
            V5Token v5Token = (V5Token) token;
            return super.compare(v5Token) &&
                    timestamp == v5Token.getBlockTimestamp() &&
                    totalLiquidSupply.equals(v5Token.getLiquidSupply()) &&
                    granularity.toBigInteger().equals(v5Token.getGranularity());
        } else return this.equals(token);
    }

    private V4Token() {
    }

    private V4Token(String contractAddress, String transactionHash, String creatorAddress, BigInteger totalLiquidSupply, BigInteger totalSupply, BigDecimal granularity, String tokenName, String symbol, long timestamp) {
        this.contractAddress = contractAddress;
        this.transactionHash = transactionHash;
        this.creatorAddress = creatorAddress;
        this.totalLiquidSupply = totalLiquidSupply;
        this.totalSupply = totalSupply;
        this.granularity = granularity;
        this.tokenName = tokenName;
        this.symbol = symbol;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Token{" +
                "contractAddress='" + contractAddress + '\'' +
                ", transactionHash='" + transactionHash + '\'' +
                ", creatorAddress='" + creatorAddress + '\'' +
                ", timestamp='" + timestamp +
                ", totalSupply=" + totalSupply +
                ", granularity=" + granularity +
                ", tokenName='" + tokenName + '\'' +
                ", symbol='" + symbol + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        V4Token token = (V4Token) o;
        return Objects.equals(contractAddress, token.contractAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contractAddress);
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public String getCreatorAddress() {
        return creatorAddress;
    }

//    public String getSpecialAddress() {
//        return specialAddress;
//    }

    public BigInteger getTotalLiquidSupply() {
        return totalLiquidSupply;
    }

    public BigInteger getTotalSupply() {
        return totalSupply;
    }

    public BigDecimal getGranularity() {
        return granularity;
    }

    public String getTokenName() {
        return tokenName;
    }

    public String getSymbol() {
        return symbol;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public static class TokenBuilder {

        private String contractAddress = "";
        private String transactionHash = "";
        private String creatorAddress = "";

        private BigInteger totalLiquidSupply = BigInteger.ZERO;
        private BigInteger totalSupply = BigInteger.ZERO;
        private BigDecimal granularity = BigDecimal.ZERO;
        private String name = "";
        private String symbol = "";
        private long timestamp = -1;

        public TokenBuilder contractAddress(String contractAddress) {
            this.contractAddress = contractAddress;
            return this;
        }

        public TokenBuilder transactionHash(String transactionHash) {
            this.transactionHash = transactionHash;
            return this;
        }

        public TokenBuilder creatorAddress(String creatorAddress) {
            this.creatorAddress = creatorAddress;
            return this;
        }


        public TokenBuilder totalLiquidSupply(BigInteger totalLiquidSupply) {
            this.totalLiquidSupply = totalLiquidSupply;
            return this;
        }

        public TokenBuilder totalSupply(BigInteger totalSupply) {
            this.totalSupply = totalSupply;
            return this;
        }

        public TokenBuilder granularity(BigDecimal granularity) {
            this.granularity = granularity;
            return this;
        }

        public TokenBuilder name(String name) {
            this.name = name;
            return this;
        }

        public TokenBuilder symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public TokenBuilder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public TokenBuilder() {

        }

        public V4Token build() {
            if (contractAddress.equals("") || creatorAddress.equals("") || transactionHash.equals("") || name.equals("") || symbol.equals("") ||
                    totalSupply.equals(BigInteger.ZERO) || totalLiquidSupply.equals(BigInteger.ZERO) || granularity.equals(BigDecimal.ZERO) || timestamp <= 0) {
                throw new IllegalStateException("Falsely identified a token");
            }
            return new V4Token(contractAddress, transactionHash, creatorAddress,
                    totalLiquidSupply, totalSupply, granularity, name, symbol, timestamp);
        }
    }
}
