package com.aion.dashboard.etl.domainobjects.v5;


import com.aion.dashboard.etl.domainobjects.base.AbstractBlock;
import com.aion.dashboard.etl.domainobjects.v4.V4Block;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;

import static com.aion.dashboard.etl.util.Utils.getZDT;

public class V5Block extends AbstractBlock {

    private long difficulty;
    private long totalDifficulty;
    private long blockSize;
    private String transactionHashes;
    private String lastTransactionHash;
    private BigDecimal nrgReward;
    private int blockYear;
    private int blockMonth;
    private int blockDay;
    private double approxNrgReward;

    @Override
    public boolean compare(AbstractBlock block) {
        if(block instanceof V4Block) {
            V4Block v4Block = (V4Block) block;

            String[] v5TransactionHashes = transactionHashes.replace("[", "").replace("]", "").trim().split(",");
            for (String v5TransactionHash : v5TransactionHashes) {
                if(!v4Block.getTransactionList().contains(v5TransactionHash)) {
                    return false;
                }
            }

            return super.compare(block) &&
                    blockSize == v4Block.getSize() &&
                    nrgReward.doubleValue() == v4Block.getNrgReward().doubleValue() &&
                    Long.toHexString(difficulty).equalsIgnoreCase(v4Block.getDifficulty()) &&
                    Long.toHexString(totalDifficulty).equalsIgnoreCase(v4Block.getTotalDifficulty());

        } return this.equals(block);
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

    public double getApproxNrgReward() {
        return approxNrgReward;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        V5Block v5Block = (V5Block) o;
        return blockNumber == v5Block.blockNumber &&
                nrgConsumed == v5Block.nrgConsumed &&
                nrgLimit == v5Block.nrgLimit &&
                blockSize == v5Block.blockSize &&
                blockTimestamp == v5Block.blockTimestamp &&
                numTransactions == v5Block.numTransactions &&
                blockTime == v5Block.blockTime &&
                Objects.equals(blockHash, v5Block.blockHash) &&
                Objects.equals(minerAddress, v5Block.minerAddress) &&
                Objects.equals(parentHash, v5Block.parentHash) &&
                Objects.equals(receiptTxRoot, v5Block.receiptTxRoot) &&
                Objects.equals(stateRoot, v5Block.stateRoot) &&
                Objects.equals(txTrieRoot, v5Block.txTrieRoot) &&
                Objects.equals(extraData, v5Block.extraData) &&
                Objects.equals(nonce, v5Block.nonce) &&
                Objects.equals(bloom, v5Block.bloom) &&
                Objects.equals(solution, v5Block.solution) &&
                Objects.equals(difficulty, v5Block.difficulty) &&
                Objects.equals(totalDifficulty, v5Block.totalDifficulty) &&
                Objects.equals(transactionHashes, v5Block.transactionHashes) &&
                Objects.equals(lastTransactionHash, v5Block.lastTransactionHash) &&
                Objects.equals(nrgReward, v5Block.nrgReward);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockNumber, blockHash, minerAddress, parentHash, receiptTxRoot, stateRoot, txTrieRoot, extraData, nonce, bloom, solution, difficulty, totalDifficulty, nrgConsumed, nrgLimit, blockSize, blockTimestamp, numTransactions, blockTime, lastTransactionHash, transactionHashes, nrgReward);
    }

    public String getLastTransactionHash() {
        return lastTransactionHash;
    }

    public void setLastTransactionHash(String lastTransactionHash) {
        this.lastTransactionHash = lastTransactionHash;
    }

    public String getTransactionHashes() {
        return transactionHashes;
    }



    public void setApproxNrgReward(double approxNrgReward) {
        this.approxNrgReward = approxNrgReward;
    }

    public void setTransactionHashes(String transactionHashes) {
        this.transactionHashes = transactionHashes;
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

    public long getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(long difficulty) {
        this.difficulty = difficulty;
    }

    public long getTotalDifficulty() {
        return totalDifficulty;
    }

    public void setTotalDifficulty(long totalDifficulty) {
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

    public long getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(long blockSize) {
        this.blockSize = blockSize;
    }

    public long getBlockTimestamp() {
        return blockTimestamp;
    }

    public void setBlockTimestamp(long blockTimestamp) {
        this.blockTimestamp = blockTimestamp;
        final ZonedDateTime zonedDateTime = getZDT(blockTimestamp);
        blockYear = zonedDateTime.getYear();
        blockMonth = zonedDateTime.getMonthValue();
        blockDay = zonedDateTime.getDayOfMonth();

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

    public BigDecimal getNrgReward() {
        return nrgReward;
    }

    public void setNrgReward(BigDecimal nrgReward) {
        this.nrgReward = nrgReward;
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
        long difficulty;
        long totalDifficulty;
        long nrgConsumed;
        long nrgLimit;
        long blockSize;
        long blockTimestamp;
        long numTransactions;
        long blockTime;
        String transactionHashes;
        String lastTransactionHash;
        BigDecimal nrgReward;
        double approxNrgReward;


        public BlockBuilder approxNrgReward(double nrgReward){
            approxNrgReward = nrgReward;
            return this;
        }
        public BlockBuilder() {
        }

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

        public BlockBuilder difficulty(long difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public BlockBuilder totalDifficulty(long totalDifficulty) {
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

        public BlockBuilder blockSize(long blockSize) {
            this.blockSize = blockSize;
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

        public BlockBuilder transactionHash(String lastTransactionHash) {
            this.lastTransactionHash = lastTransactionHash;
            return this;
        }

        public BlockBuilder transactionHashes(String transactionHashes) {
            this.transactionHashes = transactionHashes;
            return this;
        }

        public BlockBuilder nrgReward(BigDecimal nrgReward) {
            this.nrgReward = nrgReward;
            return this;
        }

        public V5Block build() {
            V5Block v5Block = new V5Block();

            v5Block.blockNumber = blockNumber;
            v5Block.blockHash = blockHash;
            v5Block.minerAddress = minerAddress;
            v5Block.parentHash = parentHash;
            v5Block.receiptTxRoot = receiptTxRoot;
            v5Block.stateRoot = stateRoot;
            v5Block.txTrieRoot = txTrieRoot;
            v5Block.extraData = extraData;
            v5Block.nonce = nonce;
            v5Block.bloom = bloom;
            v5Block.solution = solution;
            v5Block.difficulty = difficulty;
            v5Block.totalDifficulty = totalDifficulty;
            v5Block.nrgConsumed = nrgConsumed;
            v5Block.nrgLimit = nrgLimit;
            v5Block.blockSize = blockSize;
            v5Block.setBlockTimestamp(blockTimestamp);
            v5Block.numTransactions = numTransactions;
            v5Block.blockTime = blockTime;
            v5Block.transactionHashes = transactionHashes;
            v5Block.lastTransactionHash = lastTransactionHash;
            v5Block.nrgReward = nrgReward;
            v5Block.approxNrgReward = approxNrgReward;
            return v5Block;
        }
    }
}
