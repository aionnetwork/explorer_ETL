package aion.dashboard.domainobject;

import aion.dashboard.blockchain.ContractType;
import org.aion.api.type.BlockDetails;
import org.aion.api.type.TxDetails;

import java.util.Objects;

import static aion.dashboard.util.Utils.getZDT;

public class Contract {


    private String contractAddr;//The address at which the contract is deployed on the block chain
    private String contractName;// the name of the contract - this should be supplied by the owner I can't really pull this off the blockchain
    private String contractCreatorAddr;// the address that deployed this contract
    private String contractTxHash; // the transaction in which the contract was deployed
    private long blockNumber;//The block in which the contract was deployed
    private long timestamp;
    private int blockYear;
    private int blockMonth;
    private int blockDay;
    private ContractType type;


    private Contract(String contractAddr, String contractName, String contractCreatorAddr, String contractTxHash, long blockNumber, long timestamp, ContractType type) {
        this.contractAddr = contractAddr;
        this.contractName = contractName;
        this.contractCreatorAddr = contractCreatorAddr;
        this.contractTxHash = contractTxHash;
        this.blockNumber = blockNumber;
        this.timestamp = timestamp;
        this.type = type;
        var zdt = getZDT(timestamp);
        blockYear  = zdt.getYear();
        blockMonth = zdt.getMonthValue();
        blockDay = zdt.getDayOfMonth();
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
        if (!(o instanceof Contract)) return false;
        Contract contract = (Contract) o;
        return Objects.equals(getContractAddr(), contract.getContractAddr());
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

    public String getType() {
        return type.type;
    }

    public ContractType getContractType(){
        return type;
    }
    private static final ThreadLocal<ContractBuilder> builder = ThreadLocal.withInitial(ContractBuilder::new);
    public static Contract from(BlockDetails b, TxDetails tx){
        return builder.get()
                .setType(tx.getType())
                .setBlockNumber(b.getNumber())
                .setTimestamp(b.getTimestamp())
                .setContractCreatorAddr(tx.getFrom().toString())
                .setContractAddr(tx.getContract().toString())
                .setContractTxHash(tx.getTxHash().toString())
                .setContractName("").build();
    }

    public static class ContractBuilder {

        private String contractAddr;//The address at which the contract is deployed on the block chain
        private String contractName;// the name of the contract - this should be supplied by the owner I can't really pull this off the blockchain
        private String contractCreatorAddr;// the address that deployed this contract
        private String contractTxHash; // the transaction in which the contract was deployed
        private long blockNumber;//The block in which the contract was deployed
        private long timestamp;
        private ContractType type;


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

        public Contract build() {
            return new Contract(contractAddr, contractName, contractCreatorAddr, contractTxHash, blockNumber, timestamp, type);
        }

        public ContractBuilder setTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ContractBuilder setType(byte type) {
            this.type = ContractType.fromByte(type);
            return this;
        }

        public ContractBuilder setType(String type){
            this.type = ContractType.fromType(type);
            return this;
        }
    }
}
