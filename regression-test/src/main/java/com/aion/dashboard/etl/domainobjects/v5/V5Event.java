package com.aion.dashboard.etl.domainobjects.v5;

import com.aion.dashboard.etl.domainobjects.base.AbstractEvent;

import java.util.Objects;

public class V5Event extends AbstractEvent {
    private String transactionHash;// the hash of the transaction on which the event was fired

    private V5Event(String name, String parameterList, String inputList, String transactionHash, long blockNumber, String contractAddr, long timestamp) {
        this.name = name;
        this.parameterList = parameterList;
        this.inputList = inputList;
        this.transactionHash = transactionHash;
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

    public String getTransactionHash() {
        return transactionHash;
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
        if (!(o instanceof V5Event)) return false;
        V5Event v5Event = (V5Event) o;
        return getBlockNumber() == v5Event.getBlockNumber() &&
                getTimestamp() == v5Event.getTimestamp() &&
                Objects.equals(getName(), v5Event.getName()) &&
                Objects.equals(getParameterList(), v5Event.getParameterList()) &&
                Objects.equals(getInputList(), v5Event.getInputList()) &&
                Objects.equals(getTransactionHash(), v5Event.getTransactionHash()) &&
                Objects.equals(getContractAddr(), v5Event.getContractAddr());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getParameterList(), getInputList(), getTransactionHash(), getBlockNumber(), getContractAddr(), getTimestamp());
    }

    public static class EventBuilder {
        private String name;
        private String parameterList;// the parameter list stored as a json object in the format ["to: Address, from:Address, value: uint"]
        private String inputList;// The list of input values
        private String transactionHash;// the hash of the transaction on which the event was fired
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

        public EventBuilder setTransactionHash(String transactionHash) {
            this.transactionHash = transactionHash;
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

        public V5Event build() {
            return new V5Event(name, parameterList, inputList, transactionHash, blockNumber, contractAddr, timestamp);
        }


        public EventBuilder setTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
    }
}

