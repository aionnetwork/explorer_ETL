package aion.dashboard.blockchain;

import org.aion.api.type.BlockDetails;
import org.aion.api.type.TxDetails;
import org.aion.base.type.Address;
import org.aion.base.type.Hash256;
import org.aion.base.util.ByteArrayWrapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.math3.random.RandomDataImpl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Programmatically mock data from the API
 */
public class MockDataGeneratorImpl implements MockDataGenerator {


    long difficulty;
    long lastBlockNum;
    long currDifficulty;
    long numTx;
    long numBlks;



    long nrgConsumed;
    long nrgLimit;
    Hash256 parentHash;
    long value;
    long nrgPrice;
    long lastTxNum;

    public MockDataGeneratorImpl(Builder builder) {
        this.difficulty = builder.difficulty;
        this.lastBlockNum = builder.lastBlockNum;
        this.currDifficulty = builder.currDifficulty;
        this.numTx = builder.numTx;
        this.numBlks = builder.numBlks;
        this.nrgConsumed = builder.nrgConsumed;
        this.nrgLimit = builder.nrgLimit;
        this.parentHash = builder.parentHash;
        this.value = builder.value;
        this.nrgPrice = builder.nrgPrice;
        this.lastTxNum = builder.lastTxNum;
    }

    @Override
    public List<BlockDetails> mockBlockDetailsF() throws Exception {
        return generateBlocks();
    }
    
    
    private List<BlockDetails> generateBlocks(){

        List<BlockDetails> list = new ArrayList<>();
        BlockDetails.BlockDetailsBuilder builder = new BlockDetails.BlockDetailsBuilder();
        for (var i = 0 ; i <= numBlks ; i++){
            String seed = DigestUtils.sha256Hex(RandomStringUtils.random(32));
            if (parentHash == null) throw new Error("Fatal Error occurred");
            builder.number(lastBlockNum + 1)
                    .timestamp(System.currentTimeMillis())
                    .nrgConsumed(nrgConsumed)
                    .nrgLimit(nrgLimit)
                    .bloom(new ByteArrayWrapper(new byte[]{}))
                    .extraData(new ByteArrayWrapper(new byte[]{}))
                    .solution(new ByteArrayWrapper(new byte[]{}))
                    .txDetails(generateTx())
                    .parentHash(parentHash)
                    .hash(Hash256.wrap(seed))
                    .difficulty(new BigInteger(String.valueOf(difficulty)))
                    .totalDifficulty(new BigInteger(String.valueOf(currDifficulty + difficulty)))
                    .miner(new Address(new byte[]{}))
                    .stateRoot(new Hash256(DigestUtils.sha256Hex(RandomStringUtils.random(32)) ))
                    .receiptTxRoot(new Hash256(DigestUtils.sha256Hex(RandomStringUtils.random(32))))
                    .size(32)
                    .blockTime(10)
                    .nonce(new BigInteger(RandomStringUtils.randomNumeric(32)))
                    .txTrieRoot(new Hash256(DigestUtils.sha256Hex(RandomStringUtils.random(32))));





            currDifficulty = currDifficulty + difficulty;
            parentHash = Hash256.wrap(seed);
            lastBlockNum = lastBlockNum + 1;


            list.add(builder.createBlockDetails());
        }


        System.out.println(list.size());
        return list;

    }
    
    private List<TxDetails> generateTx(){
        List<TxDetails> txDetails = new ArrayList<>();
        TxDetails.TxDetailsBuilder builder = new TxDetails.TxDetailsBuilder();
        for (var i = 0; i <= numTx; i++){
            String seed  = DigestUtils.sha256Hex(RandomStringUtils.random(32));

            builder.error("")
                    .from(new Address(DigestUtils.sha256(RandomStringUtils.random(32))))
                    .to(new Address(DigestUtils.sha256(RandomStringUtils.random(32))))
                    .txHash(new Hash256(DigestUtils.sha256(RandomStringUtils.random(32))))
                    .value(new BigInteger(String.valueOf(value)))
                    .data(new ByteArrayWrapper(new byte[]{}))
                    .logs(List.of())
                    .timestamp(System.nanoTime())
                    .nrgPrice(nrgPrice)
                    .nrgConsumed(10)
                    .contract(new Address(""))
                    .txIndex(Math.toIntExact(lastTxNum))
                    .nonce(new BigInteger(RandomStringUtils.randomNumeric(32)))
                    .txHash(Hash256.wrap(seed));
            lastTxNum = lastTxNum+1;
        }


        return txDetails;
    }

    public static Builder builder (){return new Builder();}

    static class Builder{
        long difficulty;
        long lastBlockNum;
        long currDifficulty;
        long numTx;
        long numBlks;
        long nrgConsumed;
        long nrgLimit;
        Hash256 parentHash;
        long value;
        long nrgPrice;
        long lastTxNum;

        public Builder setDifficulty(long difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public Builder setLastBlockNum(long lastBlockNum) {
            this.lastBlockNum = lastBlockNum;
            return this;
        }

        public Builder setCurrDifficulty(long currDifficulty) {
            this.currDifficulty = currDifficulty;
            return this;
        }

        public Builder setNumTx(long numTx) {
            this.numTx = numTx;
            return this;
        }

        public Builder setNumBlks(long numBlks) {
            this.numBlks = numBlks;
            return this;
        }

        public Builder setNrgConsumed(long nrgConsumed) {
            this.nrgConsumed = nrgConsumed;
            return this;
        }

        public Builder setNrgLimit(long nrgLimit) {
            this.nrgLimit = nrgLimit;
            return this;
        }

        public Builder setParentHash(Hash256 parentHash) {
            this.parentHash = parentHash;
            return this;
        }

        public Builder setValue(long value) {
            this.value = value;
            return this;
        }

        public Builder setNrgPrice(long nrgPrice) {
            this.nrgPrice = nrgPrice;
            return this;
        }

        public Builder setLastTxNum(long lastTxNum) {
            this.lastTxNum = lastTxNum;
            return this;
        }


        public MockDataGeneratorImpl build(){
            return new MockDataGeneratorImpl(this);
        }
    }



    
 }
