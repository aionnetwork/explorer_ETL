package aion.dashboard.domainobject;

import java.math.BigDecimal;
import java.util.Objects;

public class TokenBalance {


    private String contractAddress;
    private BigDecimal balance;
    private String holderAddress;
    private long blockNumber;


    public TokenBalance(String contractAddress, BigDecimal balance, String holderAddress, long blockNumber) {
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
        if (!(o instanceof TokenBalance)) return false;
        TokenBalance that = (TokenBalance) o;
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


        public TokenBalance build() {
            return new TokenBalance(contractAddress, balance, holderAddress, blockNumber);
        }

        public TokenBalanceBuilder setBlockNumber(long blockNumber) {
            this.blockNumber = blockNumber;
            return this;
        }
    }
}
