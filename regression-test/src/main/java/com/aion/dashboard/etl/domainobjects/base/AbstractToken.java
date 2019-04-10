package com.aion.dashboard.etl.domainobjects.base;

import java.math.BigInteger;
import java.util.Objects;

public abstract class AbstractToken {
    public BigInteger totalSupply;
    public String contractAddress;
    public String transactionHash;
    public String creatorAddress;
    public String tokenName;
    public String symbol;

    public boolean compare(AbstractToken token) {
        return totalSupply.equals(token.totalSupply) &&
                Objects.equals(contractAddress, token.contractAddress) &&
                Objects.equals(transactionHash, token.transactionHash) &&
                Objects.equals(creatorAddress, token.creatorAddress) &&
                Objects.equals(tokenName, token.tokenName) &&
                Objects.equals(symbol, token.symbol);
    }
}
