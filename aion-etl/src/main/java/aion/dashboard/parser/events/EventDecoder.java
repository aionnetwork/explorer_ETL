package aion.dashboard.parser.events;

import aion.dashboard.blockchain.ContractType;
import aion.dashboard.cache.CacheManager;
import aion.dashboard.domainobject.Contract;
import aion.dashboard.exception.InvalidContractException;
import aion.dashboard.service.ContractService;
import aion.dashboard.service.ContractServiceImpl;
import org.aion.api.type.TxLog;

import java.util.Objects;
import java.util.Optional;

public abstract class EventDecoder {



    private static ContractService service = ContractServiceImpl.getInstance();

    private static final CacheManager<String,Contract> CONTRACT_CACHE = CacheManager.getManager(CacheManager.Cache.CONTRACT);
    /**
     *
     * @param contractAddress the contract which triggered the event
     * @return the decoder to be used to decode the event
     */
    @SuppressWarnings("unused")
    public static EventDecoder decoderFor(String contractAddress){
        final Contract contract = findContract(contractAddress.replaceFirst("0x","")).orElseThrow(()->
                new InvalidContractException("Failed to find the contract in the cache or the database."));

        return decoderFor(contract.getContractType());
    }


    public static EventDecoder decoderFor(ContractType type){
        switch (type) {
            case DEFAULT:
                return FVMEventDecoder.DECODER_INSTANCE;
            case AVM:
                return AVMEventDecoder.DECODER_INSTANCE;
            default:
                throw new InvalidContractException("Attempted to decode an event for a contract with an unknown type.");
        }
    }



    private static Optional<Contract> findContract(String contractAddress) {
        if (CONTRACT_CACHE.contains(contractAddress)) return Optional.of(CONTRACT_CACHE.getIfPresent(contractAddress));
        else {
            try {
                Contract contract = Objects.requireNonNull(service.selectContractsByContractAddr(contractAddress));
                CONTRACT_CACHE.putIfAbsent(contractAddress, contract);
                return Optional.of(contract);
            } catch (Exception e) {
                return Optional.empty();
            }
        }
    }


    /**
     * Contracts should be registered by the parser to a
     * @param contract
     */
    public static void register(Contract contract) {
        CONTRACT_CACHE.putIfAbsent(contract.getContractAddr().replaceFirst("0x",""), contract);
    }

    public abstract Optional<ContractEvent> decodeEvent(TxLog txLog);

}
