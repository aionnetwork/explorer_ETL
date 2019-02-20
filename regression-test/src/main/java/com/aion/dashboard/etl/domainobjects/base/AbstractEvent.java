package com.aion.dashboard.etl.domainobjects.base;

import java.util.Objects;

public abstract class AbstractEvent {
    public String name;
    public String parameterList;
    public String inputList;
    public String contractAddr;
    public long blockNumber;
    public long timestamp;

    public boolean compare(AbstractEvent event) {
        return blockNumber == event.blockNumber &&
                timestamp == event.timestamp &&
                Objects.equals(contractAddr, event.contractAddr) &&
                Objects.equals(name, event.name) &&
                Objects.equals(parameterList, event.parameterList) &&
                Objects.equals(inputList, event.inputList);
    }
}
