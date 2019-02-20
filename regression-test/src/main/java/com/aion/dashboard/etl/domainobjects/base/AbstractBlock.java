package com.aion.dashboard.etl.domainobjects.base;

import java.util.Objects;

public abstract class AbstractBlock {

    public long blockNumber;
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
    public long nrgConsumed;
    public long nrgLimit;
    public long blockTimestamp;
    public long numTransactions;
    public long blockTime;

    public boolean compare(AbstractBlock block) {
        return blockNumber == block.blockNumber &&
                nrgConsumed == block.nrgConsumed &&
                nrgLimit == block.nrgLimit &&
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
                Objects.equals(solution, block.solution);
    }
}
