package com.aion.dashboard.etl.domainobjects.base;

import java.util.Objects;

public abstract class AbstractTokenHolder {
    public String contractAddress;
    public String holderAddress;
    public long blockNumber;

    public boolean compare(AbstractTokenHolder tokenHolder) {
        return blockNumber == tokenHolder.blockNumber &&
                Objects.equals(contractAddress, tokenHolder.contractAddress) &&
                Objects.equals(holderAddress, tokenHolder.holderAddress);
    }
}
