package com.aion.dashboard.etl.domainobjects.v5;

import com.aion.dashboard.etl.domainobjects.base.AbstractTransaction;
import com.aion.dashboard.etl.domainobjects.v4.V4Transaction;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static com.aion.dashboard.etl.util.Utils.getZDT;

public class V5Transaction extends AbstractTransaction {
    private BigDecimal value;
    private int blockYear;
    private int blockMonth;
    private int blockDay;
    private double approxValue;

    @Override
    public boolean compare(AbstractTransaction transaction) {
        if(transaction instanceof V4Transaction) {
            V4Transaction v4Transaction = (V4Transaction) transaction;
            return super.compare(v4Transaction) &&
                    value.equals(new BigDecimal(v4Transaction.getValue()));
        } else return this.equals(transaction);
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

    public long getBlockTimestamp() {
        return blockTimestamp;
    }

    public void setBlockTimestamp(long blockTimestamp) {
        ZonedDateTime zdt = getZDT(blockTimestamp);
        blockYear = zdt.getYear();
        blockMonth = zdt.getMonthValue();
        blockDay = zdt.getDayOfMonth();
        this.blockTimestamp = blockTimestamp;
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

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
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

    public double getApproxValue() {
        return approxValue;
    }

    public V5Transaction setApproxValue(double approxValue) {
        this.approxValue = approxValue;
        return this;
    }


    public static class TransactionBuilder {

        String transactionHash;
        String blockHash;
        long blockNumber;
        long blockTimestamp;
        long transactionIndex;
        String fromAddr;
        String toAddr;
        long nrgConsumed;
        long nrgPrice;
        long transactionTimestamp;
        BigDecimal value;
        String transactionLog;
        String data;
        String nonce;
        String txError;
        String contractAddr;
        private double approxValue;


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

        public TransactionBuilder setBlockTimestamp(long blockTimestamp) {
            this.blockTimestamp = blockTimestamp;
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

        public TransactionBuilder setValue(BigDecimal value) {
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

        public V5Transaction build() {
            V5Transaction tx = new V5Transaction();

            tx.transactionHash = transactionHash;
            tx.blockHash = blockHash;
            tx.blockNumber = blockNumber;
            tx.blockTimestamp = blockTimestamp;
            tx.transactionIndex = transactionIndex;
            tx.fromAddr = fromAddr;
            tx.toAddr = toAddr;
            tx.nrgConsumed = nrgConsumed;
            tx.nrgPrice = nrgPrice;
            tx.transactionTimestamp = transactionTimestamp;
            tx.value = value;
            tx.transactionLog = transactionLog;
            tx.data = data;
            tx.nonce = nonce;
            tx.txError = txError;
            tx.contractAddr = contractAddr;
            tx.approxValue = approxValue;


            return tx;
        }

        public TransactionBuilder setApproxValue(double approxValue) {
            this.approxValue = approxValue;
            return this;
        }
    }

}