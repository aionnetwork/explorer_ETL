package com.aion.dashboard.etl.domainobjects.base;

import java.util.Objects;

public abstract class AbstractTokenTransfer {
    public String operator;
    public String toAddress;
    public String fromAddress;
    public String contractAddress;
    public long transferTimestamp;

    public boolean compare(AbstractTokenTransfer tokenTransfer) {
        return transferTimestamp == tokenTransfer.transferTimestamp &&
                Objects.equals(operator, tokenTransfer.operator) &&
                Objects.equals(toAddress, tokenTransfer.toAddress) &&
                Objects.equals(fromAddress, tokenTransfer.fromAddress) &&
                Objects.equals(contractAddress, tokenTransfer.contractAddress);
    }
}
