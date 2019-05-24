package aion.dashboard.parser;

import aion.dashboard.domainobject.*;
import aion.dashboard.parser.events.EventDecoder;
import aion.dashboard.exception.DecodeException;
import aion.dashboard.parser.events.AVMABIDefinitions;
import aion.dashboard.parser.events.SolABIDefinitions;
import aion.dashboard.parser.events.ContractEvent;
import aion.dashboard.parser.type.Message;
import aion.dashboard.parser.type.ParserBatch;
import org.aion.api.type.BlockDetails;
import org.aion.api.type.ContractAbiEntry;
import org.aion.api.type.TxDetails;
import org.aion.api.type.TxLog;
import org.aion.base.util.Utils;
import org.aion.mcf.vm.types.Bloom;
import org.aion.util.bytes.ByteUtil;
import org.aion.zero.impl.core.BloomFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static aion.dashboard.parser.events.ContractEvents.decodeEventLog;

public class Parsers {

    protected static final Logger GENERAL= LoggerFactory.getLogger("logger_general");
    //create predicates to be used when identifying a token
    static final Predicate<String> STRING_RESPONSE_EXISTS = s -> !s.isBlank();
    static final Predicate<BigInteger> NUM_RESPONSE_EXISTS = e -> e.compareTo(BigInteger.ZERO) > 0;

    private Parsers() {
        throw new UnsupportedOperationException("Cannot create an instance of: "+Parsers.class);
    }

    /**
     * Checks for the existence of any readable events within a block
     *
     * @param bloomBytes the bloom from the block
     * @return whether the block may contain the event
     */
    static boolean containsReadableEvent(byte[] bloomBytes) {
        bloomBytes = Arrays.copyOf(bloomBytes, bloomBytes.length);// Copying byte array for sanity reasons
        Bloom bloom = new Bloom(bloomBytes);
        var avmHashes=AVMABIDefinitions.getInstance().getAllHashes().stream();


        var entries = SolABIDefinitions.getInstance().getAllEvents().stream().map(ContractAbiEntry::getHashed);

        return Stream.concat(entries, avmHashes).parallel().map(ByteUtil::hexStringToBytes)
                .anyMatch(entry -> BloomFilter.containsEvent(bloom, entry));
    }

    static List<ContractEvent> readEvents(List<TxLog> logs){
        return logs.stream().map(log -> EventDecoder.decoderFor(log.getAddress().toString()).decodeEvent(log))// Attempt to decode this element in the txlog
                .filter(Optional::isPresent)
                .map(Optional::get)//Get the decoded event
                .collect(Collectors.toList());// return the result
    }

    private static Optional<ContractEvent> readEvent(TxLog log){
        List<ContractAbiEntry> entries = SolABIDefinitions.getInstance().getAllEvents();// get the events that can be decoded
        for (var entry : entries) {
            try {
                var optionalContractEvent = decodeEventLog(log, entry);// attempt to decode the event


                if (optionalContractEvent.isPresent()) {
                    return optionalContractEvent;// return if the attempt succeeded
                }

            } catch (DecodeException e) {
                GENERAL.trace("Failed to identify event: ",e);
            }

        }
        return Optional.empty();// else return nothing
    }

    static Optional<Contract> readContract(TxDetails tx, BlockDetails b) {
        if (tx.getContract() == null || tx.getContract().isEmptyAddress()) {
            return Optional.empty();
        } else {

            return Optional.ofNullable(Contract.from(b, tx));
        }
    }



    static boolean containsTokenEvent(List<ContractEvent> events) {
        for (var event: events){
            if (checkForEventSol(event.getSignatureHash(), SolABIDefinitions.ATS_CONTRACT) ||
                    checkForEventAVM(event.getSignatureHash(), AVMABIDefinitions.ATS_CONTRACT)){
                return true;
            }

        }
        return false;
    }

    @SuppressWarnings("SameParameterValue")
    private static boolean checkForEventSol(String topic, String contract){
        return SolABIDefinitions.getInstance().getABI(contract)
                .parallelStream()
                .anyMatch(e -> e.getHashed().replace("0x", "").equals(topic.replace("0x", "")));
    }


    private static boolean checkForEventAVM(String topic, String contract){
        return AVMABIDefinitions.getInstance()
                .signatureForContract(contract)
                .parallelStream()
                .anyMatch(e->e.getHashed().replace("0x", "").equals(topic.replace("0x", "")));
    }

    static List<InternalTransfer> readInternalTransfer(List<ContractEvent> events, TxDetails txDetails, BlockDetails blockDetails){
        return InternalTransfer.transfersFrom(filterInternalTransferEvents(events), txDetails, blockDetails);
    }

    /**
     *
     * @param events the list of events read in this transaction log
     * @return whether this log contains any internal transfer events
     */
    static boolean containsInternalTransfer(List<ContractEvent> events){
        return events.stream().anyMatch(event -> event.getEventName().equals("Distributed") || event.getEventName().equals("Withdraws"));
    }

    /**
     *
     * @param events the list of events that contain an internal transfer
     * @return Only the events that match an internal transfer
     */
    private static List<ContractEvent> filterInternalTransferEvents(List<ContractEvent> events){
        return events.stream()
                .filter(event -> event.getEventName().equals("Distributed")
                        || event.getEventName().equals("Withdraws"))// get only the events that contain an internal transfer
                .collect(Collectors.toList());
    }

    static String getFirstTxHash(List<TxDetails> txDetails) {
        //If no tx are found in this block return an empty string
        //Otherwise find the last tx and return the hash
        //This should probably be removed in a future release
        return txDetails.isEmpty() ? "" : txDetails.get(0).getTxHash().toString();
    }

    static Collection<String> accFromTransaction(TxDetails transaction) {
        Set<String> addresses = new HashSet<>();

        if (transaction.getContract() !=null &&!transaction.getContract().isEmptyAddress()) {
            addresses.add(transaction.getContract().toString());
        }

        if (!transaction.getTo().isEmptyAddress()) {
            addresses.add(transaction.getTo().toString());
        }


        addresses.add(transaction.getFrom().toString());


        for (var log : transaction.getLogs()) {
            addresses.add(log.getAddress().toString());

            for (var topic : log.getTopics()) {
                if (Utils.isValidAddress(topic)) {
                    addresses.add(topic);
                }
            }
        }

        return addresses;
    }

    static List<TokenTransfers> getTokenTransfers(List<ContractEvent> events, BlockDetails b, TxDetails tx, Token tkn) {
        return TokenTransfers.tokenTransfersFrom(events, tx, b, tkn);
    }

    static boolean isSupplyUpdate(List<ContractEvent> events){
        return events.stream().anyMatch(event1->
                event1.getEventName().equals("Froze") ||
                event1.getEventName().equals("Thawed") ||
                event1.getEventName().equals("Minted") ||
                event1.getEventName().equals("Burned")
        );
    }

    static List<String> getInputs(ContractEvent event, String... params){
        return Arrays.stream(params).map(param -> event.getInput(param, String.class)).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

    static void parseEvents(ParserBatch batchObject, BlockDetails block, List<Message<ContractEvent>> tokenMessages, boolean canRead, TxDetails tx) {
        if (canRead) {
            //Attempt to do fun stuff with the events
            List<ContractEvent> events = readEvents(tx.getLogs());

            batchObject.addEvents(Event.eventsFrom(events, block, tx));


            if (containsInternalTransfer(events)) {
                //Attempt Read internal transfers
                batchObject.addTransfers(readInternalTransfer(events, tx, block));
            } else if (containsTokenEvent(events)) {
                tokenMessages.add(new Message<>(events, block, tx));
            }

        }
    }
}
