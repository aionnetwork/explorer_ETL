package aion.dashboard.domainobjects;

public class BlockMap {

    String blockHash;
    Long blockNumber;


    public BlockMap(String blockHash, Long blockNumber) {
        this.blockHash = blockHash;
        this.blockNumber = blockNumber;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public Long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Long blockNumber) {
        this.blockNumber = blockNumber;
    }

}
