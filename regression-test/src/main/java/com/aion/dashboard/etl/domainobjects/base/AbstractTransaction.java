package com.aion.dashboard.etl.domainobjects.base;

import java.util.Objects;

public abstract class AbstractTransaction {
    public String transactionHash;
    public String fromAddr;
    public String toAddr;
    public String blockHash;
    public long blockNumber;
    public long blockTimestamp;
    public long transactionIndex;
    public long nrgConsumed;
    public long nrgPrice;
    public long transactionTimestamp;
    public String transactionLog;
    public String data;
    public String nonce;
    public String txError;
    public String contractAddr;

    public boolean compare(AbstractTransaction transaction) {
        return blockNumber == transaction.blockNumber &&
                blockTimestamp == transaction.blockTimestamp &&
                transactionIndex == transaction.transactionIndex &&
                nrgConsumed == transaction.nrgConsumed &&
                nrgPrice == transaction.nrgPrice &&
                transactionTimestamp == transaction.transactionTimestamp &&
                Objects.equals(transactionHash, transaction.transactionHash) &&
                Objects.equals(fromAddr, transaction.fromAddr) &&
                Objects.equals(toAddr, transaction.toAddr) &&
                Objects.equals(blockHash, transaction.blockHash) &&
                Objects.equals(transactionLog, transaction.transactionLog) &&
                Objects.equals(data, transaction.data) &&
                Objects.equals(nonce, transaction.nonce) &&
                Objects.equals(txError, transaction.txError) &&
                Objects.equals(contractAddr, transaction.contractAddr);
    }
}
