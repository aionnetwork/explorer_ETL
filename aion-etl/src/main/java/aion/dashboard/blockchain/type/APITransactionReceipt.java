package aion.dashboard.blockchain.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.aion.util.bytes.ByteUtil;

import java.util.List;

public class APITransactionReceipt {
    private String blockHash;
    private String nrgPrice;
    private String logsBloom;
    private String nrgUsed;
    private String contractAddress;
    private int transactionIndex;
    private String transactionHash;
    private String gasLimit;
    private String cumulativeNrgUsed;
    private String gasUsed;
    private int blockNumber;
    private String root;
    private String cumulativeGasUsed;
    private String from;
    private String to;
    private List<APITransactionLog> logs;
    private String gasPrice;
    private String status;


    @JsonCreator
    public APITransactionReceipt(
            @JsonProperty("blockHash") String blockHash,
            @JsonProperty("nrgPrice") String nrgPrice,
            @JsonProperty("logsBloom") String logsBloom,
            @JsonProperty("nrgUsed") String nrgUsed,
            @JsonProperty("contractAddress") String contractAddress,
            @JsonProperty("transactionIndex") String transactionIndex,
            @JsonProperty("transactionHash") String transactionHash,
            @JsonProperty("gasLimit") String gasLimit,
            @JsonProperty("cumulativeNrgUsed") String cumulativeNrgUsed,
            @JsonProperty("gasUsed") String gasUsed,
            @JsonProperty("blockNumber") String blockNumber,
            @JsonProperty("root") String root,
            @JsonProperty("cumulativeGasUsed") String cumulativeGasUsed,
            @JsonProperty("from") String from,
            @JsonProperty("to") String to,
            @JsonProperty("logs") List<APITransactionLog> logs,
            @JsonProperty("gasPrice") String gasPrice,
            @JsonProperty("status") String status) {
        this.blockHash = blockHash;
        this.nrgPrice = nrgPrice;
        this.logsBloom = logsBloom;
        this.nrgUsed = nrgUsed;
        this.contractAddress = contractAddress;
        this.transactionIndex = ByteUtil.byteArrayToInt(ByteUtil.hexStringToBytes(transactionIndex));
        this.transactionHash = transactionHash;
        this.gasLimit = gasLimit;
        this.cumulativeNrgUsed = cumulativeNrgUsed;
        this.gasUsed = gasUsed;
        this.blockNumber = ByteUtil.byteArrayToInt(ByteUtil.hexStringToBytes(blockNumber));
        this.root = root;
        this.cumulativeGasUsed = cumulativeGasUsed;
        this.from = from;
        this.to = to;
        this.logs = logs;
        this.gasPrice = gasPrice;
        this.status = status;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public String getNrgPrice() {
        return nrgPrice;
    }

    public String getLogsBloom() {
        return logsBloom;
    }

    public String getNrgUsed() {
        return nrgUsed;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public int getTransactionIndex() {
        return transactionIndex;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public String getGasLimit() {
        return gasLimit;
    }

    public String getCumulativeNrgUsed() {
        return cumulativeNrgUsed;
    }

    public String getGasUsed() {
        return gasUsed;
    }



    public String getRoot() {
        return root;
    }

    public String getCumulativeGasUsed() {
        return cumulativeGasUsed;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public int getBlockNumber() {
        return blockNumber;
    }

    public APITransactionReceipt setBlockNumber(int blockNumber) {
        this.blockNumber = blockNumber;
        return this;
    }

    public List<APITransactionLog> getLogs() {
        return logs;
    }

    @Override
    public String toString() {
        return "APITransactionReceipt{" +
                "blockHash='" + blockHash + '\'' +
                ", nrgPrice='" + nrgPrice + '\'' +
                ", logsBloom='" + logsBloom + '\'' +
                ", nrgUsed='" + nrgUsed + '\'' +
                ", contractAddress='" + contractAddress + '\'' +
                ", transactionIndex=" + transactionIndex +
                ", transactionHash='" + transactionHash + '\'' +
                ", gasLimit='" + gasLimit + '\'' +
                ", cumulativeNrgUsed='" + cumulativeNrgUsed + '\'' +
                ", gasUsed='" + gasUsed + '\'' +
                ", blockNumber=" + blockNumber +
                ", root='" + root + '\'' +
                ", cumulativeGasUsed='" + cumulativeGasUsed + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", logs=" + logs +
                '}';
    }
}
