package aion.dashboard.blockchain.type;

import aion.dashboard.domainobject.Block;
import aion.dashboard.util.Utils;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.aion.base.type.Hash256;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@JsonDeserialize(builder = APIBlock.APIBlockBuilder.class)
public class APIBlock {
    private String logsBloom;
    private Long totalDifficulty;
    private String receiptsRoot;
    private String extraData;
    private Long nrgUsed;
    private List<String> transactions;
    private String nonce;
    private String miner;
    private long difficulty;
    private long number;
    private String gasLimit;
    private String gasUsed;
    private Long nrgLimit;
    private String solution;
    private long size;
    private String transactionsRoot;
    private String stateRoot;
    private String parentHash;
    private String hash;
    private long timestamp;

    public String getHash() {
        return hash;
    }

    private APIBlock(String logsBloom, String totalDifficulty, String receiptsRoot, String extraData, String nrgUsed, List<String> transactions, String nonce, String miner, String difficulty, String number, String gasLimit, String gasUsed, String nrgLimit, String solution, String size, String transactionsRoot, String stateRoot, String parentHash, String hash, String timestamp) {
        this.logsBloom = Utils.sanitizeHex(logsBloom);
        this.totalDifficulty = Long.parseLong(Utils.sanitizeHex(totalDifficulty),16);
        this.receiptsRoot = Utils.sanitizeHex(receiptsRoot);
        this.extraData = Utils.sanitizeHex(extraData);
        this.nrgUsed = Long.parseLong(Utils.sanitizeHex(nrgUsed), 16);
        this.transactions = transactions.stream().map(Utils::sanitizeHex).collect(Collectors.toList());
        this.nonce = Utils.sanitizeHex(nonce);
        this.miner = Utils.sanitizeHex(miner);
        this.difficulty = Long.parseLong(Utils.sanitizeHex(difficulty),16);
        this.number = Long.parseLong(number,10);
        this.gasLimit = Utils.sanitizeHex(gasLimit);
        this.gasUsed = Utils.sanitizeHex(gasUsed);
        this.nrgLimit = Long.parseLong(Utils.sanitizeHex(nrgLimit),16);
        this.solution = Utils.sanitizeHex(solution);
        this.size = Long.parseLong(Utils.sanitizeHex(size),16);
        this.transactionsRoot = Utils.sanitizeHex(transactionsRoot);
        this.stateRoot = Utils.sanitizeHex(stateRoot);
        this.parentHash = Utils.sanitizeHex(parentHash);
        this.hash = Utils.sanitizeHex(hash);
        this.timestamp = Long.parseLong(Utils.sanitizeHex(timestamp), 16);
    }

    @Override
    public String toString() {
        return "APIBlock{" +
                "logsBloom='" + logsBloom + '\'' +
                ", totalDifficulty=" + totalDifficulty +
                ", receiptsRoot='" + receiptsRoot + '\'' +
                ", extraData='" + extraData + '\'' +
                ", nrgUsed=" + nrgUsed +
                ", transactions=" + transactions +
                ", nonce='" + nonce + '\'' +
                ", miner='" + miner + '\'' +
                ", difficulty=" + difficulty +
                ", number=" + number +
                ", gasLimit='" + gasLimit + '\'' +
                ", gasUsed='" + gasUsed + '\'' +
                ", nrgLimit=" + nrgLimit +
                ", solution='" + solution + '\'' +
                ", size=" + size +
                ", transactionsRoot='" + transactionsRoot + '\'' +
                ", stateRoot='" + stateRoot + '\'' +
                ", parentHash='" + parentHash + '\'' +
                ", hash='" + hash + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    public boolean compareBlocks(aion.dashboard.domainobject.Block block ){
        if (block == null) {
            return false;
        }else {
            List<String> txHashes = Arrays.stream(block.getTransactionHashes().replaceAll("(\\[|]|\")", "").split(",")).filter(s -> !s.isBlank()).filter(s1 -> !s1.isEmpty()).collect(Collectors.toList());

            if (this.transactions.containsAll(txHashes) && txHashes.containsAll(transactions)) {
                return Utils.compareStrings(this.parentHash, block.getParentHash()) && Utils.compareStrings(this.logsBloom, block.getBloom())
                        && this.totalDifficulty == block.getTotalDifficulty() && Utils.compareStrings(this.receiptsRoot, block.getReceiptTxRoot())
                        && Utils.compareStrings(this.extraData, (block.getExtraData())) && this.nrgUsed == block.getNrgConsumed()
                        && Utils.compareStrings(this.nonce, block.getNonce()) && Utils.compareStrings(this.miner, (block.getMinerAddress()))
                        && this.difficulty == (block.getDifficulty()) && this.number == block.getBlockNumber()
                        && this.nrgLimit == (block.getNrgLimit()) && Utils.compareStrings(this.solution, (block.getSolution()))
                        && this.size == block.getBlockSize() && Utils.compareStrings(this.transactionsRoot, (block.getTxTrieRoot()))
                        && Utils.compareStrings(this.stateRoot, block.getStateRoot()) && Utils.compareStrings(block.getBlockHash(), (this.hash))
                        && this.timestamp == block.getBlockTimestamp();

            } else {
                return false;
            }
        }

    }

    public boolean compareHash(Block block){
        return block != null && Utils.compareStrings(this.hash, block.getBlockHash());
    }




    private static final ThreadLocal<APIBlockBuilder> blockBuilderThreadLocal = ThreadLocal.withInitial(APIBlockBuilder::new);


    public static APIBlock from(org.aion.api.type.Block block){
        APIBlockBuilder apiBlockBuilder = blockBuilderThreadLocal.get();
        return apiBlockBuilder.setDifficulty(block.getDifficulty().toString(16))
                .setExtraData(block.getExtraData().toString())
                .setGasLimit(Long.toHexString(block.getNrgLimit()))
                .setGasUsed(Long.toHexString(block.getNrgConsumed()))
                .setLogsBloom(block.getBloom().toString())
                .setMiner(block.getMinerAddress().toString())
                .setNonce(block.getNonce().toString(16))
                .setTotalDifficulty(block.getTotalDifficulty().toString(16))
                .setNumber(Long.toString(block.getNumber()))
                .setParentHash(block.getParentHash().toString())
                .setReceiptsRoot(block.getReceiptTxRoot().toString())
                .setStateRoot(block.getStateRoot().toString())
                .setSize(Integer.toHexString(block.getSize()))
                .setSolution(block.getSolution().toString())
                .setTransactionsRoot(block.getTxTrieRoot().toString())
                .setTransactions(block.getTxHash().stream().map(Hash256::toString).collect(Collectors.toList()))
                .setNrgLimit(Long.toHexString(block.getNrgLimit()))
                .setNrgUsed(Long.toHexString(block.getNrgConsumed()))
                .setHash(block.getHash().toString())
                .setTimestamp(Long.toHexString(block.getTimestamp())).create();

    }


    @JsonPOJOBuilder(buildMethodName = "create", withPrefix = "set")
    public static class APIBlockBuilder {
        private String logsBloom;
        private String totalDifficulty;
        private String receiptsRoot;
        private String extraData;
        private String nrgUsed;
        private List<String> transactions;
        private String nonce;
        private String miner;
        private String difficulty;
        private String number;
        private String gasLimit;
        private String gasUsed;
        private String nrgLimit;
        private String solution;
        private String size;
        private String transactionsRoot;
        private String stateRoot;
        private String parentHash;
        private String hash;
        private String timestamp;

        public APIBlockBuilder setHash(String hash) {
            this.hash = hash;
            return this;
        }

        APIBlockBuilder setLogsBloom(String logsBloom) {
            this.logsBloom = logsBloom;
            return this;
        }

        APIBlockBuilder setTotalDifficulty(String totalDifficulty) {
            this.totalDifficulty = totalDifficulty;
            return this;
        }

        APIBlockBuilder setReceiptsRoot(String receiptsRoot) {
            this.receiptsRoot = receiptsRoot;
            return this;
        }

        APIBlockBuilder setExtraData(String extraData) {
            this.extraData = extraData;
            return this;
        }

        APIBlockBuilder setNrgUsed(String nrgUsed) {
            this.nrgUsed = nrgUsed;
            return this;
        }

        APIBlockBuilder setTransactions(List<String> transactions) {
            this.transactions = transactions;
            return this;
        }

        APIBlockBuilder setNonce(String nonce) {
            this.nonce = nonce;
            return this;
        }

        APIBlockBuilder setMiner(String miner) {
            this.miner = miner;
            return this;
        }

        APIBlockBuilder setDifficulty(String difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        APIBlockBuilder setNumber(String number) {
            this.number = number;
            return this;
        }

        APIBlockBuilder setGasLimit(String gasLimit) {
            this.gasLimit = gasLimit;
            return this;
        }

        APIBlockBuilder setGasUsed(String gasUsed) {
            this.gasUsed = gasUsed;
            return this;
        }

        APIBlockBuilder setNrgLimit(String nrgLimit) {
            this.nrgLimit = nrgLimit;
            return this;
        }

        APIBlockBuilder setSolution(String solution) {
            this.solution = solution;
            return this;
        }

        APIBlockBuilder setSize(String size) {
            this.size = size;
            return this;
        }

        APIBlockBuilder setTransactionsRoot(String transactionsRoot) {
            this.transactionsRoot = transactionsRoot;
            return this;
        }

        APIBlockBuilder setStateRoot(String stateRoot) {
            this.stateRoot = stateRoot;
            return this;
        }

        APIBlockBuilder setParentHash(String parentHash) {
            this.parentHash = parentHash;
            return this;
        }

        APIBlock create() {
            return new APIBlock(logsBloom, totalDifficulty, receiptsRoot, extraData, nrgUsed, transactions, nonce, miner, difficulty, number, gasLimit, gasUsed, nrgLimit, solution, size, transactionsRoot, stateRoot, parentHash, hash, timestamp);
        }

        public APIBlockBuilder setTimestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }
    }
}
