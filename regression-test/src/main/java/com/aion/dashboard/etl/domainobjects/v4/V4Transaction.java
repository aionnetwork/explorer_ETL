package com.aion.dashboard.etl.domainobjects.v4;

import com.aion.dashboard.etl.domainobjects.v5.V5Transaction;
import com.aion.dashboard.etl.domainobjects.base.AbstractTransaction;

import java.math.BigInteger;

public class V4Transaction extends AbstractTransaction {

    private BigInteger id;
    private String value;

    @Override
    public boolean compare(AbstractTransaction transaction) {
        if(transaction instanceof V5Transaction) {
            V5Transaction v5Transaction = (V5Transaction) transaction;
            return super.compare(v5Transaction) &&
                    value.equals(v5Transaction.getValue().toBigInteger().toString(16));
        } else return this.equals(transaction);
    }

    public String getTxError() {
        return txError;
    }

    public void setTxError(String txError) {
        this.txError = txError;
    }

    public String getContractAddr() {
        return contractAddr;
    }

    public void setContractAddr(String contractAddr) {
        this.contractAddr = contractAddr;
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public long getTransactionIndex() {
        return transactionIndex;
    }

    public void setTransactionIndex(long transactionIndex) {
        this.transactionIndex = transactionIndex;
    }

    public String getFromAddr() {
        return fromAddr;
    }

    public void setFromAddr(String fromAddr) {
        this.fromAddr = fromAddr;
    }

    public String getToAddr() {
        return toAddr;
    }

    public void setToAddr(String toAddr) {
        this.toAddr = toAddr;
    }

    public long getNrgConsumed() {
        return nrgConsumed;
    }

    public void setNrgConsumed(long nrgConsumed) {
        this.nrgConsumed = nrgConsumed;
    }

    public long getNrgPrice() {
        return nrgPrice;
    }

    public void setNrgPrice(long nrgPrice) {
        this.nrgPrice = nrgPrice;
    }

    public long getTransactionTimestamp() {
        return transactionTimestamp;
    }

    public void setTransactionTimestamp(long transactionTimestamp) {
        this.transactionTimestamp = transactionTimestamp;
    }

    public long getBlockTimestamp() {
        return blockTimestamp;
    }

    public void setBlockTimestamp(long blockTimestamp) {
        this.blockTimestamp = blockTimestamp;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTransactionLog() {
        return transactionLog;
    }

    public void setTransactionLog(String transactionLog) {
        this.transactionLog = transactionLog;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }


    public static class TransactionBuilder {

        BigInteger id;
        String transactionHash;
        String blockHash;
        long blockNumber;
        long transactionIndex;
        String fromAddr;
        String toAddr;
        long nrgConsumed;
        long nrgPrice;
        long transactionTimestamp;
        long blockTimestamp;
        String value;
        String transactionLog;
        String data;
        String nonce;
        String txError;
        String contractAddr;

        public TransactionBuilder setId(BigInteger id) {
            this.id = id;
            return this;
        }

        public TransactionBuilder setTransactionHash(String transactionHash) {
            this.transactionHash = transactionHash;
            return this;
        }

        public TransactionBuilder setBlockHash(String blockHash) {
            this.blockHash = blockHash;
            return this;
        }

        public TransactionBuilder setBlockNumber(long blockNumber) {
            this.blockNumber = blockNumber;
            return this;
        }

        public TransactionBuilder setTransactionIndex(long transactionIndex) {
            this.transactionIndex = transactionIndex;
            return this;
        }

        public TransactionBuilder setFromAddr(String fromAddr) {
            this.fromAddr = fromAddr;
            return this;
        }

        public TransactionBuilder setToAddr(String toAddr) {
            this.toAddr = toAddr;
            return this;
        }

        public TransactionBuilder setNrgConsumed(long nrgConsumed) {
            this.nrgConsumed = nrgConsumed;
            return this;
        }

        public TransactionBuilder setNrgPrice(long nrgPrice) {
            this.nrgPrice = nrgPrice;
            return this;
        }

        public TransactionBuilder setTransactionTimestamp(long transactionTimestamp) {
            this.transactionTimestamp = transactionTimestamp;
            return this;
        }

        public TransactionBuilder setBlockTimestamp(long blockTimestamp) {
            this.blockTimestamp = blockTimestamp;
            return this;
        }

        public TransactionBuilder setValue(String value) {
            this.value = value;
            return this;
        }

        public TransactionBuilder setTransactionLog(String transactionLog) {
            this.transactionLog = transactionLog;
            return this;
        }

        public TransactionBuilder setData(String data) {
            this.data = data;
            return this;
        }

        public TransactionBuilder setNonce(String nonce) {
            this.nonce = nonce;
            return this;
        }

        public TransactionBuilder setTxError(String txError) {
            this.txError = txError;
            return this;
        }

        public TransactionBuilder setContractAddr(String contractAddr) {
            this.contractAddr = contractAddr;
            return this;
        }


        public TransactionBuilder() {
        }

        public V4Transaction build() {
            V4Transaction tx = new V4Transaction();

            tx.id = id;
            tx.transactionHash = transactionHash;
            tx.blockHash = blockHash;
            tx.blockNumber = blockNumber;
            tx.transactionIndex = transactionIndex;
            tx.fromAddr = fromAddr;
            tx.toAddr = toAddr;
            tx.nrgConsumed = nrgConsumed;
            tx.nrgPrice = nrgPrice;
            tx.transactionTimestamp = transactionTimestamp;
            tx.blockTimestamp = blockTimestamp;
            tx.value = value;
            tx.transactionLog = transactionLog;
            tx.data = data;
            tx.nonce = nonce;
            tx.txError = txError;
            tx.contractAddr = contractAddr;


            return tx;
        }

    }

}
