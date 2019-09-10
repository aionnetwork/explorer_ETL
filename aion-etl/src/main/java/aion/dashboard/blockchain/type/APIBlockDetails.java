package aion.dashboard.blockchain.type;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

import static aion.dashboard.util.Utils.bigIntegerFromHex;
import static aion.dashboard.util.Utils.longFromHexString;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

@JsonDeserialize(builder = APIBlockDetails.Builder.class)
public class APIBlockDetails {
    private int numTransactions;
    private String logsBloom;
    private long totalDifficulty;
    private String receiptsRoot;
    private String extraData;
    private long nrgUsed;
    private String miner;
    private long difficulty;
    private long number;
    private String gasLimit;
    private String gasUsed;
    private long nrgLimit;
    private long size;
    private String transactionsRoot;
    private String stateRoot;
    private String parentHash;
    private String hash;
    private long timestamp;
    private List<APITxDetails> txDetails;
    private boolean isMainChain;
    private String antiParentHash;
    private int blockTime;
    private String txTrieRoot;
    private String bloom;
    private BigInteger blockReward;
    //differentiates block types
    private APIBlock.SealType sealType;
    //--------------------------
    //Required for pos blocks
    private String seed;
    private String signature;
    private String publicKey;
    //--------------------------
    //Required for pow blocks
    private String solution;
    private String nonce;
    //--------------------------

    private APIBlockDetails(Builder builder){
        logsBloom = requireNonNull(builder.logsBloom);
        totalDifficulty = requireNonNull(builder.totalDifficulty);
        receiptsRoot = requireNonNull(builder.receiptsRoot);
        extraData = requireNonNull(builder.extraData);
        nrgUsed = requireNonNull(builder.nrgUsed);
        miner = requireNonNull(builder.miner);
        difficulty = builder.difficulty;
        number = builder.number;
        nrgLimit = requireNonNull(builder.nrgLimit);
        size = builder.size;
        transactionsRoot = requireNonNull(builder.transactionsRoot);
        stateRoot = requireNonNull(builder.stateRoot);
        parentHash = requireNonNull(builder.parentHash);
        hash = requireNonNull(builder.hash);
        timestamp = builder.timestamp;
        txDetails = requireNonNull(builder.apiTxDetails);
        isMainChain = requireNonNullElse(builder.isMainChain, true);
        antiParentHash = requireNonNullElse(builder.antiParentHash, "");
        numTransactions = builder.numTransactions;
        blockReward = requireNonNull(builder.blockReward);
        bloom = requireNonNull(builder.logsBloom);
        txTrieRoot = requireNonNull(builder.txTrieRoot);
        blockTime = builder.blockTime;
        sealType = APIBlock.SealType.fromByte(Objects.requireNonNullElse(builder.sealType, 1));
        if (sealType.equals(APIBlock.SealType.POW)){
            nonce = requireNonNull(builder.nonce);
            solution = requireNonNull(builder.solution);
            seed = "";
            signature = "";
            publicKey = "";
        }
        else {
            seed = requireNonNull(builder.seed);
            signature = requireNonNull(builder.signature);
            publicKey = requireNonNull(builder.publicKey);
            nonce = "";
            solution = "";
        }
    }


    public String getLogsBloom() {
        return logsBloom;
    }

    public long getTotalDifficulty() {
        return totalDifficulty;
    }

    public String getReceiptsRoot() {
        return receiptsRoot;
    }

    public String getExtraData() {
        return extraData;
    }

    public long getNrgUsed() {
        return nrgUsed;
    }

    public String getNonce() {
        return nonce;
    }

    public String getMiner() {
        return miner;
    }

    public long getDifficulty() {
        return difficulty;
    }

    public long getNumber() {
        return number;
    }

    public String getGasLimit() {
        return gasLimit;
    }

    public String getGasUsed() {
        return gasUsed;
    }

    public long getNrgLimit() {
        return nrgLimit;
    }

    public String getSolution() {
        return solution;
    }

    public long getSize() {
        return size;
    }

    public String getTransactionsRoot() {
        return transactionsRoot;
    }

    public String getStateRoot() {
        return stateRoot;
    }

    public String getParentHash() {
        return parentHash;
    }

    public String getHash() {
        return hash;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<APITxDetails> getTxDetails() {
        return txDetails;
    }

    public boolean isMainChain() {
        return isMainChain;
    }

    public String getAntiParentHash() {
        return antiParentHash;
    }

    public APIBlock.SealType getSealType() {
        return sealType;
    }

    public String getSeed() {
        return seed;
    }

    public String getSignature() {
        return signature;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public int getNumTransactions() {
        return numTransactions;
    }

    public int getBlockTime() {
        return blockTime;
    }

    public String getTxTrieRoot() {
        return txTrieRoot;
    }

    public String getBloom() {
        return bloom;
    }

    public BigInteger getBlockReward() {
        return blockReward;
    }


    @JsonPOJOBuilder(withPrefix = "set", buildMethodName = "create")
    public static class Builder{
        private String logsBloom;
        private Long totalDifficulty;
        private String receiptsRoot;
        private String extraData;
        private Long nrgUsed;
        private String nonce;
        private String miner;
        private long difficulty;
        private long number;
        private Long nrgLimit;
        private String solution;
        private long size;
        private String transactionsRoot;
        private String stateRoot;
        private String parentHash;
        private String hash;
        private long timestamp;
        private List<APITxDetails> apiTxDetails;
        private Integer sealType;
        private String seed;
        private String signature;
        private String publicKey;
        private boolean isMainChain;
        private String antiParentHash;
        private int blockTime;
        private String txTrieRoot;
        private BigInteger blockReward;
        private String gasLimit;
        private String gasUsed;
        private int numTransactions;

        public Builder setBlockTime(int blockTime) {
            this.blockTime = blockTime;
            return this;
        }

        public Builder setTxTrieRoot(String txTrieRoot) {
            this.txTrieRoot = txTrieRoot;
            return this;
        }

        public Builder setMainChain(Boolean mainChain) {
            isMainChain = mainChain;
            return this;
        }

        public Builder setAntiParentHash(String antiParentHash) {
            this.antiParentHash = antiParentHash;
            return this;
        }

        public Builder setSealType(Integer sealType) {
            this.sealType = sealType;

            return this;
        }

        public Builder setSeed(String seed) {
            this.seed = seed;
            return this;
        }

        public Builder setSignature(String signature) {
            this.signature = signature;
            return this;
        }

        public Builder setPublicKey(String publicKey) {

            this.publicKey = publicKey;
            return this;
        }

        public Builder setLogsBloom(String logsBloom) {
            this.logsBloom = logsBloom;
            return this;
        }

        public Builder setTotalDifficulty(String totalDifficulty) {
            this.totalDifficulty = longFromHexString(totalDifficulty);
            return this;
        }

        public Builder setReceiptsRoot(String receiptsRoot) {
            this.receiptsRoot = receiptsRoot;
            return this;
        }

        public Builder setExtraData(String extraData) {
            this.extraData = extraData;
            return this;
        }

        public Builder setNrgUsed(String nrgUsed) {
            this.nrgUsed = longFromHexString(nrgUsed);
            return this;
        }

        public Builder setNonce(String nonce) {
            this.nonce = nonce;
            return this;
        }

        public Builder setMiner(String miner) {
            this.miner = miner;
            return this;
        }

        public Builder setNumTransactions(int numTransactions){
            this.numTransactions = numTransactions;
            return this;
        }

        public Builder setDifficulty(String difficulty) {
            this.difficulty = longFromHexString(difficulty);
            return this;
        }

        public Builder setNumber(long number) {
            this.number = number;
            return this;
        }

        public Builder setNrgLimit(String nrgLimit) {
            this.nrgLimit = longFromHexString(nrgLimit);
            return this;
        }

        public Builder setSolution(String solution) {
            this.solution = solution;
            return this;
        }

        public Builder setSize(long size) {
            this.size = size;
            return this;
        }

        public Builder setTransactionsRoot(String transactionsRoot) {
            this.transactionsRoot = transactionsRoot;
            return this;
        }

        public Builder setStateRoot(String stateRoot) {
            this.stateRoot = stateRoot;
            return this;
        }

        public Builder setParentHash(String parentHash) {
            this.parentHash = parentHash;
            return this;
        }

        public Builder setHash(String hash) {
            this.hash = hash;
            return this;
        }

        public Builder setTimestamp(String timestamp) {
            this.timestamp = longFromHexString(timestamp);
            return this;
        }

        public Builder setTransactions(List<APITxDetails> apiTxDetails) {
            this.apiTxDetails = apiTxDetails;
            return this;
        }

        public Builder setGasLimit(String gasLimit) {
            this.gasLimit = gasLimit;
            return this;
        }

        public Builder setGasUsed(String gasUsed) {
            this.gasUsed = gasUsed;
            return this;
        }

        public Builder setBlockReward(String blockReward) {
            this.blockReward = bigIntegerFromHex(blockReward);
            return this;
        }

        public APIBlockDetails create(){
            return new APIBlockDetails(this);
        }
    }


}
