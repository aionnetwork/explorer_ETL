package aion.dashboard.domainobjects;

public class Transaction {

    public Long id;
    public String transactionHash;
    public String blockHash;
    public Long blockNumber;
    public Long transactionIndex;
    public String fromAddr;
    public String toAddr;
    public Long nrgConsumed;
    public Long nrgPrice;
    public long transactionTimestamp;
    public long blockTimestamp;
    public String value;
    public String transactionLog;
    public String data;
    public String nonce;
    public String txError;
    public String contractAddr;


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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public Long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public Long getTransactionIndex() {
        return transactionIndex;
    }

    public void setTransactionIndex(Long transactionIndex) {
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

    public Long getNrgConsumed() {
        return nrgConsumed;
    }

    public void setNrgConsumed(Long nrgConsumed) {
        this.nrgConsumed = nrgConsumed;
    }

    public Long getNrgPrice() {
        return nrgPrice;
    }

    public void setNrgPrice(Long nrgPrice) {
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

        Long id;
        String transactionHash;
        String blockHash;
        Long blockNumber;
        Long transactionIndex;
        String fromAddr;
        String toAddr;
        Long nrgConsumed;
        Long nrgPrice;
        long transactionTimestamp;
        long blockTimestamp;
        String value;
        String transactionLog;
        String data;
        String nonce;
        String txError;
        String contractAddr;

        public TransactionBuilder setId(Long id) {
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

        public TransactionBuilder setBlockNumber(Long blockNumber) {
            this.blockNumber = blockNumber;
            return this;
        }

        public TransactionBuilder setTransactionIndex(Long transactionIndex) {
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

        public TransactionBuilder setNrgConsumed(Long nrgConsumed) {
            this.nrgConsumed = nrgConsumed;
            return this;
        }

        public TransactionBuilder setNrgPrice(Long nrgPrice) {
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

        public Transaction build() {
            Transaction tx = new Transaction();

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
