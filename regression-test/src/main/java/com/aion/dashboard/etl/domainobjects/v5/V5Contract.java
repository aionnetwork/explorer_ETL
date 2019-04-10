package com.aion.dashboard.etl.domainobjects.v5;

import com.aion.dashboard.etl.domainobjects.base.AbstractContract;

import java.time.ZonedDateTime;
import java.util.Objects;
import static com.aion.dashboard.etl.util.Utils.getZDT;

public class V5Contract extends AbstractContract {

    private int blockYear;
    private int blockMonth;
    private int blockDay;


    private V5Contract(String contractAddr, String contractName, String contractCreatorAddr, String contractTxHash, long blockNumber, long timestamp) {
        this.contractAddr = contractAddr;
        this.contractName = contractName;
        this.contractCreatorAddr = contractCreatorAddr;
        this.contractTxHash = contractTxHash;
        this.blockNumber = blockNumber;
        this.timestamp = timestamp;
        ZonedDateTime zdt = getZDT(timestamp);
        blockYear  = zdt.getYear();
        blockMonth = zdt.getMonthValue();
        blockDay = zdt.getYear();
    }

    public String getContractAddr() {
        return contractAddr;
    }

    public String getContractName() {
        return contractName;
    }

    public String getContractCreatorAddr() {
        return contractCreatorAddr;
    }

    public String getContractTxHash() {
        return contractTxHash;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public long getTimestamp() {
        return timestamp;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof V5Contract)) return false;
        V5Contract v5Contract = (V5Contract) o;
        return Objects.equals(getContractAddr(), v5Contract.getContractAddr());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContractAddr());
    }

    public int getBlockYear() {
        return blockYear;
    }

    public int getBlockMonth() {
        return blockMonth;
    }

    public int getBlockDay() {
        return blockDay;
    }

    public static class ContractBuilder {

        private String contractAddr;//The address at which the contract is deployed on the block chain
        private String contractName;// the name of the contract - this should be supplied by the owner I can't really pull this off the blockchain
        private String contractCreatorAddr;// the address that deployed this contract
        private String contractTxHash; // the transaction in which the contract was deployed
        private long blockNumber;//The block in which the contract was deployed
        private long timestamp;

        public ContractBuilder() {
        }


        public ContractBuilder setContractAddr(String contractAddr) {
            this.contractAddr = contractAddr;
            return this;
        }

        public ContractBuilder setContractName(String contractName) {
            this.contractName = contractName;
            return this;
        }

        public ContractBuilder setContractCreatorAddr(String contractCreatorAddr) {
            this.contractCreatorAddr = contractCreatorAddr;
            return this;
        }

        public ContractBuilder setContractTxHash(String contractTxHash) {
            this.contractTxHash = contractTxHash;
            return this;
        }

        public ContractBuilder setBlockNumber(long blockNumber) {
            this.blockNumber = blockNumber;
            return this;
        }

        public V5Contract build() {
            return new V5Contract(contractAddr, contractName, contractCreatorAddr, contractTxHash, blockNumber, timestamp);
        }

        public ContractBuilder setTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
    }
}


