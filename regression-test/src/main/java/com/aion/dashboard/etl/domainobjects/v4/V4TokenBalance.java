package com.aion.dashboard.etl.domainobjects.v4;

import com.aion.dashboard.etl.domainobjects.base.AbstractTokenHolder;
import com.aion.dashboard.etl.domainobjects.v5.V5TokenHolder;

import java.math.BigDecimal;
import java.util.Objects;

public class V4TokenBalance extends AbstractTokenHolder {
    private BigDecimal balance;

    @Override
    public boolean compare(AbstractTokenHolder tokenHolder) {
        if(tokenHolder instanceof V5TokenHolder) {
            V5TokenHolder v5TokenHolder = (V5TokenHolder) tokenHolder;
            return super.compare(v5TokenHolder) &&
                    balance.longValueExact() == new BigDecimal(v5TokenHolder.getRawBalance()).longValueExact();
        } else return this.equals(tokenHolder);
    }

    public V4TokenBalance(String contractAddress, BigDecimal balance, String holderAddress, long blockNumber) {
        this.contractAddress = contractAddress;
        this.balance = balance;
        this.holderAddress = holderAddress;
        this.blockNumber = blockNumber;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public BigDecimal getBalance() {
        return balance;
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
        if (!(o instanceof V4TokenBalance)) return false;
        V4TokenBalance that = (V4TokenBalance) o;
        return getBlockNumber() == that.getBlockNumber() &&
                Objects.equals(getContractAddress(), that.getContractAddress()) &&
                getBalance().compareTo(that.getBalance()) == 0 &&
                Objects.equals(getHolderAddress(), that.getHolderAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContractAddress(), getBalance(), getHolderAddress(), getBlockNumber());
    }

    public static class TokenBalanceBuilder {


        private String contractAddress;
        private BigDecimal balance;
        private String holderAddress;
        private long blockNumber;


        public TokenBalanceBuilder() {

        }

        public TokenBalanceBuilder setBalance(BigDecimal balance) {
            this.balance = balance;
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


        public V4TokenBalance build() {
            return new V4TokenBalance(contractAddress, balance, holderAddress, blockNumber);
        }

        public TokenBalanceBuilder setBlockNumber(long blockNumber) {
            this.blockNumber = blockNumber;
            return this;
        }
    }
}
