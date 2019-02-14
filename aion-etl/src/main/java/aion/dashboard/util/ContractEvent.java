package aion.dashboard.util;

import org.aion.base.util.ByteUtil;

import java.util.List;
import java.util.Optional;

/**
 * Encapsulates the data extracted from a contract event
 */
public final class ContractEvent {

    private final String signatureHash;
    private final String eventName;
    private final List<Object> inputs;
    private final List<Class> classType;//TODO do we need this?
    private final List<String> types;
    private final String contractAddress;
    private final List<String> names;

    private ContractEvent(String signatureHash, String eventName, List<Object> inputs, List<Class> classType, List<String> types, List<String> names, String address) {
        this.signatureHash = signatureHash;
        this.eventName = eventName;
        this.inputs = inputs;
        this.classType = classType;
        this.types = types;
        this.names = names;
        this.contractAddress = address;
    }

    public static ContractEventBuilder builder() {
        return new ContractEventBuilder();
    }

    /**
     * Returns the input at the specified index if available, otherwise it returns an empty optional
     *
     * @param index The index of the input within the parameter list
     * @param <T>
     * @return
     */
    public <T> Optional<T> getInput(int index, Class<T> type) {
        try {

            if (index < inputs.size() && inputs.get(index).getClass().equals(type)) {
                return Optional.of(type.cast(inputs.get(index)));
            }
            else if(index< inputs.size() && inputs.get(index) instanceof byte[] && type.equals(String.class) ) {
                return (Optional<T>) Optional.of(ByteUtil.toHexString((byte[]) inputs.get(index)));
            }
            else {
                return Optional.empty();
            }

        } catch (Exception e) {

            return Optional.empty();

        }

    }

    /**
     * Get a named parameter within the event if it exists.
     *
     * @param eventName
     * @param type
     * @param <T>
     * @return
     */
    public <T> Optional<T> getInput(String eventName, Class<T> type) {


        for (int index = 0; index < inputs.size(); index++) {
            if (names.get(index).equals(eventName)) {
                return getInput(index, type);
            }
        }


        return Optional.empty();
    }

    /**
     * Returns the type at a specified index
     *
     * @param index the index of the parameter to get the type of
     * @return
     */
    public Class getType(int index) {
        return classType.get(index);
    }

    public String getSignatureHash() {
        return signatureHash;
    }

    public String getEventName() {
        return eventName;
    }

    public List<Object> getInputs() {
        return inputs;
    }

    public List<Class> getClassType() {
        return classType;
    }

    public List<String> getTypes() {
        return types;
    }

    public List<String> getNames() {
        return names;
    }

    /**
     *
     * @return The contract address of the contract that fired this event
     */
    public String getAddress() {
        return contractAddress;
    }

    public static final class ContractEventBuilder {

        private String signatureHash;
        private String eventName;
        private List<Object> inputs;
        private List<Class> classType;
        private List<String> typeName;
        private List<String> names;
        private String address;

        private ContractEventBuilder() {


        }


        public ContractEvent build() {
            if (inputs.size() != classType.size() && inputs.size() != names.size() || typeName.size() != inputs.size()) {
                throw new IllegalStateException(
                        String.format("Can't build class if list sizes differ:" +
                                        "%ninputs: %d" +
                                        "%nclassType: %d" +
                                        "%nnames: %d" +
                                        "%ntypeNames: %d",
                                inputs.size(),
                                classType.size(),
                                names.size(),
                                typeName.size()));
            }

            return new ContractEvent(signatureHash, eventName, inputs, classType, typeName, names, address);
        }

        public ContractEventBuilder setEventName(String eventName) {
            this.eventName = eventName;
            return this;
        }

        public ContractEventBuilder setSignatureHash(String signatureHash) {
            this.signatureHash = signatureHash;
            return this;
        }

        public ContractEventBuilder setInputs(List<Object> inputs) {
            this.inputs = inputs;
            return this;
        }

        public ContractEventBuilder setClassType(List<Class> classType) {
            this.classType = classType;
            return this;
        }

        public ContractEventBuilder setTypeName(List<String> typeName) {
            this.typeName = typeName;
            return this;
        }

        public ContractEventBuilder setNames(List<String> names) {
            this.names = names;
            return this;
        }

        public ContractEventBuilder setAddress(String address) {
            this.address = address;
            return this;
        }
    }
}
