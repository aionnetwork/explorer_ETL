package aion.dashboard.domainobject;

import aion.dashboard.parser.events.ContractEvent;
import org.aion.api.type.BlockDetails;
import org.aion.api.type.TxDetails;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static aion.dashboard.util.Utils.approximate;

public class InternalTransfer {
    private String fromAddr;//contractAddr
    private String toAddr;//The recipient
    private String transactionHash;//the hash this transfer occured
    private BigDecimal valueTransferred; // the amount transferred
    private long blockNumber; //block number
    private long timestamp;// timestamp of transfer
    private int transferCount;// the number of this transfer in the txlog... added to handle unlikely duplicates
    private double approxValue;

    public InternalTransfer(String fromAddr, String toAddr, String transactionHash, BigDecimal valueTransferred, long blockNumber, long timestamp, int transferCount) {
        this.fromAddr = fromAddr;
        this.toAddr = toAddr;
        this.transactionHash = transactionHash;
        this.valueTransferred = valueTransferred;
        this.blockNumber = blockNumber;
        this.timestamp = timestamp;
        this.transferCount = transferCount;

        approxValue = approximate(valueTransferred, 18);
    }

    public String getFromAddr() {
        return fromAddr;
    }

    public String getToAddr() {
        return toAddr;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public BigDecimal getValueTransferred() {
        return valueTransferred;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getTransferCount(){
        return transferCount;
    }

    public double getApproxValue() {
        return approxValue;
    }


    private static final ThreadLocal<InternalTransferBuilder> threadLocalBuilder = ThreadLocal.withInitial(InternalTransferBuilder::new);
    private static Optional<InternalTransfer> from(ContractEvent event, TxDetails tx, BlockDetails b, int transferCount) {
        try {
            return Optional.of( threadLocalBuilder.get().setBlockNumber(b.getNumber())
                    .setTransactionHash(tx.getTxHash().toString())
                    .setFromAddr(event.getAddress())
                    .setTimestamp(b.getTimestamp())
                    .setToAddr(getInput(event, String.class, "param1", "who"))
                    .setValueTransferred(new BigDecimal(getInput(event, BigInteger.class, "param2", "amount")))
                    .setTransferCount(transferCount)
                    .build());
        }
        catch (RuntimeException e){
            return Optional.empty();
        }

    }

    public static List<InternalTransfer> transfersFrom(List<ContractEvent> eventList, TxDetails tx, BlockDetails b){

        return IntStream.range(0, eventList.size())
                .mapToObj(i-> from(eventList.get(i), tx, b, i))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private static <T> T getInput( ContractEvent event, Class<T> type, String... names ){
        T ret = null;

        for (var name: names){
            Optional<T> res = event.getInput(name, type );
            if (res.isPresent()){
                ret= res.get();
                break;
            }
        }
        if (ret == null){
            throw new NoSuchElementException("Could not find the specified input");
        }
        else {
            return ret;
        }
    }

    public static class InternalTransferBuilder{

        private String fromAddr;
        private String toAddr;
        private String transactionHash;
        private BigDecimal valueTransferred;
        private long blockNumber;
        private long timestamp;
        private int transferCount;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof InternalTransferBuilder)) return false;
            InternalTransferBuilder that = (InternalTransferBuilder) o;
            return blockNumber == that.blockNumber &&
                    timestamp == that.timestamp &&
                    transferCount == that.transferCount &&
                    Objects.equals(fromAddr, that.fromAddr) &&
                    Objects.equals(toAddr, that.toAddr) &&
                    Objects.equals(transactionHash, that.transactionHash) &&
                    Objects.equals(valueTransferred, that.valueTransferred);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fromAddr, toAddr, transactionHash, valueTransferred, blockNumber, timestamp, transferCount);
        }

        public InternalTransferBuilder setFromAddr(String fromAddr) {
            this.fromAddr = fromAddr;
            return this;
        }

        public InternalTransferBuilder setToAddr(String toAddr) {
            this.toAddr = toAddr;
            return this;
        }

        public InternalTransferBuilder setTransactionHash(String transactionHash) {
            this.transactionHash = transactionHash;
            return this;
        }


        public InternalTransferBuilder setValueTransferred(BigDecimal valueTransferred) {
            this.valueTransferred = valueTransferred;
            return this;
        }

        public InternalTransferBuilder setBlockNumber(long blockNumber) {
            this.blockNumber = blockNumber;
            return this;
        }

        public InternalTransferBuilder setTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }


        public InternalTransfer build(){
            return new InternalTransfer(fromAddr, toAddr, transactionHash, valueTransferred, blockNumber, timestamp, transferCount);
        }

        public InternalTransferBuilder setTransferCount(int transferCount) {
            this.transferCount = transferCount;
            return this;
        }
    }
}

