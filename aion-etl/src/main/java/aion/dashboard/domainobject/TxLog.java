package aion.dashboard.domainobject;

import aion.dashboard.blockchain.ContractType;
import aion.dashboard.blockchain.type.APITransactionLog;
import aion.dashboard.cache.CacheManager;
import aion.dashboard.service.ContractServiceImpl;
import aion.dashboard.util.Utils;
import org.aion.api.type.BlockDetails;
import org.aion.api.type.TxDetails;
import org.json.JSONArray;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TxLog {

    private static final ThreadLocal<TxLogBuilder> builder = ThreadLocal.withInitial(TxLogBuilder::new);
    private final String transactionHash;
    private final long blockNumber;
    private final long blockTimestamp;
    private final String topics;
    private final String data;
    private final String address;
    private final String from;
    private final String to;
    private final String contractType;
    private final int index;


    private TxLog(String transactionHash, long blockNumber, long blockTimestamp, String topics, String data, String address, String from, String to, String contractType, int index) {
        this.transactionHash = transactionHash;
        this.blockNumber = blockNumber;
        this.blockTimestamp = blockTimestamp;
        this.topics = topics;
        this.data = data;
        this.address = address;
        this.from = from;
        this.to = to;
        this.contractType = contractType;
        this.index = index;
    }

    public static TxLogBuilder builder() {
        return builder.get();
    }

    public static List<TxLog> logsFrom(BlockDetails blockDetails, TxDetails txDetails) {
        List<org.aion.api.type.TxLog> logs = txDetails.getLogs();
        return IntStream.range(0, logs.size())
                .mapToObj(i -> logFrom(blockDetails, txDetails, logs.get(i), i))
                .collect(Collectors.toList());
    }

    public static ThreadLocal<TxLogBuilder> getBuilder() {
        return builder;
    }

    private static TxLog logFrom(BlockDetails blockDetails, TxDetails tx, org.aion.api.type.TxLog log, int index) {

        return builder().setBlockNumber(blockDetails.getNumber())
                .setBlockTimestamp(blockDetails.getTimestamp())
                .setFrom(Utils.sanitizeHex(tx.getFrom().toString()))
                .setTransactionHash(Utils.sanitizeHex(tx.getTxHash().toString()))
                .setTxLog(log)
                .setIndex(index)
                .setTo(Utils.sanitizeHex(tx.getTo().toString()))
                .build();

    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public long getBlockTimestamp() {
        return blockTimestamp;
    }

    public String getTopics() {
        return topics;
    }

    public String getData() {
        return data;
    }

    public String getAddress() {
        return address;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getContractType() {
        return contractType;
    }

    public int getIndex() {
        return index;
    }


    public static class TxLogBuilder {
        private static final CacheManager<String, Contract> CONTRACT_CACHE = CacheManager.getManager(CacheManager.Cache.CONTRACT);
        private String transactionHash;
        private long blockNumber;
        private long blockTimestamp;
        private String topics;
        private String data;
        private String address;
        private String from;
        private String to;
        private String contractType;
        private int index;


        private TxLogBuilder() {
        }

        public TxLogBuilder setTransactionHash(String transactionHash) {
            this.transactionHash = transactionHash;
            return this;
        }

        public TxLogBuilder setBlockNumber(long blockNumber) {
            this.blockNumber = blockNumber;
            return this;
        }

        public TxLogBuilder setBlockTimestamp(long blockTimestamp) {
            this.blockTimestamp = blockTimestamp;
            return this;
        }

        @SuppressWarnings("Duplicates")
        public TxLogBuilder setTxLog(org.aion.api.type.TxLog log) {
            address = log.getAddress().toString();
            data = log.getData().toString();
            JSONArray jsonArray = new JSONArray(log.getTopics());
            topics = jsonArray.toString();

            contractType = ContractServiceImpl.getInstance()
                    .findContract(address)
                    .map(c -> c.getContractType().type)
                    .orElse(ContractType.DEFAULT.type);
            return this;
        }

        @SuppressWarnings("Duplicates")
        public TxLogBuilder setTxLog(APITransactionLog log) {
            address = log.getAddress().toString();
            data = log.getData().toString();
            JSONArray jsonArray = new JSONArray(log.getTopics());
            topics = jsonArray.toString();
            index = log.getLogIndex();
            contractType = ContractServiceImpl.getInstance()
                    .findContract(address)
                    .map(c -> c.getContractType().type)
                    .orElse(ContractType.DEFAULT.type);
            return this;
        }

        public TxLogBuilder setFrom(String from) {
            this.from = from;
            return this;
        }

        public TxLogBuilder setTo(String to) {
            this.to = to;
            return this;
        }

        public TxLogBuilder setIndex(int index) {
            this.index = index;
            return this;
        }

        public TxLog build() {
            return new TxLog(transactionHash, blockNumber, blockTimestamp, topics, data, address, from, to, contractType, index);
        }


    }
}
