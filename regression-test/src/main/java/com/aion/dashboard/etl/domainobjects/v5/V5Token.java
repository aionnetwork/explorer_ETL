package com.aion.dashboard.etl.domainobjects.v5;

import com.aion.dashboard.etl.domainobjects.base.AbstractToken;
import com.aion.dashboard.etl.domainobjects.v4.V4Token;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.Objects;

import static com.aion.dashboard.etl.util.Utils.getZDT;

public class V5Token extends AbstractToken {
    private BigInteger liquidSupply;
    private BigInteger granularity;
    private int tokenDecimal;
    private long blockTimestamp;
    private int blockYear;
    private int blockMonth;
    private int blockDay;

    @Override
    public boolean compare(AbstractToken token) {
        if(token instanceof V4Token) {
            V4Token v4Token = (V4Token) token;
            return super.compare(v4Token) &&
                    blockTimestamp == v4Token.getTimestamp() &&
                    liquidSupply.equals(v4Token.getTotalLiquidSupply()) &&
                    new BigDecimal(granularity).equals(v4Token.getGranularity());
        } else return this.equals(token);
    }

    private V5Token(){}

    private V5Token(String contractAddress, String transactionHash, String creatorAddress, BigInteger liquidSupply, BigInteger totalSupply, BigInteger granularity, int tokenDecimal, String tokenName, String symbol, long blockTimestamp) {
        this.contractAddress = contractAddress;
        this.transactionHash = transactionHash;
        this.creatorAddress = creatorAddress;
        this.liquidSupply = liquidSupply;
        this.totalSupply = totalSupply;
        this.granularity = granularity;
        this.tokenDecimal = tokenDecimal;
        this.tokenName = tokenName;
        this.symbol = symbol;
        this.blockTimestamp = blockTimestamp;
        ZonedDateTime zdt = getZDT(blockTimestamp);
        blockYear = zdt.getYear();
        blockMonth = zdt.getMonthValue();
        blockDay = zdt.getDayOfMonth();
    }

    @Override
    public String toString() {
        return "V4Token{" +
                "contractAddress='" + contractAddress + '\'' +
                ", transactionHash='" + transactionHash + '\'' +
                ", creatorAddress='" + creatorAddress + '\'' +
                ", blockTimestamp='"+ blockTimestamp +
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
        V5Token v5Token = (V5Token) o;
        return Objects.equals(contractAddress, v5Token.contractAddress);
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

    public BigInteger getLiquidSupply() {
        return liquidSupply;
    }

    public BigInteger getTotalSupply() {
        return totalSupply;
    }

    public BigInteger getGranularity() {
        return granularity;
    }

    public String getTokenName() {
        return tokenName;
    }

    public String getSymbol() {
        return symbol;
    }

    public long getBlockTimestamp() {
        return blockTimestamp;
    }

    public int getTokenDecimal() {
        return tokenDecimal;
    }

    public static class TokenBuilder {

        private String contractAddress="";
        private String transactionHash="";
        private String creatorAddress="";
        private BigInteger liquidSupply = BigInteger.ZERO;
        private BigInteger totalSupply = BigInteger.ZERO;
        private BigInteger granularity = BigInteger.ZERO;
        private String name = "";
        private String symbol = "";
        private long timestamp =-1;
        private int tokenDecimal;

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



        public TokenBuilder liquidSupply(BigInteger liquidSupply) {
            this.liquidSupply = liquidSupply;
            return this;
        }

        public TokenBuilder totalSupply(BigInteger totalSupply) {
            this.totalSupply = totalSupply;
            return this;
        }

        public TokenBuilder granularity(BigInteger granularity) {
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

        public V5Token build(){
            if(contractAddress.equals("") || creatorAddress.equals("") || transactionHash.equals("") || name.equals("") || symbol.equals("")||
                    totalSupply.equals(BigInteger.ZERO) || liquidSupply.equals(BigInteger.ZERO) || granularity.equals(BigInteger.ZERO) || timestamp<=0){
                throw new IllegalStateException("Falsely identified a token");
            }
            return new V5Token(contractAddress,  transactionHash,  creatorAddress, liquidSupply,  totalSupply,  granularity, tokenDecimal, name,  symbol,  timestamp);
        }

        public TokenBuilder tokenDecimal(int tokenDecimal) {
            this.tokenDecimal = tokenDecimal;
            return this;
        }
    }
}