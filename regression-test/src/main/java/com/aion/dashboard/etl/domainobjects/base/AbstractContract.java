package com.aion.dashboard.etl.domainobjects.base;

import java.util.Objects;

public abstract class AbstractContract {
    public String contractAddr;
    public String contractName;
    public String contractCreatorAddr;
    public String contractTxHash;
    public long blockNumber;
    public long timestamp;

    public boolean compare(AbstractContract contract) {
        return blockNumber == contract.blockNumber &&
                timestamp == contract.timestamp &&
                Objects.equals(contractAddr, contract.contractAddr) &&
                Objects.equals(contractName, contract.contractName) &&
                Objects.equals(contractCreatorAddr, contract.contractCreatorAddr) &&
                Objects.equals(contractTxHash, contract.contractTxHash);
    }
}
