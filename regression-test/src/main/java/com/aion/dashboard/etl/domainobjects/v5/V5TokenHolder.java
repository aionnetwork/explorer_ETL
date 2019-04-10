package com.aion.dashboard.etl.domainobjects.v5;

import com.aion.dashboard.etl.domainobjects.base.AbstractTokenHolder;
import com.aion.dashboard.etl.domainobjects.v4.V4TokenBalance;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public class V5TokenHolder extends AbstractTokenHolder {
    private int tokenDecimal;
    private String rawBalance;
    private BigInteger granularity;
    private BigDecimal scaledBalance;

    @Override
    public boolean compare(AbstractTokenHolder tokenHolder) {
        if(tokenHolder instanceof V4TokenBalance) {
            V4TokenBalance v4TokenBalance = (V4TokenBalance) tokenHolder;
            return super.compare(v4TokenBalance) &&
                    new BigDecimal(rawBalance).longValueExact() == v4TokenBalance.getBalance().longValueExact();
        } else return this.equals(tokenHolder);
    }

    private V5TokenHolder(String contractAddress, BigDecimal scaledBalance, String holderAddress, long blockNumber, String rawBalance, int tokenDecimal, BigInteger tokenGranularity) {
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
        if (!(o instanceof V5TokenHolder)) return false;
        V5TokenHolder that = (V5TokenHolder) o;
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

    public static class TokenHolderBuilder {


        private String contractAddress;
        private BigDecimal scaledBalance;
        private String holderAddress;
        private long blockNumber;
        private String rawBalance;
        private int tokenDecimal;
        private BigInteger tokenGranularity;



        public TokenHolderBuilder() {

        }

        public TokenHolderBuilder setRawBalance(String rawBalance) {
            this.rawBalance = rawBalance;
            return this;
        }

        public TokenHolderBuilder setTokenDecimal(int tokenDecimal) {
            this.tokenDecimal = tokenDecimal;
            return this;
        }

        public TokenHolderBuilder setTokenGranularity(BigInteger tokenGranularity) {
            this.tokenGranularity = tokenGranularity;
            return this;
        }

        public TokenHolderBuilder setScaledBalance(BigDecimal balance) {
            this.scaledBalance = balance;
            return this;
        }

        public TokenHolderBuilder setContractAddress(String contractAddress) {
            this.contractAddress = contractAddress;
            return this;
        }

        public TokenHolderBuilder setHolderAddress(String holderAddress) {
            this.holderAddress = holderAddress;
            return this;
        }


        public V5TokenHolder build() {
            return new V5TokenHolder(contractAddress, scaledBalance, holderAddress, blockNumber, rawBalance, tokenDecimal, tokenGranularity);
        }

        public TokenHolderBuilder setBlockNumber(long blockNumber) {
            this.blockNumber = blockNumber;
            return this;
        }
    }
}
