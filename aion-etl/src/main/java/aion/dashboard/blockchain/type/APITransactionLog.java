package aion.dashboard.blockchain.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.aion.util.bytes.ByteUtil;

import java.util.List;

public class APITransactionLog {
    String address;
    int logIndex;
    String data;
    List<String> topics;
    long blockNumber;
    int transactionIndex;

    @JsonCreator
    public APITransactionLog(
            @JsonProperty("address") String address,
            @JsonProperty("logIndex") String logIndex,
            @JsonProperty("data") String data,
            @JsonProperty("topics") List<String> topics,
            @JsonProperty("blockNumber") String blockNumber,
            @JsonProperty("transactionIndex") String transactionIndex) {
        this.address = address;
        this.logIndex = ByteUtil.byteArrayToInt(ByteUtil.hexStringToBytes(logIndex));
        this.data = data;
        this.topics = topics;
        this.blockNumber = ByteUtil.byteArrayToInt(ByteUtil.hexStringToBytes(blockNumber));
        this.transactionIndex = ByteUtil.byteArrayToInt(ByteUtil.hexStringToBytes(transactionIndex));
    }

    public String getAddress() {
        return address;
    }

    public int getLogIndex() {
        return logIndex;
    }

    public String getData() {
        return data;
    }

    public List<String> getTopics() {
        return topics;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public int getTransactionIndex() {
        return transactionIndex;
    }

    @Override
    public String toString() {
        return "APITransactionLog{" +
                "address='" + address + '\'' +
                ", logIndex=" + logIndex +
                ", data='" + data + '\'' +
                ", topics=" + topics +
                ", blockNumber=" + blockNumber +
                ", transactionIndex=" + transactionIndex +
                '}';
    }
}
