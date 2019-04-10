package com.aion.dashboard.etl.domainobjects.v4;

import com.aion.dashboard.etl.domainobjects.base.AbstractEvent;

import java.util.Objects;

public class V4Event extends AbstractEvent {
    private long transactionId;// the hash of the transaction on which the event was fired

    private V4Event(String name, String parameterList, String inputList, long transactionId, long blockNumber, String contractAddr, long timestamp) {
        this.name = name;
        this.parameterList = parameterList;
        this.inputList = inputList;
        this.transactionId = transactionId;
        this.blockNumber = blockNumber;
        this.contractAddr = contractAddr;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public String getParameterList() {
        return parameterList;
    }

    public String getInputList() {
        return inputList;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public String getContractAddr() {
        return contractAddr;
    }

    public long getTimestamp() {
        return timestamp;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof V4Event)) return false;
        V4Event v4Event = (V4Event) o;
        return getBlockNumber() == v4Event.getBlockNumber() &&
                getTimestamp() == v4Event.getTimestamp() &&
                Objects.equals(getName(), v4Event.getName()) &&
                Objects.equals(getParameterList(), v4Event.getParameterList()) &&
                Objects.equals(getInputList(), v4Event.getInputList()) &&
                Objects.equals(getTransactionId(), v4Event.getTransactionId()) &&
                Objects.equals(getContractAddr(), v4Event.getContractAddr());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getParameterList(), getInputList(), getTransactionId(), getBlockNumber(), getContractAddr(), getTimestamp());
    }

    public static class EventBuilder {
        private String name;
        private String parameterList;// the parameter list stored as a json object in the format ["to: Address, from:Address, value: uint"]
        private String inputList;// The list of input values
        private long transactionID;// the hash of the transaction on which the event was fired
        private long blockNumber; // the block in which the event was fired
        private String contractAddr; // the address of the contract which fired the event
        private long timestamp;

        public EventBuilder() {
        }

        public EventBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public EventBuilder setParameterList(String parameterList) {
            this.parameterList = parameterList;
            return this;
        }

        public EventBuilder setInputList(String inputList) {
            this.inputList = inputList;
            return this;
        }

        public EventBuilder setTransactionID(long transactionID) {
            this.transactionID = transactionID;
            return this;
        }

        public EventBuilder setBlockNumber(long blockNumber) {
            this.blockNumber = blockNumber;
            return this;
        }

        public EventBuilder setContractAddr(String contractAddr) {
            this.contractAddr = contractAddr;
            return this;
        }

        public V4Event build() {
            return new V4Event(name, parameterList, inputList, transactionID, blockNumber, contractAddr, timestamp);
        }


        public EventBuilder setTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
    }
}
