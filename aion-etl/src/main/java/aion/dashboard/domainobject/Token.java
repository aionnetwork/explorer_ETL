package aion.dashboard.domainobject;

import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.Objects;

import static aion.dashboard.util.Utils.getZDT;

public class Token {
    private String contractAddress;
    private String transactionHash;
    private String creatorAddress;
    private BigInteger totalLiquidSupply;
    private BigInteger totalSupply;
    private BigInteger granularity;
    private int tokenDecimal;
    private String tokenName;
    private String symbol;
    private long blockTimestamp;
    private int blockYear;
    private int blockMonth;
    private int blockDay;



    // linting rule squid:S00107 can be ignored here since the constructor is only accessed via the builder method
    private  Token(String contractAddress, String transactionHash, String creatorAddress, BigInteger totalLiquidSupply, BigInteger totalSupply, BigInteger granularity, int tokenDecimal, String tokenName, String symbol, long blockTimestamp) {
        this.contractAddress = contractAddress;
        this.transactionHash = transactionHash;
        this.creatorAddress = creatorAddress;
        this.totalLiquidSupply = totalLiquidSupply;
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
        return "Token{" +
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
        Token token = (Token) o;
        return Objects.equals(contractAddress, token.contractAddress);
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

    public BigInteger getTotalLiquidSupply() {
        return totalLiquidSupply;
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
        private BigInteger totalLiquidSupply = BigInteger.ZERO;
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



        public TokenBuilder totalLiquidSupply(BigInteger totalLiquidSupply) {
            this.totalLiquidSupply = totalLiquidSupply;
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


        public Token build(){
            if(contractAddress.equals("") || creatorAddress.equals("") || transactionHash.equals("") || name.equals("") || symbol.equals("")||
            totalSupply.equals(BigInteger.ZERO) || totalLiquidSupply.equals(BigInteger.ZERO) || granularity.equals(BigInteger.ZERO) || timestamp<=0){
                throw new IllegalStateException("Falsely identified a token");
            }
            return new Token(contractAddress,  transactionHash,  creatorAddress, totalLiquidSupply,  totalSupply,  granularity, tokenDecimal, name,  symbol,  timestamp);
        }

        public TokenBuilder setTokenDecimal(int tokenDecimal) {
            this.tokenDecimal = tokenDecimal;
            return this;
        }
    }
}
