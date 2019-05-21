package aion.dashboard.domainobject;

import aion.dashboard.util.ContractEvent;
import org.aion.api.type.BlockDetails;
import org.aion.api.type.TxDetails;
import org.checkerframework.checker.nullness.Opt;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class Event {
    private String name;//The name of the Event triggered
    private String parameterList;// the parameter list stored as a json object in the format ["to: Address", "from:Address", "value: uint"]
    private String inputList;// The list of input values
    private String transactionHash;// the hash of the transaction on which the event was fired
    private long blockNumber; // the block in which the event was fired
    private String contractAddr; // the address of the contract which fired the event
    private long timestamp;

    private Event(String name, String parameterList, String inputList, String transactionHash, long blockNumber, String contractAddr, long timestamp) {
        this.name = name;
        this.parameterList = parameterList;
        this.inputList = inputList;
        this.transactionHash = transactionHash;
        this.blockNumber = blockNumber;
        this.contractAddr = contractAddr;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public String getParameterList() {
        return parameterList;
    }

    public String getInputList() {
        return inputList;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public String getContractAddr() {
        return contractAddr;
    }

    public long getTimestamp() {
        return timestamp;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;
        Event event = (Event) o;
        return getBlockNumber() == event.getBlockNumber() &&
                getTimestamp() == event.getTimestamp() &&
                Objects.equals(getName(), event.getName()) &&
                Objects.equals(getParameterList(), event.getParameterList()) &&
                Objects.equals(getInputList(), event.getInputList()) &&
                Objects.equals(getTransactionHash(), event.getTransactionHash()) &&
                Objects.equals(getContractAddr(), event.getContractAddr());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getParameterList(), getInputList(), getTransactionHash(), getBlockNumber(), getContractAddr(), getTimestamp());
    }


    private static final ThreadLocal<EventBuilder> threadLocalBuilder = ThreadLocal.withInitial(EventBuilder::new);

    @SuppressWarnings("Duplicates")
    static Optional<Event> from(ContractEvent contractEvent, BlockDetails b, TxDetails tx){
        JSONArray inputList = new JSONArray();
        JSONArray paramList = new JSONArray();
        final EventBuilder eventBuilder = threadLocalBuilder.get().setName(contractEvent.getEventName())
                .setContractAddr(contractEvent.getAddress())
                .setTimestamp(b.getTimestamp())
                .setBlockNumber(b.getNumber())
                .setTransactionHash(tx.getTxHash().toString());

        List<String> names = contractEvent.getNames();
        List<String> types = contractEvent.getTypes();
        List<Object> inputs = contractEvent.getInputs();

        if (names.stream().noneMatch(s -> s.isBlank()||s.isEmpty())
                && types.stream().noneMatch(s -> s.isBlank()||s.isEmpty()) ) {// sanitize any bad events


            for (int i = 0; i < names.size(); i++) {
                paramList.put(types.get(i) + " " + names.get(i));
                inputList.put(inputs.get(i));
            }
            eventBuilder.setInputList(inputList.toString())
                    .setParameterList(paramList.toString());

            if (GENERAL.isTraceEnabled()) {
                GENERAL.trace("Built event: {}", contractEvent);
            }

            return Optional.ofNullable(eventBuilder.build());
        }else {
            return Optional.empty();
        }

    }
    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");

    public static List<Event> eventsFrom(List<ContractEvent> events, BlockDetails b, TxDetails tx){
        return events.stream()
                .map(event -> from(event, b, tx))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public static class EventBuilder {
        private String name;
        private String parameterList;// the parameter list stored as a json object in the format ["to: Address, from:Address, value: uint"]
        private String inputList;// The list of input values
        private String transactionHash;// the hash of the transaction on which the event was fired
        private long blockNumber; // the block in which the event was fired
        private String contractAddr; // the address of the contract which fired the event
        private long timestamp;


        public EventBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public EventBuilder setParameterList(String parameterList) {
            this.parameterList = parameterList;
            return this;
        }

        public EventBuilder setInputList(String inputList) {
            this.inputList = inputList;
            return this;
        }

        public EventBuilder setTransactionHash(String transactionHash) {
            this.transactionHash = transactionHash;
            return this;
        }

        public EventBuilder setBlockNumber(long blockNumber) {
            this.blockNumber = blockNumber;
            return this;
        }

        public EventBuilder setContractAddr(String contractAddr) {
            this.contractAddr = contractAddr;
            return this;
        }

        public Event build() {
            return new Event(name, parameterList, inputList, transactionHash, blockNumber, contractAddr, timestamp);
        }


        public EventBuilder setTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
    }
}
