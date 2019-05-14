package aion.dashboard.util;

import org.aion.util.bytes.ByteUtil;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Encapsulates the data extracted from a contract event
 */
final public class ContractEvent {

    private final String SignatureHash;
    private final String EventName;
    private final List<Object> Inputs;
    private final List<Class> classType;//TODO do we need this?
    private final List<String> Types;
    private final String Address;
    private final List<String> names;

    private ContractEvent(String signatureHash, String eventName, List<Object> inputs, List<Class> classType, List<String> types, List<String> names, String address) {
        SignatureHash = signatureHash;
        EventName = eventName;
        Inputs = inputs;
        this.classType = classType;
        Types = types;
        this.names = names;
        Address = address;
    }

    public static ContractEventBuilder builder() {
        return new ContractEventBuilder();
    }

    /**
     * Returns the input at the specified index if available, otherwise it returns an empty optional
     *
     * @param index The index of the input within the parameter list
     * @param <Any>
     * @return
     */
    public <Any> Optional<Any> getInput(int index, Class<Any> type) {
        try {

            if (index < Inputs.size() && Inputs.get(index).getClass().equals(type)) {
                return Optional.of(type.cast(Inputs.get(index)));
            }
            else if(index<Inputs.size() && Inputs.get(index) instanceof byte[] && type.equals(String.class) ) {
                return (Optional<Any>) Optional.of(ByteUtil.toHexString((byte[]) Inputs.get(index)));
            }
        } catch (Exception e) {
        }

        return Optional.empty();

    }

    /**
     * Get a named parameter within the event if it exists.
     *
     * @param eventName
     * @param type
     * @param <Any>
     * @return
     */
    public <Any> Optional<Any> getInput(String eventName, Class<Any> type) {

        int index = 0;
        for (; index < Inputs.size(); index++) {
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
        return SignatureHash;
    }

    public String getEventName() {
        return EventName;
    }

    public List<Object> getInputs() {
        return Inputs;
    }

    public List<Class> getClassType() {
        return classType;
    }

    public List<String> getTypes() {
        return Types;
    }

    public List<String> getNames() {
        return names;
    }

    public String getAddress() {
        return Address;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContractEvent)) return false;
        ContractEvent event = (ContractEvent) o;
        return SignatureHash.equals(event.SignatureHash) &&
                EventName.equals(event.EventName) &&
                Inputs.equals(event.Inputs) &&
                classType.equals(event.classType) &&
                Types.equals(event.Types) &&
                Address.equals(event.Address) &&
                names.equals(event.names);
    }

    @Override
    public int hashCode() {
        return Objects.hash(SignatureHash, EventName, Inputs, classType, Types, Address, names);
    }

    public static final class ContractEventBuilder {

        private String SignatureHash;
        private String EventName;
        private List<Object> Inputs;
        private List<Class> ClassType;
        private List<String> TypeName;
        private List<String> names;
        private String address;

        private ContractEventBuilder() {


        }


        public ContractEvent build() {
            if (Inputs.size() != ClassType.size() && Inputs.size() != names.size() || TypeName.size() != Inputs.size()) {
                throw new IllegalStateException(
                        String.format("Can't build class if list sizes differ:" +
                                        "%ninputs: %d" +
                                        "%nclassType: %d" +
                                        "%nnames: %d" +
                                        "%ntypeNames: %d",
                                Inputs.size(),
                                ClassType.size(),
                                names.size(),
                                TypeName.size()));
            }

            return new ContractEvent(SignatureHash, EventName, Inputs, ClassType, TypeName, names, address);
        }

        public ContractEventBuilder setEventName(String eventName) {
            EventName = eventName;
            return this;
        }

        public ContractEventBuilder setSignatureHash(String signatureHash) {
            SignatureHash = signatureHash;
            return this;
        }

        public ContractEventBuilder setInputs(List<Object> inputs) {
            Inputs = inputs;
            return this;
        }

        public ContractEventBuilder setClassType(List<Class> classType) {
            ClassType = classType;
            return this;
        }

        public ContractEventBuilder setTypeName(List<String> typeName) {
            TypeName = typeName;
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
