package aion.dashboard.domainobject;

import aion.dashboard.blockchain.ContractType;
import aion.dashboard.util.Utils;
import org.aion.api.type.BlockDetails;
import org.aion.api.type.TxDetails;
import org.aion.api.type.TxLog;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.List;

import static aion.dashboard.util.Utils.getZDT;

public class Transaction {

    private String transactionHash;
    private String blockHash;
    private long blockNumber;
    private long blockTimestamp;
    private long transactionIndex;
    private String fromAddr;
    private String toAddr;
    private long nrgConsumed;
    private long nrgPrice;
    private long transactionTimestamp;
    private BigDecimal value;
    private String transactionLog;
    private String data;
    private String nonce;
    private String txError;
    private String contractAddr;
    private int blockYear;
    private int blockMonth;
    private int blockDay;
    private double approxValue;
    private ContractType type;

    public void setType(String type) {
        this.type = ContractType.fromType(type);
    }

    public void setType(byte type){
        this.type=ContractType.fromByte(type);
    }

    public String getType() {
        return type.type;
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
        var zdt = getZDT(blockTimestamp);
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

    public Transaction setApproxValue(double approxValue) {
        this.approxValue = approxValue;
        return this;
    }

    private static final ThreadLocal<TransactionBuilder> builderThreadLocal = ThreadLocal.withInitial(TransactionBuilder::new);
    public static Transaction from(TxDetails tx, BlockDetails b){
        return builderThreadLocal.get()
                .setType(tx.getType())
                .setTransactionHash(tx.getTxHash().toString())
                .setApproxValue(Utils.approximate(tx.getValue(), 18))
                .setTransactionHash(tx.getTxHash().toString())
                .setBlockHash(b.getHash().toString())
                .setBlockNumber(b.getNumber())
                .setBlockTimestamp(b.getTimestamp())
                .setTransactionIndex(tx.getTxIndex())
                .setFromAddr(tx.getFrom().toString())
                .setToAddr(tx.getTo().toString())
                .setNrgConsumed(tx.getNrgConsumed())
                .setNrgPrice(tx.getNrgPrice())
                .setData(tx.getData().toString())
                .setTransactionTimestamp(tx.getTimestamp())
                .setValue(new BigDecimal(tx.getValue()))
                .setTransactionLog(buildLog(tx.getLogs()))
                .setData(tx.getData().toString())
                .setNonce(tx.getNonce().toString(16))
                .setTxError(tx.getError())
                .setContractAddr(tx.getContract().toString())
                .build();
    }

    private static String buildLog(List<TxLog> logs){
        JSONArray txLogs = new JSONArray();
        for (var txLog : logs) {

            JSONObject txLogObject = new JSONObject();
            JSONArray topicsArr = new JSONArray();

            for (var topic : txLog.getTopics()){
                topicsArr.put(topic);
            }
            txLogObject.put("address", txLog.getAddress().toString());
            txLogObject.put("data", txLog.getData());


            txLogObject.put("topics", topicsArr);
            txLogs.put(txLogObject);


        }
        return txLogs.toString();
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionHash='" + transactionHash + '\'' +
                ", blockHash='" + blockHash + '\'' +
                ", blockNumber=" + blockNumber +
                ", blockTimestamp=" + blockTimestamp +
                ", transactionIndex=" + transactionIndex +
                ", fromAddr='" + fromAddr + '\'' +
                ", toAddr='" + toAddr + '\'' +
                ", nrgConsumed=" + nrgConsumed +
                ", nrgPrice=" + nrgPrice +
                ", transactionTimestamp=" + transactionTimestamp +
                ", value=" + value +
                ", transactionLog='" + transactionLog + '\'' +
                ", data='" + data + '\'' +
                ", nonce='" + nonce + '\'' +
                ", txError='" + txError + '\'' +
                ", contractAddr='" + contractAddr + '\'' +
                ", blockYear=" + blockYear +
                ", blockMonth=" + blockMonth +
                ", blockDay=" + blockDay +
                ", approxValue=" + approxValue +
                ", type=" + type +
                '}';
    }

    public static class TransactionBuilder {

        private String transactionHash;
        private String blockHash;
        private long blockNumber;
        private long blockTimestamp;
        private long transactionIndex;
        private String fromAddr;
        private String toAddr;
        private long nrgConsumed;
        private long nrgPrice;
        private long transactionTimestamp;
        private BigDecimal value;
        private String transactionLog;
        private String data;
        private String nonce;
        private String txError;
        private String contractAddr;
        private double approxValue;
        private ContractType type;

        public TransactionBuilder setType(byte bytes){
            type= ContractType.fromByte(bytes);
            return this;
        }

        public TransactionBuilder setType(String typeStr){
            type= ContractType.fromType(typeStr);
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

        

        public Transaction build() {
            Transaction tx = new Transaction();

            tx.transactionHash = transactionHash;
            tx.blockHash = blockHash;
            tx.blockNumber = blockNumber;
            tx.setBlockTimestamp(blockTimestamp);
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
            tx.type = type;
            return tx;
        }

        public TransactionBuilder setApproxValue(double approxValue) {
            this.approxValue = approxValue;
            return this;
        }
    }

}