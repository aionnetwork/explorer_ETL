package aion.dashboard.domainobject;

import java.math.BigInteger;
import java.util.Objects;

public class Block {

    private long blockNumber;
    private String blockHash;
    private String minerAddress;
    private String parentHash;
    private String receiptTxRoot;
    private String stateRoot;
    private String txTrieRoot;
    private String extraData;
    private String nonce;
    private String bloom;
    private String solution;
    private String difficulty;
    private String totalDifficulty;
    private long nrgConsumed;
    private long nrgLimit;
    private long size;
    private long blockTimestamp;
    private long numTransactions;
    private long blockTime;
    private String transactionList;
    private BigInteger nrgReward;
    private BigInteger transactionId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block block = (Block) o;
        return blockNumber == block.blockNumber &&
                nrgConsumed == block.nrgConsumed &&
                nrgLimit == block.nrgLimit &&
                size == block.size &&
                blockTimestamp == block.blockTimestamp &&
                numTransactions == block.numTransactions &&
                blockTime == block.blockTime &&
                Objects.equals(blockHash, block.blockHash) &&
                Objects.equals(minerAddress, block.minerAddress) &&
                Objects.equals(parentHash, block.parentHash) &&
                Objects.equals(receiptTxRoot, block.receiptTxRoot) &&
                Objects.equals(stateRoot, block.stateRoot) &&
                Objects.equals(txTrieRoot, block.txTrieRoot) &&
                Objects.equals(extraData, block.extraData) &&
                Objects.equals(nonce, block.nonce) &&
                Objects.equals(bloom, block.bloom) &&
                Objects.equals(solution, block.solution) &&
                Objects.equals(difficulty, block.difficulty) &&
                Objects.equals(totalDifficulty, block.totalDifficulty) &&
                Objects.equals(transactionId, block.transactionId) &&
                Objects.equals(transactionList, block.transactionList) &&
                Objects.equals(nrgReward, block.nrgReward);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockNumber, blockHash, minerAddress, parentHash, receiptTxRoot, stateRoot, txTrieRoot, extraData, nonce, bloom, solution, difficulty, totalDifficulty, nrgConsumed, nrgLimit, size, blockTimestamp, numTransactions, blockTime, transactionId, transactionList, nrgReward);
    }

    public BigInteger getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(BigInteger transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionList() {
        return transactionList;
    }

    public void setTransactionList(String transactionList) {
        this.transactionList = transactionList;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public String getMinerAddress() {
        return minerAddress;
    }

    public void setMinerAddress(String minerAddress) {
        this.minerAddress = minerAddress;
    }

    public String getParentHash() {
        return parentHash;
    }

    public void setParentHash(String parentHash) {
        this.parentHash = parentHash;
    }

    public String getReceiptTxRoot() {
        return receiptTxRoot;
    }

    public void setReceiptTxRoot(String receiptTxRoot) {
        this.receiptTxRoot = receiptTxRoot;
    }

    public String getStateRoot() {
        return stateRoot;
    }

    public void setStateRoot(String stateRoot) {
        this.stateRoot = stateRoot;
    }

    public String getTxTrieRoot() {
        return txTrieRoot;
    }

    public void setTxTrieRoot(String txTrieRoot) {
        this.txTrieRoot = txTrieRoot;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getBloom() {
        return bloom;
    }

    public void setBloom(String bloom) {
        this.bloom = bloom;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getTotalDifficulty() {
        return totalDifficulty;
    }

    public void setTotalDifficulty(String totalDifficulty) {
        this.totalDifficulty = totalDifficulty;
    }

    public long getNrgConsumed() {
        return nrgConsumed;
    }

    public void setNrgConsumed(long nrgConsumed) {
        this.nrgConsumed = nrgConsumed;
    }

    public long getNrgLimit() {
        return nrgLimit;
    }

    public void setNrgLimit(long nrgLimit) {
        this.nrgLimit = nrgLimit;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getBlockTimestamp() {
        return blockTimestamp;
    }

    public void setBlockTimestamp(long blockTimestamp) {
        this.blockTimestamp = blockTimestamp;
    }

    public long getNumTransactions() {
        return numTransactions;
    }

    public void setNumTransactions(long numTransactions) {
        this.numTransactions = numTransactions;
    }

    public long getBlockTime() {
        return blockTime;
    }

    public void setBlockTime(long blockTime) {
        this.blockTime = blockTime;
    }

    public BigInteger getNrgReward() {
        return nrgReward;
    }

    public void setNrgReward(BigInteger nrgReward) {
        this.nrgReward = nrgReward;
    }

    @Override
    public String toString() {
        return "Block{" +
                "blockNumber=" + blockNumber +
                ", blockHash='" + blockHash + '\'' +
                ", minerAddress='" + minerAddress + '\'' +
                ", parentHash='" + parentHash + '\'' +
                ", receiptTxRoot='" + receiptTxRoot + '\'' +
                ", stateRoot='" + stateRoot + '\'' +
                ", txTrieRoot='" + txTrieRoot + '\'' +
                ", extraData='" + extraData + '\'' +
                ", nonce='" + nonce + '\'' +
                ", bloom='" + bloom + '\'' +
                ", solution='" + solution + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", totalDifficulty='" + totalDifficulty + '\'' +
                ", nrgConsumed=" + nrgConsumed +
                ", nrgLimit=" + nrgLimit +
                ", size=" + size +
                ", blockTimestamp=" + blockTimestamp +
                ", numTransactions=" + numTransactions +
                ", blockTime=" + blockTime +
                ", transactionId=" + transactionId +
                ", transactionList='" + transactionList + '\'' +
                ", nrgReward=" + nrgReward +
                '}';
    }

    public static class BlockBuilder {
        long blockNumber;
        String blockHash;
        String minerAddress;
        String parentHash;
        String receiptTxRoot;
        String stateRoot;
        String txTrieRoot;
        String extraData;
        String nonce;
        String bloom;
        String solution;
        String difficulty;
        String totalDifficulty;
        long nrgConsumed;
        long nrgLimit;
        long size;
        long blockTimestamp;
        long numTransactions;
        long blockTime;
        String transactionList;
        BigInteger transactionId;
        BigInteger nrgReward;




        public BlockBuilder blockNumber(long blockNumber) {
            this.blockNumber = blockNumber;
            return this;
        }

        public BlockBuilder blockHash(String blockHash) {
            this.blockHash = blockHash;
            return this;
        }

        public BlockBuilder minerAddress(String minerAddress) {
            this.minerAddress = minerAddress;
            return this;
        }

        public BlockBuilder parentHash(String parentHash) {
            this.parentHash = parentHash;
            return this;
        }

        public BlockBuilder receiptTxRoot(String receiptTxRoot) {
            this.receiptTxRoot = receiptTxRoot;
            return this;
        }

        public BlockBuilder stateRoot(String stateRoot) {
            this.stateRoot = stateRoot;
            return this;
        }

        public BlockBuilder txTrieRoot(String txTrieRoot) {
            this.txTrieRoot = txTrieRoot;
            return this;
        }

        public BlockBuilder extraData(String extraData) {
            this.extraData = extraData;
            return this;
        }

        public BlockBuilder nonce(String nonce) {
            this.nonce = nonce;
            return this;
        }

        public BlockBuilder bloom(String bloom) {
            this.bloom = bloom;
            return this;
        }

        public BlockBuilder solution(String solution) {
            this.solution = solution;
            return this;
        }

        public BlockBuilder difficulty(String difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public BlockBuilder totalDifficulty(String totalDifficulty) {
            this.totalDifficulty = totalDifficulty;
            return this;
        }

        public BlockBuilder nrgConsumed(long nrgConsumed) {
            this.nrgConsumed = nrgConsumed;
            return this;
        }

        public BlockBuilder nrgLimit(long nrgLimit) {
            this.nrgLimit = nrgLimit;
            return this;
        }

        public BlockBuilder size(long size) {
            this.size = size;
            return this;
        }

        public BlockBuilder blockTimestamp(long blockTimestamp) {
            this.blockTimestamp = blockTimestamp;
            return this;
        }

        public BlockBuilder numTransactions(long numTransactions) {
            this.numTransactions = numTransactions;
            return this;
        }

        public BlockBuilder blockTime(long blockTime) {
            this.blockTime = blockTime;
            return this;
        }

        public BlockBuilder  transactionId(BigInteger transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public BlockBuilder transactionList(String transactionList) {
            this.transactionList = transactionList;
            return this;
        }

        public BlockBuilder nrgReward(BigInteger nrgReward) {
            this.nrgReward = nrgReward;
            return this;
        }

        public Block build() {
            Block block = new Block();

            block.blockNumber = blockNumber;
            block.blockHash = blockHash;
            block.minerAddress = minerAddress;
            block.parentHash = parentHash;
            block.receiptTxRoot = receiptTxRoot;
            block.stateRoot = stateRoot;
            block.txTrieRoot = txTrieRoot;
            block.extraData = extraData;
            block.nonce = nonce;
            block.bloom = bloom;
            block.solution = solution;
            block.difficulty = difficulty;
            block.totalDifficulty = totalDifficulty;
            block.nrgConsumed = nrgConsumed;
            block.nrgLimit = nrgLimit;
            block.size = size;
            block.blockTimestamp = blockTimestamp;
            block.numTransactions = numTransactions;
            block.blockTime = blockTime;
            block.transactionList = transactionList;
            block.transactionId=transactionId;
            block.nrgReward = nrgReward;

            return block;
        }
    }
}
