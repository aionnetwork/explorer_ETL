package aion.dashboard.domainobject;

import aion.dashboard.blockchain.type.APIBlock;
import aion.dashboard.blockchain.type.APIBlockDetails;
import aion.dashboard.util.Utils;
import org.aion.api.type.BlockDetails;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.Objects;

import static aion.dashboard.util.Utils.getZDT;
import static aion.dashboard.util.Utils.toAion;

public class Block {

    private long blockNumber;
    private String blockHash;
    private String minerAddress;
    private String parentHash;
    private String receiptTxRoot;
    private String stateRoot;
    private String txTrieRoot;
    private String extraData;
    private String nonce;
    private String bloom;
    private String solution;
    private long difficulty;
    private long totalDifficulty;
    private long nrgConsumed;
    private long nrgLimit;
    private long blockSize;
    private long blockTimestamp;
    private long numTransactions;
    private long blockTime;
    private String transactionHashes;
    private String lastTransactionHash;
    private BigDecimal nrgReward;
    private int blockYear;
    private int blockMonth;
    private int blockDay;
    private double approxNrgReward;
    private BigDecimal blockReward;
    private String seed;
    private String signature;
    private String publicKey;
    private String sealType;

    public int getBlockYear() {
        return blockYear;
    }

    public int getBlockMonth() {
        return blockMonth;
    }

    public int getBlockDay() {
        return blockDay;
    }

    public double getApproxNrgReward() {
        return approxNrgReward;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block block = (Block) o;
        return blockNumber == block.blockNumber &&
                nrgConsumed == block.nrgConsumed &&
                nrgLimit == block.nrgLimit &&
                blockSize == block.blockSize &&
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
                Objects.equals(solution, block.solution) &&
                Objects.equals(difficulty, block.difficulty) &&
                Objects.equals(totalDifficulty, block.totalDifficulty) &&
                Objects.equals(transactionHashes, block.transactionHashes) &&
                Objects.equals(lastTransactionHash, block.lastTransactionHash) &&
                Objects.equals(nrgReward, block.nrgReward) &&
                Objects.equals(seed, block.seed) &&
                Objects.equals(publicKey, block.publicKey) &&
                Objects.equals(signature, block.signature) &&
                Objects.equals(blockReward, block.blockReward);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockNumber, blockHash, minerAddress, parentHash, receiptTxRoot, stateRoot, txTrieRoot, extraData, nonce, bloom, solution, difficulty, totalDifficulty, nrgConsumed, nrgLimit, blockSize, blockTimestamp, numTransactions, blockTime, lastTransactionHash, transactionHashes, nrgReward);
    }

    public String getLastTransactionHash() {
        return lastTransactionHash;
    }

    public void setLastTransactionHash(String lastTransactionHash) {
        this.lastTransactionHash = lastTransactionHash;
    }

    public String getTransactionHashes() {
        return transactionHashes;
    }



    public void setApproxNrgReward(double approxNrgReward) {
        this.approxNrgReward = approxNrgReward;
    }

    public void setTransactionHashes(String transactionHashes) {
        this.transactionHashes = transactionHashes;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public String getMinerAddress() {
        return minerAddress;
    }

    public void setMinerAddress(String minerAddress) {
        this.minerAddress = minerAddress;
    }

    public String getParentHash() {
        return parentHash;
    }

    public void setParentHash(String parentHash) {
        this.parentHash = parentHash;
    }

    public String getReceiptTxRoot() {
        return receiptTxRoot;
    }

    public void setReceiptTxRoot(String receiptTxRoot) {
        this.receiptTxRoot = receiptTxRoot;
    }

    public String getStateRoot() {
        return stateRoot;
    }

    public void setStateRoot(String stateRoot) {
        this.stateRoot = stateRoot;
    }

    public String getTxTrieRoot() {
        return txTrieRoot;
    }

    public void setTxTrieRoot(String txTrieRoot) {
        this.txTrieRoot = txTrieRoot;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getBloom() {
        return bloom;
    }

    public void setBloom(String bloom) {
        this.bloom = bloom;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public long getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(long difficulty) {
        this.difficulty = difficulty;
    }

    public long getTotalDifficulty() {
        return totalDifficulty;
    }

    public void setTotalDifficulty(long totalDifficulty) {
        this.totalDifficulty = totalDifficulty;
    }

    public long getNrgConsumed() {
        return nrgConsumed;
    }

    public void setNrgConsumed(long nrgConsumed) {
        this.nrgConsumed = nrgConsumed;
    }

    public long getNrgLimit() {
        return nrgLimit;
    }

    public void setNrgLimit(long nrgLimit) {
        this.nrgLimit = nrgLimit;
    }

    public long getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(long blockSize) {
        this.blockSize = blockSize;
    }

    public long getBlockTimestamp() {
        return blockTimestamp;
    }

    public void setBlockTimestamp(long blockTimestamp) {
        this.blockTimestamp = blockTimestamp;
        final ZonedDateTime zonedDateTime = getZDT(blockTimestamp);
        blockYear = zonedDateTime.getYear();
        blockMonth = zonedDateTime.getMonthValue();
        blockDay = zonedDateTime.getDayOfMonth();

    }

    public long getNumTransactions() {
        return numTransactions;
    }

    public void setNumTransactions(long numTransactions) {
        this.numTransactions = numTransactions;
    }

    public long getBlockTime() {
        return blockTime;
    }

    public void setBlockTime(long blockTime) {
        this.blockTime = blockTime;
    }

    public BigDecimal getNrgReward() {
        return nrgReward;
    }

    public void setNrgReward(BigDecimal nrgReward) {
        this.nrgReward = nrgReward;
    }

    @Override
    public String toString() {
        return "Block{" +
                "blockNumber=" + blockNumber +
                ", blockHash='" + blockHash + '\'' +
                ", minerAddress='" + minerAddress + '\'' +
                ", parentHash='" + parentHash + '\'' +
                ", receiptTxRoot='" + receiptTxRoot + '\'' +
                ", stateRoot='" + stateRoot + '\'' +
                ", txTrieRoot='" + txTrieRoot + '\'' +
                ", extraData='" + extraData + '\'' +
                ", nonce='" + nonce + '\'' +
                ", bloom='" + bloom + '\'' +
                ", solution='" + solution + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", totalDifficulty='" + totalDifficulty + '\'' +
                ", nrgConsumed=" + nrgConsumed +
                ", nrgLimit=" + nrgLimit +
                ", blockSize=" + blockSize +
                ", blockTimestamp=" + blockTimestamp +
                ", numTransactions=" + numTransactions +
                ", blockTime=" + blockTime +
                ", transactionHashes='" + transactionHashes + '\'' +
                ", lastTransactionHash='" + lastTransactionHash + '\'' +
                ", nrgReward=" + nrgReward +
                '}';
    }

    private static final ThreadLocal<BlockBuilder> threadLocalBuilder = ThreadLocal.withInitial(BlockBuilder::new);
    public static BlockBuilder getBuilder(){
        return threadLocalBuilder.get();
    }
   @Deprecated
    public static Block from(BlockDetails b, String lastHash, String txList, BigDecimal nrgReward, BigInteger blockReward){
        return threadLocalBuilder.get()
                .blockNumber(b.getNumber())
                .blockTime(b.getBlockTime())
                .blockHash(b.getHash().toString())
                .minerAddress(b.getMinerAddress().toString())
                .parentHash(b.getParentHash().toString())
                .receiptTxRoot(b.getReceiptTxRoot().toString())
                .stateRoot(b.getStateRoot().toString())
                .extraData(b.getExtraData().toString())
                .nonce(b.getNonce().toString(16))
                .bloom(b.getBloom().toString())
                .solution(b.getSolution().toString())
                .difficulty(b.getDifficulty().longValue())
                .totalDifficulty(b.getTotalDifficulty().longValue())
                .nrgConsumed(b.getNrgConsumed())
                .nrgLimit(b.getNrgLimit())
                .blockSize(b.getSize())
                .blockTimestamp(b.getTimestamp())
                .numTransactions(b.getTxDetails().size())
                .blockTime((b.getBlockTime()))
                .transactionHash(lastHash)
                .transactionList(txList)
                .nrgReward(nrgReward)
                .txTrieRoot(b.getTxTrieRoot().toString())
                .approxNrgReward(Utils.approximate(nrgReward,18))
                .blockReward(new BigDecimal(blockReward))
                .build();
    }


    public static Block from(APIBlockDetails b, String lastHash, String txList, BigDecimal nrgReward){
        return threadLocalBuilder.get()
                .blockNumber(b.getNumber())
                .blockTime(b.getBlockTime())
                .blockHash(Utils.sanitizeHex(b.getHash()))
                .minerAddress(Utils.sanitizeHex(b.getMiner()))
                .parentHash(Utils.sanitizeHex(b.getParentHash()))
                .receiptTxRoot(Utils.sanitizeHex(b.getReceiptsRoot()))
                .stateRoot(Utils.sanitizeHex(b.getStateRoot()))
                .extraData(Utils.sanitizeHex(b.getExtraData()))
                .nonce(Utils.sanitizeHex(b.getNonce()))
                .bloom(Utils.sanitizeHex(b.getBloom()))
                .solution(Utils.sanitizeHex(b.getSolution()))
                .difficulty(b.getDifficulty())
                .totalDifficulty(b.getTotalDifficulty())
                .nrgConsumed(b.getNrgUsed())
                .nrgLimit(b.getNrgLimit())
                .blockSize(b.getSize())
                .blockTimestamp(b.getTimestamp())
                .numTransactions(b.getTxDetails().size())
                .blockTime((b.getBlockTime()))
                .transactionHash(Utils.sanitizeHex(lastHash))
                .transactionList(txList)
                .nrgReward(nrgReward)
                .txTrieRoot(Utils.sanitizeHex(b.getTxTrieRoot()))
                .approxNrgReward(Utils.approximate(nrgReward,18))
                .blockReward(new BigDecimal(b.getBlockReward()))
                .seed(b.getSeed())
                .signature(b.getSignature())
                .publicKey(b.getPublicKey())
                .sealType(b.getSealType())
                .build();
    }

    public BigDecimal getBlockReward() {
        return blockReward;
    }

    public Block setBlockReward(BigDecimal blockReward) {
        this.blockReward = blockReward;
        return this;
    }

    public String getSeed() {
       return seed;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getSignature() {
        return signature;
    }

    public String getSealType() {
        return sealType;
    }

    public static class BlockBuilder {
        long blockNumber;
        String blockHash;
        String minerAddress;
        String parentHash;
        String receiptTxRoot;
        String stateRoot;
        String txTrieRoot;
        String extraData;
        String nonce;
        String bloom;
        String solution;
        long difficulty;
        long totalDifficulty;
        long nrgConsumed;
        long nrgLimit;
        long blockSize;
        long blockTimestamp;
        long numTransactions;
        long blockTime;
        String transactionHashes;
        String lastTransactionHash;
        BigDecimal nrgReward;
        double approxNrgReward;
        BigDecimal blockReward;
        String seed;
        String signature;
        String publicKey;
        String sealType;

        BlockBuilder sealType(APIBlock.SealType sealType){
            this.sealType = sealType.name();
            return this;
        }

        public BlockBuilder sealType(String sealType){
            this.sealType = sealType;
            return this;
        }

        public BlockBuilder seed(String seed){
            this.seed = seed;
            return this;
        }

        public BlockBuilder signature(String signature){
            this.signature = signature;
            return this;
        }

        public BlockBuilder publicKey(String publicKey){
            this.publicKey = publicKey;
            return this;
        }

        public BlockBuilder blockReward(BigDecimal blockReward) {
            this.blockReward = blockReward;
            return this;
        }

        public BlockBuilder approxNrgReward(double nrgReward){
            approxNrgReward = nrgReward;
            return this;
        }


        public BlockBuilder blockNumber(long blockNumber) {
            this.blockNumber = blockNumber;
            return this;
        }

        public BlockBuilder blockHash(String blockHash) {
            this.blockHash = blockHash;
            return this;
        }

        public BlockBuilder minerAddress(String minerAddress) {
            this.minerAddress = minerAddress;
            return this;
        }

        public BlockBuilder parentHash(String parentHash) {
            this.parentHash = parentHash;
            return this;
        }

        public BlockBuilder receiptTxRoot(String receiptTxRoot) {
            this.receiptTxRoot = receiptTxRoot;
            return this;
        }

        public BlockBuilder stateRoot(String stateRoot) {
            this.stateRoot = stateRoot;
            return this;
        }

        public BlockBuilder txTrieRoot(String txTrieRoot) {
            this.txTrieRoot = txTrieRoot;
            return this;
        }

        public BlockBuilder extraData(String extraData) {
            this.extraData = extraData;
            return this;
        }

        public BlockBuilder nonce(String nonce) {
            this.nonce = nonce;
            return this;
        }

        public BlockBuilder bloom(String bloom) {
            this.bloom = bloom;
            return this;
        }

        public BlockBuilder solution(String solution) {
            this.solution = solution;
            return this;
        }

        public BlockBuilder difficulty(long difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public BlockBuilder totalDifficulty(long totalDifficulty) {
            this.totalDifficulty = totalDifficulty;
            return this;
        }

        public BlockBuilder nrgConsumed(long nrgConsumed) {
            this.nrgConsumed = nrgConsumed;
            return this;
        }

        public BlockBuilder nrgLimit(long nrgLimit) {
            this.nrgLimit = nrgLimit;
            return this;
        }

        public BlockBuilder blockSize(long blockSize) {
            this.blockSize = blockSize;
            return this;
        }

        public BlockBuilder blockTimestamp(long blockTimestamp) {
            this.blockTimestamp = blockTimestamp;
            return this;
        }

        public BlockBuilder numTransactions(long numTransactions) {
            this.numTransactions = numTransactions;
            return this;
        }

        public BlockBuilder blockTime(long blockTime) {
            this.blockTime = blockTime;
            return this;
        }

        public BlockBuilder transactionHash(String lastTransactionHash) {
            this.lastTransactionHash = lastTransactionHash;
            return this;
        }

        public BlockBuilder transactionList(String transactionList) {
            this.transactionHashes = transactionList;
            return this;
        }

        public BlockBuilder nrgReward(BigDecimal nrgReward) {
            this.nrgReward = nrgReward;
            return this;
        }

        public Block build() {
            Block block = new Block();

            block.blockNumber = blockNumber;
            block.blockHash = blockHash;
            block.minerAddress = minerAddress;
            block.parentHash = parentHash;
            block.receiptTxRoot = receiptTxRoot;
            block.stateRoot = stateRoot;
            block.txTrieRoot = txTrieRoot;
            block.extraData = extraData;
            block.nonce = nonce;
            block.bloom = bloom;
            block.solution = solution;
            block.difficulty = difficulty;
            block.totalDifficulty = totalDifficulty;
            block.nrgConsumed = nrgConsumed;
            block.nrgLimit = nrgLimit;
            block.blockSize = blockSize;
            block.setBlockTimestamp(blockTimestamp);
            block.numTransactions = numTransactions;
            block.blockTime = blockTime;
            block.transactionHashes = transactionHashes;
            block.lastTransactionHash = lastTransactionHash;
            block.nrgReward = nrgReward;
            block.approxNrgReward = approxNrgReward;
            block.blockReward = blockReward;
            block.seed = seed;
            block.publicKey = publicKey;
            block.signature = signature;
            block.sealType = sealType;
            return block;
        }
    }
}