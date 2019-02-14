package aion.dashboard.domainobject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public class Token {
    private String contractAddress;
    private String transactionHash;
    private String creatorAddress;

    private BigInteger totalLiquidSupply;
    private BigInteger totalSupply;
    private BigDecimal granularity;
    private String tokenName;
    private String symbol;
    private long timestamp;


    private Token(){}

    private  Token(String contractAddress, String transactionHash, String creatorAddress,   BigInteger totalLiquidSupply, BigInteger totalSupply, BigDecimal granularity, String tokenName, String symbol, long timestamp) {
        //This constructor is private and so it can have more than 7 params
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
                ", timestamp='"+timestamp +
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
        Token token = (Token) o;
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

        private String contractAddress="";
        private String transactionHash="";
        private String creatorAddress="";

        private BigInteger totalLiquidSupply = BigInteger.ZERO;
        private BigInteger totalSupply=BigInteger.ZERO;
        private BigDecimal granularity=BigDecimal.ZERO;
        private String name="";
        private String symbol="";
        private long timestamp=-1;

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




        public Token build(){
            if(contractAddress.equals("") || creatorAddress.equals("") || transactionHash.equals("") || name.equals("") || symbol.equals("")||
            totalSupply.equals(BigInteger.ZERO) || totalLiquidSupply.equals(BigInteger.ZERO) ||granularity.equals(BigDecimal.ZERO) || timestamp<=0){
                throw new IllegalStateException("Falsely identified a token");
            }
            return new Token( contractAddress,  transactionHash,  creatorAddress,
                    totalLiquidSupply,  totalSupply,  granularity,  name,  symbol,  timestamp);
        }
    }
}
