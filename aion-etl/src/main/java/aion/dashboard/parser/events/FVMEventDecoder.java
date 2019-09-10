package aion.dashboard.parser.events;

import aion.dashboard.blockchain.type.APITransactionLog;
import aion.dashboard.util.Utils;
import org.aion.api.type.ContractAbiEntry;

import java.util.*;

public class FVMEventDecoder extends EventDecoder {


    static final FVMEventDecoder DECODER_INSTANCE = new FVMEventDecoder();
    private final Map<String, List<ContractAbiEntry>> entryMap;

    public FVMEventDecoder() {
        var abis = SolABIDefinitions.getInstance().getAllEvents();
        entryMap = new HashMap<>();
        for(var abi: abis){

            if (entryMap.containsKey(abi.getHashed())){
                entryMap.get(abi.getHashed()).add(abi);
            }
            else {
                List<ContractAbiEntry> entries = new ArrayList<>();
                entries.add(abi);
                entryMap.put(abi.getHashed(), entries);
            }

        }
    }

    @Override
    public Optional<ContractEvent> decodeEvent(APITransactionLog txLog) {
        if (txLog.getTopics().isEmpty() || !entryMap.containsKey(Utils.sanitizeHex(txLog.getTopics().get(0)))){
            return Optional.empty();
        }else {
            var entries = entryMap.get(Utils.sanitizeHex(txLog.getTopics().get(0)));

            for (var entry: entries){
                try{
                    return ContractEvents.decodeEventLog(txLog, entry);
                }catch (Exception e){
                    //just continue looping
                }

            }

            return Optional.empty();
        }
    }



}
