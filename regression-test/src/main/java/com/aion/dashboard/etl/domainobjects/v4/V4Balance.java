package com.aion.dashboard.etl.domainobjects.v4;

import com.aion.dashboard.etl.domainobjects.base.AbstractAccount;
import com.aion.dashboard.etl.domainobjects.v5.V5Account;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public class V4Balance extends AbstractAccount {

    private BigInteger nonce;
    private Long transactionId;

    private V4Balance(Long lastBlockNumber, BigDecimal balance, String address, int contract, BigInteger nonce, Long transactionId){
        this.lastBlockNumber=lastBlockNumber;
        this.balance=balance;
        this.address=address;
        this.contract=contract;
        this.nonce = nonce;
        this.transactionId=transactionId;
    }



    private V4Balance(){}

    public V4Balance setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
        return this;
    }
    public int getContract() {
        return contract;
    }


    public Long getLastBlockNumber() {
        return lastBlockNumber;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public BigDecimal getBalance() {
        return balance;
    }


    public String getAddress () {
        return address;
    }

    public BigInteger getNonce () {
        return nonce;
    }

    public void setContract(int contract){
        this.contract = contract;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        V4Balance v4Balance1 = (V4Balance) o;
        return  Objects.equals(lastBlockNumber, v4Balance1.lastBlockNumber) &&
                Objects.equals(balance, v4Balance1.balance) &&
                Objects.equals(address, v4Balance1.address)&&
                Objects.equals(nonce, v4Balance1.nonce);
    }

    @Override
    public int hashCode() {

        return Objects.hash(address);
    }


    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    public void setNonce(BigInteger nonce){this.nonce = nonce;}
    public void setLastBlockNumber(long blockNumber){
        lastBlockNumber = blockNumber;
    }

    public static class BalanceBuilder {
        private String address;
        private BigInteger nonce;
        private BigDecimal balance;
        private Long lastBlockNumber;
        private int contract=0;
        private Long transactionId;

        private String transactionHash;

        public BalanceBuilder balance (BigDecimal balance){
            this.balance = balance;
            return this;
        }

        public BalanceBuilder lastBlockNumber(Long lastBlockNumber) {
            this.lastBlockNumber = lastBlockNumber;
            return this;
        }


        public BalanceBuilder address (String address){
            this.address = address;
            return this;
        }

        public BalanceBuilder contract(int contract) {
            this.contract = contract;
            return this;
        }

        public BalanceBuilder nonce(BigInteger nonce){
            this.nonce = nonce;
            return this;
        }


        public BalanceBuilder transactionId(Long transactionId){
            this.transactionId= transactionId;
            return this;
        }

        public BalanceBuilder(){}
        public V4Balance build(){
            return new V4Balance(lastBlockNumber,balance,address,contract, nonce,transactionId);
        }

        public BalanceBuilder setTransactionHash(String transactionHash) {
            this.transactionHash = transactionHash;
            return this;
        }
    }
}
