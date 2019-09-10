package aion.dashboard.domainobject;

import aion.dashboard.blockchain.type.APIBlockDetails;
import aion.dashboard.util.Utils;
import org.aion.api.type.BlockDetails;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public class TokenHolders {


    private String contractAddress;
    private BigDecimal scaledBalance;
    private String holderAddress;
    private long blockNumber;
    private String rawBalance;
    private int tokenDecimal; // the decimal of the token. Token decimal
    private BigInteger granularity;


    private TokenHolders(String contractAddress, BigDecimal scaledBalance, String holderAddress, long blockNumber, String rawBalance, int tokenDecimal, BigInteger tokenGranularity) {
        this.contractAddress = contractAddress;
        this.scaledBalance = scaledBalance;
        this.holderAddress = holderAddress;
        this.blockNumber = blockNumber;
        this.rawBalance = rawBalance;
        this.tokenDecimal = tokenDecimal;
        this.granularity = tokenGranularity;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public BigDecimal getScaledBalance() {
        return scaledBalance;
    }

    public String getHolderAddress() {
        return holderAddress;
    }

    public long getBlockNumber() {
        return blockNumber;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TokenHolders)) return false;
        TokenHolders that = (TokenHolders) o;
        return getBlockNumber() == that.getBlockNumber() &&
                getTokenDecimal() == that.getTokenDecimal() &&
                Objects.equals(getContractAddress(), that.getContractAddress()) &&
                Objects.equals(getScaledBalance(), that.getScaledBalance()) &&
                Objects.equals(getHolderAddress(), that.getHolderAddress()) &&
                Objects.equals(getRawBalance(), that.getRawBalance()) &&
                Objects.equals(getGranularity(), that.getGranularity());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContractAddress(), getScaledBalance(), getHolderAddress(), getBlockNumber(), getRawBalance(), getTokenDecimal(), getGranularity());
    }

    public String getRawBalance() {
        return rawBalance;
    }

    public int getTokenDecimal() {
        return tokenDecimal;
    }

    public BigInteger getGranularity() {
        return granularity;
    }


    private static final ThreadLocal<TokenBalanceBuilder> threadLocalBuilder = ThreadLocal.withInitial(TokenBalanceBuilder::new);

    @Deprecated
    public static TokenHolders from(String address, BlockDetails b, Token tkn, BigInteger rawBalance){
        return threadLocalBuilder.get()
                .setBlockNumber(b.getNumber())
                .setContractAddress(tkn.getContractAddress().replace("0x",""))
                .setHolderAddress(address.replace("0x",""))
                .setRawBalance(rawBalance.toString())
                .setTokenDecimal(tkn.getTokenDecimal())
                .setScaledBalance(Utils.scaleTokenValue(rawBalance,tkn.getTokenDecimal()))
                .setTokenGranularity(tkn.getGranularity())
                .build();
    }

    public static TokenHolders from(String address, APIBlockDetails b, Token tkn, BigInteger rawBalance){
        return threadLocalBuilder.get()
                .setBlockNumber(b.getNumber())
                .setContractAddress(tkn.getContractAddress().replace("0x",""))
                .setHolderAddress(address.replace("0x",""))
                .setRawBalance(rawBalance.toString())
                .setTokenDecimal(tkn.getTokenDecimal())
                .setScaledBalance(Utils.scaleTokenValue(rawBalance,tkn.getTokenDecimal()))
                .setTokenGranularity(tkn.getGranularity())
                .build();
    }


    public static class TokenBalanceBuilder {


        private String contractAddress;
        private BigDecimal scaledBalance;
        private String holderAddress;
        private long blockNumber;
        private String rawBalance;
        private int tokenDecimal;
        private BigInteger tokenGranularity;

        

        public TokenBalanceBuilder setRawBalance(String rawBalance) {
            this.rawBalance = rawBalance;
            return this;
        }

        public TokenBalanceBuilder setTokenDecimal(int tokenDecimal) {
            this.tokenDecimal = tokenDecimal;
            return this;
        }

        public TokenBalanceBuilder setTokenGranularity(BigInteger tokenGranularity) {
            this.tokenGranularity = tokenGranularity;
            return this;
        }

        public TokenBalanceBuilder setScaledBalance(BigDecimal balance) {
            this.scaledBalance = balance;
            return this;
        }

        public TokenBalanceBuilder setContractAddress(String contractAddress) {
            this.contractAddress = contractAddress;
            return this;
        }

        public TokenBalanceBuilder setHolderAddress(String holderAddress) {
            this.holderAddress = holderAddress;
            return this;
        }


        public TokenHolders build() {
            return new TokenHolders(contractAddress, scaledBalance, holderAddress, blockNumber, rawBalance, tokenDecimal, tokenGranularity);
        }

        public TokenBalanceBuilder setBlockNumber(long blockNumber) {
            this.blockNumber = blockNumber;
            return this;
        }
    }
}
