package aion.dashboard.parser.events;

import aion.dashboard.blockchain.ContractType;
import aion.dashboard.cache.CacheManager;
import aion.dashboard.domainobject.Contract;
import aion.dashboard.exception.InvalidContractException;
import aion.dashboard.service.ContractService;
import aion.dashboard.service.ContractServiceImpl;
import aion.dashboard.util.Utils;
import org.aion.api.type.TxLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

public abstract class EventDecoder {



    private static ContractService service = ContractServiceImpl.getInstance();
    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");

    private static final CacheManager<String,Contract> CONTRACT_CACHE = CacheManager.getManager(CacheManager.Cache.CONTRACT);
    /**
     *
     * @param contractAddress the contract which triggered the event
     * @return the decoder to be used to decode the event
     */
    public static EventDecoder decoderFor(String contractAddress) {
        try {
            final ContractType contractType;
            if (Utils.sanitizeHex(contractAddress).equalsIgnoreCase("0000000000000000000000000000000000000000000000000000000000000200"))
                contractType = ContractType.DEFAULT;
            else
                contractType = findContract(Utils.sanitizeHex(contractAddress)).map(Contract::getContractType).orElseThrow(() -> new InvalidContractException("Failed to find contract in the database"));

            return decoderFor(contractType);
        }catch (Exception e){
            GENERAL.error("Error finding decoder for contract: {}", contractAddress);
            throw e;
        }
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
