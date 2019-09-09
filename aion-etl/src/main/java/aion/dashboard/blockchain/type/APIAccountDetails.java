package aion.dashboard.blockchain.type;

import aion.dashboard.util.Utils;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigInteger;


public class APIAccountDetails {
    private String address;
    private BigInteger balance;
    private long blockNumber;
    private BigInteger nonce;
    private BigInteger stakedBalance;

    @JsonCreator
    public APIAccountDetails(@JsonProperty("address") String address,
                             @JsonProperty("balance") String balance,
                             @JsonProperty("blockNumber") long blockNumber,
                             @JsonProperty("nonce") String nonce){
        this.address = address;
        this.balance = Utils.bigIntegerFromHex(balance);
        this.blockNumber = blockNumber;
        this.nonce = Utils.bigIntegerFromHex(nonce);
    }

    @JsonIgnore
    public void setStakedBalance(BigInteger stakedBalance) {
        this.stakedBalance = stakedBalance;
    }

    @JsonIgnore
    public BigInteger getStakedBalance() {
        return stakedBalance;
    }

    @JsonIgnore
    public String getAddress() {
        return address;
    }

    @JsonIgnore
    public BigInteger getBalance() {
        return balance;
    }

    @JsonIgnore
    public long getBlockNumber() {
        return blockNumber;
    }

    @JsonIgnore
    public BigInteger getNonce() {
        return nonce;
    }
}
