package com.aion.dashboard.etl.domainobjects.v5;

import com.aion.dashboard.etl.domainobjects.base.AbstractAccount;
import com.aion.dashboard.etl.domainobjects.v4.V4Balance;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public class V5Account extends AbstractAccount {
    private String transactionHash;
    private String nonce;

    private V5Account(Long lastBlockNumber, BigDecimal balance, String address, int contract, String nonce, String transactionHash){
        this.lastBlockNumber=lastBlockNumber;
        this.balance=balance;
        this.address=address;
        this.contract=contract;
        this.nonce = nonce;
        this.transactionHash = transactionHash;
    }

    private V5Account(){}

    public V5Account setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
        return this;
    }
    public int getContract() {
        return contract;
    }


    public Long getLastBlockNumber() {
        return lastBlockNumber;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public BigDecimal getBalance() {
        return balance;
    }


    public String getAddress () {
        return address;
    }

    public String getNonce () {
        return nonce;
    }

    public void setContract(int contract){
        this.contract = contract;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        V5Account v5Account1 = (V5Account) o;
        return  Objects.equals(lastBlockNumber, v5Account1.lastBlockNumber) &&
                Objects.equals(balance, v5Account1.balance) &&
                Objects.equals(address, v5Account1.address)&&
                Objects.equals(nonce, v5Account1.nonce);
    }

    @Override
    public int hashCode() {

        return Objects.hash(address);
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
    public void setNonce(String nonce){this.nonce = nonce;}

    public void setLastBlockNumber(long blockNumber){
        lastBlockNumber = blockNumber;
    }
    public static class AccountBuilder

    {
        private String address;
        private String nonce;
        private BigDecimal balance;
        private Long lastBlockNumber;
        private int contract=0;
        private String transactionHash;

        public AccountBuilder balance(BigDecimal balance){
            this.balance = balance;
            return this;
        }

        public AccountBuilder lastBlockNumber(Long lastBlockNumber) {
            this.lastBlockNumber = lastBlockNumber;
            return this;
        }


        public AccountBuilder address(String address){
            this.address = address;
            return this;
        }

        public AccountBuilder contract(int contract) {
            this.contract = contract;
            return this;
        }

        public AccountBuilder nonce(String nonce){
            this.nonce = nonce;
            return this;
        }


        public AccountBuilder transactionHash(String transactionHash){
            this.transactionHash = transactionHash;
            return this;
        }

        public AccountBuilder(){}
        public V5Account build(){
            return new V5Account(lastBlockNumber,balance,address,contract, nonce, transactionHash);
        }
    }
}
