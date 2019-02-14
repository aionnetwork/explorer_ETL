package aion.dashboard.domainobjects;

public class Block {

    public Long blockNumber;
    public String blockHash;
    public String minerAddress;
    public String parentHash;
    public String receiptTxRoot;
    public String stateRoot;
    public String txTrieRoot;
    public String extraData;
    public String nonce;
    public String bloom;
    public String solution;
    public String difficulty;
    public String totalDifficulty;
    public Long nrgConsumed;
    public Long nrgLimit;
    public Long size;
    public Long blockTimestamp;
    public Long numTransactions;
    public Long blockTime;
    public String transactionList;
    public String nrgReward;


    public String getTransactionList() {
        return transactionList;
    }

    public void setTransactionList(String transactionList) {
        this.transactionList = transactionList;
    }

    public Long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Long blockNumber) {
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

    public Long getNrgConsumed() {
        return nrgConsumed;
    }

    public void setNrgConsumed(Long nrgConsumed) {
        this.nrgConsumed = nrgConsumed;
    }

    public Long getNrgLimit() {
        return nrgLimit;
    }

    public void setNrgLimit(Long nrgLimit) {
        this.nrgLimit = nrgLimit;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getBlockTimestamp() {
        return blockTimestamp;
    }

    public void setBlockTimestamp(Long blockTimestamp) {
        this.blockTimestamp = blockTimestamp;
    }

    public Long getNumTransactions() {
        return numTransactions;
    }

    public void setNumTransactions(Long numTransactions) {
        this.numTransactions = numTransactions;
    }

    public Long getBlockTime() {
        return blockTime;
    }

    public void setBlockTime(Long blockTime) {
        this.blockTime = blockTime;
    }

    public String getNrgReward() {
        return nrgReward;
    }

    public void setNrgReward(String nrgReward) {
        this.nrgReward = nrgReward;
    }


    public static class BlockBuilder {
        Long blockNumber;
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
        Long nrgConsumed;
        Long nrgLimit;
        Long size;
        Long blockTimestamp;
        Long numTransactions;
        Long blockTime;
        String transactionList;
        String nrgReward;

        public BlockBuilder() {
        }

        public BlockBuilder setBlockNumber(Long blockNumber) {
            this.blockNumber = blockNumber;
            return this;
        }

        public BlockBuilder setBlockHash(String blockHash) {
            this.blockHash = blockHash;
            return this;
        }

        public BlockBuilder setMinerAddress(String minerAddress) {
            this.minerAddress = minerAddress;
            return this;
        }

        public BlockBuilder setParentHash(String parentHash) {
            this.parentHash = parentHash;
            return this;
        }

        public BlockBuilder setReceiptTxRoot(String receiptTxRoot) {
            this.receiptTxRoot = receiptTxRoot;
            return this;
        }

        public BlockBuilder setStateRoot(String stateRoot) {
            this.stateRoot = stateRoot;
            return this;
        }

        public BlockBuilder setTxTrieRoot(String txTrieRoot) {
            this.txTrieRoot = txTrieRoot;
            return this;
        }

        public BlockBuilder setExtraData(String extraData) {
            this.extraData = extraData;
            return this;
        }

        public BlockBuilder setNonce(String nonce) {
            this.nonce = nonce;
            return this;
        }

        public BlockBuilder setBloom(String bloom) {
            this.bloom = bloom;
            return this;
        }

        public BlockBuilder setSolution(String solution) {
            this.solution = solution;
            return this;
        }

        public BlockBuilder setDifficulty(String difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public BlockBuilder setTotalDifficulty(String totalDifficulty) {
            this.totalDifficulty = totalDifficulty;
            return this;
        }

        public BlockBuilder setNrgConsumed(Long nrgConsumed) {
            this.nrgConsumed = nrgConsumed;
            return this;
        }

        public BlockBuilder setNrgLimit(Long nrgLimit) {
            this.nrgLimit = nrgLimit;
            return this;
        }

        public BlockBuilder setSize(Long size) {
            this.size = size;
            return this;
        }

        public BlockBuilder setBlockTimestamp(Long blockTimestamp) {
            this.blockTimestamp = blockTimestamp;
            return this;
        }

        public BlockBuilder setNumTransactions(Long numTransactions) {
            this.numTransactions = numTransactions;
            return this;
        }

        public BlockBuilder setBlockTime(Long blockTime) {
            this.blockTime = blockTime;
            return this;
        }

        public BlockBuilder setTransactionList(String transactionList) {
            this.transactionList = transactionList;
            return this;
        }

        public BlockBuilder setNrgReward(String nrgReward) {
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
            block.nrgReward = nrgReward;

            return block;
        }
    }
}
