package aion.dashboard.domainobject;

import org.aion.api.type.AccountDetails;
import org.aion.api.type.BlockDetails;
import org.aion.api.type.TxDetails;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

import static aion.dashboard.util.Utils.approximate;

public class Account {

    private Long lastBlockNumber;
    private String transactionHash;
    private BigDecimal balance;
    private String address;
    private int contract=0;
    private String nonce;
    private double approxBalance;

    private Account(Long lastBlockNumber, BigDecimal balance, String address, int contract, String nonce, String transactionHash){
        this.lastBlockNumber=lastBlockNumber;
        this.balance=balance;
        this.address=address;
        this.contract=contract;
        this.nonce = nonce;
        this.transactionHash = transactionHash;
        approxBalance= balance == null ? 0d : approximate(balance, 18);
    }



    private Account(){}

    public Account setTransactionHash(String transactionHash) {
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
        Account account1 = (Account) o;
        return  Objects.equals(lastBlockNumber, account1.lastBlockNumber) &&
                Objects.equals(balance, account1.balance) &&
                Objects.equals(address, account1.address)&&
                Objects.equals(nonce, account1.nonce);
    }

    @Override
    public int hashCode() {

        return Objects.hash(address);
    }

    public void setBalance(BigDecimal balance) {

        this.balance = balance;
        approxBalance= balance == null ? 0d : approximate(balance, 18);

    }
    public void setNonce(String nonce){this.nonce = nonce;}

    public void setLastBlockNumber(long blockNumber){
        lastBlockNumber = blockNumber;
    }

    public double getApproxBalance() {
        return approxBalance;
    }


    private static final ThreadLocal<AccountBuilder> accountBuilder = ThreadLocal.withInitial(AccountBuilder::new);

    public static Account from(String address, BlockDetails blockDetails, TxDetails tx, BigDecimal balance, BigInteger nonce){
        return accountBuilder.get()
                .address(address.replace("0x",""))
                .balance((balance))
                .nonce(nonce.toString(16))
                .lastBlockNumber(blockDetails.getNumber())
                .contract(tx==null || tx.getContract().isEmptyAddress()? 0:1)
                .transactionHash(tx==null?"":tx.getTxHash().toString())
                .build();
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

        public Account build(){
            return new Account(lastBlockNumber,balance,address,contract, nonce, transactionHash);
        }
    }
}