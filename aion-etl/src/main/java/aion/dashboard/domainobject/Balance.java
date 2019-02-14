package aion.dashboard.domainobject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public class Balance {

    private Long lastBlockNumber;
    private Long transactionId;
    private BigDecimal balance;
    private String address;
    private int contract=0;
    private BigInteger nonce;

    private  Balance(Long lastBlockNumber,BigDecimal balance,String address,int contract, BigInteger nonce,Long transactionId){
        this.lastBlockNumber=lastBlockNumber;
        this.balance=balance;
        this.address=address;
        this.contract=contract;
        this.nonce = nonce;
        this.transactionId=transactionId;
    }



    private Balance(){}

    public Balance setTransactionId(Long transactionId) {
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
        Balance balance1 = (Balance) o;
        return  Objects.equals(lastBlockNumber, balance1.lastBlockNumber) &&
                Objects.equals(balance, balance1.balance) &&
                Objects.equals(address, balance1.address)&&
                Objects.equals(nonce, balance1.nonce);
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
    public static class BalanceBuilder

    {
        private String address;
        private BigInteger nonce;
        private BigDecimal balance;
        private Long lastBlockNumber;
        private int contract=0;
        private Long transactionId;

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

        public Balance build(){
            return new Balance(lastBlockNumber,balance,address,contract, nonce,transactionId);
        }
    }
}
