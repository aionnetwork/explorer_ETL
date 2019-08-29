package aion.dashboard.consumer;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.domainobject.InternalTransaction;
import aion.dashboard.parser.type.AbstractBatch;
import aion.dashboard.parser.type.InternalTransactionBatch;
import aion.dashboard.service.InternalTransactionService;
import aion.dashboard.service.InternalTransactionServiceImpl;
import aion.dashboard.service.TransactionService;
import aion.dashboard.service.TransactionServiceImpl;

import java.sql.Connection;
import java.util.*;

public class InternalTransactionWriter implements WriteTask<InternalTransactionBatch> {

    private final InternalTransactionService internalTransactionService = InternalTransactionServiceImpl.getInstance();
    private final TransactionService transactionService = TransactionServiceImpl.getInstance();

    @Override
    public void write(InternalTransactionBatch records) throws Exception {

        Map<String, List<InternalTransaction>> internalTransactionMap = new HashMap<>();
        Set<Long> uniqueBlocks = new HashSet<>();
        for (var internalTx: records.getInternalTransactions()){
            if (!internalTransactionMap.containsKey(internalTx.getTransactionHash())){
                internalTransactionMap.put(internalTx.getTransactionHash(), new ArrayList<>());
            }
            if (!uniqueBlocks.contains(internalTx.getBlockNumber())){
                uniqueBlocks.add(internalTx.getBlockNumber());
            }
            internalTransactionMap.get(internalTx.getTransactionHash()).add(internalTx);
        }

        try(Connection connection = DbConnectionPool.getConnection()){
            try (var psInternalTx = internalTransactionService.prepare(connection, records.getInternalTransactions())){

                for (var entry: internalTransactionMap.entrySet()){
                    try(var psTxUpdate = transactionService.prepareInternalTransactionUpdate(connection, entry.getKey(), entry.getValue().size())){
                        psTxUpdate.execute();
                    }
                }
                for (long blockNumber: uniqueBlocks){
                    try(var psDelete = internalTransactionService.deleteExisting(blockNumber, connection)){
                        psDelete.execute();
                    }
                }
                psInternalTx.executeBatch();
                connection.commit();
            }
            catch (Exception e){
                connection.rollback();
                throw e;
            }
        }
    }
}
