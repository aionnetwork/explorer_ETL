package aion.dashboard.blockchain.type;

import aion.dashboard.util.Utils;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.aion.api.type.Transaction;
import org.aion.util.bytes.ByteUtil;

import java.math.BigDecimal;
import java.math.BigInteger;


@JsonDeserialize(builder = APITransaction.APITransactionBuilder.class)
public class APITransaction {

    private BigInteger nrgPrice;
    private String blockHash;
    private long nrg;
    private int transactionIndex;
    private String nonce;
    private String input;
    private long blockNumber;
    private long gas;
    private String from;
    private String to;
    private BigDecimal value;
    private String hash;
    private BigInteger gasPrice;
    private long timestamp;
    private final String contractAddress;
    private String beaconHash;
    private String proof;

    private APITransaction(String nrgPrice, String blockHash, long nrg, int transactionIndex, String nonce, String input,
                           long blockNumber, long gas, String from, String to, String value, String hash,
                           String gasPrice, long timestamp, String contractAddress,String beaconHash,String proof) {

        this.nrgPrice = convertHex(nrgPrice);
        this.blockHash = Utils.sanitizeHex(blockHash);
        this.nrg = nrg;
        this.transactionIndex = transactionIndex;
        this.nonce = Utils.sanitizeHex(nonce);
        this.input = Utils.sanitizeHex(input);
        this.blockNumber = blockNumber;
        this.gas = gas;
        this.from = Utils.sanitizeHex(from);
        this.to = Utils.sanitizeHex(to);
        this.value = new BigDecimal(convertHex(value));
        this.hash = Utils.sanitizeHex(hash);
        this.gasPrice = convertHex(gasPrice);
        this.timestamp = timestamp;
        this.contractAddress = contractAddress;

        this.beaconHash=beaconHash;
        this.proof=proof;
    }

    @Override
    public String toString() {
        return "APITransaction{" +
                "nrgPrice=" + nrgPrice +
                ", blockHash='" + blockHash + '\'' +
                ", nrg=" + nrg +
                ", transactionIndex=" + transactionIndex +
                ", nonce='" + nonce + '\'' +
                ", input='" + input + '\'' +
                ", blockNumber=" + blockNumber +
                ", gas=" + gas +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", value=" + value +
                ", hash='" + hash + '\'' +
                ", gasPrice=" + gasPrice +
                ", timestamp=" + timestamp +
                '}';
    }

    public boolean compareTransactions(aion.dashboard.domainobject.Transaction transaction){
        return transaction != null && BigInteger.valueOf(transaction.getNrgPrice()).compareTo(this.nrgPrice) == 0 && Utils.compareStrings(transaction.getBlockHash(),(this.blockHash)) && Utils.compareStrings(transaction.getTransactionHash(),this.hash)
                && transaction.getTransactionIndex() == this.transactionIndex && Utils.compareStrings(transaction.getNonce(), this.nonce)
                && Utils.compareStrings(transaction.getFromAddr(),this.from) && Utils.compareStrings(transaction.getToAddr(),this.to)
                && Utils.compareStrings(transaction.getData(),this.input) && transaction.getBlockNumber() == this.blockNumber
                && transaction.getValue().compareTo(this.value)==0 && transaction.getBlockTimestamp() == timestamp;
    }
    private static BigInteger convertHex(String hex){
        byte[] bytes = ByteUtil.hexStringToBytes(hex);
        return ByteUtil.bytesToBigInteger(bytes);

    }

    private static ThreadLocal<APITransactionBuilder> threadLocalBuilder=  ThreadLocal.withInitial(APITransactionBuilder::new);

    public static APITransaction from(Transaction transaction){
        return threadLocalBuilder.get().setBlockHash(transaction.getBlockHash().toString())
                .setBlockNumber(String.valueOf(transaction.getBlockNumber()))
                .setFrom(transaction.getFrom().toString())
                .setTo(transaction.getTo().toString())
                .setGas(transaction.getNrgConsumed())
                .setGasPrice(Long.toString(transaction.getNrgPrice(),16))
                .setNrg(transaction.getNrgConsumed())
                .setNrgPrice(Long.toString(transaction.getNrgPrice(),16))
                .setHash(transaction.getTxHash().toString())
                .setInput(transaction.getData().toString())
                .setTransactionIndex(transaction.getTransactionIndex())
                .setValue(transaction.getValue().toString(16))
                .setNonce("0x"+transaction.getNonce().toString(16)).setContractAddress("").create();

    }

    public String getBeaconHash() {
        return beaconHash;
    }

    public String getProof() {
        return proof;
    }


    @JsonPOJOBuilder(buildMethodName = "create", withPrefix = "set")
    public static class APITransactionBuilder {
        private String nrgPrice;
        private String blockHash;
        private long nrg;
        private int transactionIndex;
        private String nonce;
        private String input;
        private long blockNumber;
        private long gas;
        private String from;
        private String to;
        private String value;
        private String hash;
        private String gasPrice;
        private long timestamp;
        private String contractAddress;
        private String beaconHash;
        private String proof;

        public APITransactionBuilder setNrgPrice(String nrgPrice) {
            this.nrgPrice = nrgPrice;
            return this;
        }

        public APITransactionBuilder setBlockHash(String blockHash) {
            this.blockHash = blockHash;
            return this;
        }

        public APITransactionBuilder setNrg(long nrg) {
            this.nrg = nrg;
            return this;
        }

        public APITransactionBuilder setTransactionIndex(int transactionIndex) {
            this.transactionIndex = transactionIndex;
            return this;
        }

        public APITransactionBuilder setNonce(String nonce) {
            if (nonce.startsWith("0x")){

                this.nonce = nonce;
            }
            else {
                this.nonce = "0x"+(new BigInteger(nonce, 10)).toString(16);
            }
            return this;
        }

        public APITransactionBuilder setInput(String input) {
            this.input = input;
            return this;
        }

        public APITransactionBuilder setBlockNumber(String blockNumber) {
            if (blockNumber.startsWith("0x")) {
                this.blockNumber = Long.parseLong(blockNumber.replaceFirst("0x",""),16);
            }
            else {
                this.blockNumber = Long.parseLong(blockNumber);
            }
            return this;
        }

        public APITransactionBuilder setGas(long gas) {
            this.gas = gas;
            return this;
        }

        public APITransactionBuilder setFrom(String from) {
            this.from = from;
            return this;
        }

        public APITransactionBuilder setTo(String to) {
            this.to = to;
            return this;
        }

        public APITransactionBuilder setValue(String value) {
            this.value = value;
            return this;
        }

        public APITransactionBuilder setHash(String hash) {
            this.hash = hash;
            return this;
        }

        public APITransactionBuilder setGasPrice(String gasPrice) {
            this.gasPrice = gasPrice;
            return this;
        }

        public APITransactionBuilder setTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public APITransaction create() {
            return new APITransaction(nrgPrice, blockHash, nrg, transactionIndex, nonce, input, blockNumber, gas, from, to, value, hash, gasPrice, timestamp, contractAddress,beaconHash, proof);
        }

        public APITransactionBuilder setContractAddress(String contractAddress) {
            this.contractAddress = contractAddress;
            return this;
        }

        public APITransactionBuilder setBeaconHash(String beaconHash) {
            this.beaconHash = beaconHash;
            return this;
        }
        public APITransactionBuilder setProof(String proof) {
            this.proof = proof;
            return this;
        }
    }
}
