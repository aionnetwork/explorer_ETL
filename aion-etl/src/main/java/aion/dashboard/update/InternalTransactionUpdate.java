package aion.dashboard.update;

import aion.dashboard.blockchain.interfaces.Web3Service;
import aion.dashboard.blockchain.type.APIBlock;
import aion.dashboard.blockchain.type.APIInternalTransaction;
import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.domainobject.Block;
import aion.dashboard.domainobject.InternalTransaction;
import aion.dashboard.domainobject.UpdateState;
import aion.dashboard.service.*;
import org.aion.api.sol.impl.Int;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class InternalTransactionUpdate extends UpdateTask<InternalTransaction>{
    private final Web3Service web3Service;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final InternalTransactionService internalTransactionService;
    private final TransactionService transactionService;

    protected InternalTransactionUpdate(Web3Service web3Service, InternalTransactionService internalTransactionService, TransactionService transactionService) {
        super(2);
        this.web3Service = web3Service;
        this.internalTransactionService = internalTransactionService;
        this.transactionService = transactionService;
    }

    protected InternalTransactionUpdate(Web3Service web3Service) {
        this(web3Service, InternalTransactionServiceImpl.getInstance(), TransactionServiceImpl.getInstance());
    }

    @Override
    protected List<InternalTransaction> readForBlock(long block, long end) {


        return LongStream.rangeClosed(block,end)
                .sequential()
                .mapToObj(blockNumber -> CompletableFuture.supplyAsync(()-> readForBlock(blockNumber), executor))
                .map(CompletableFuture::join)
                .flatMap(s->s)
                .collect(Collectors.toList());
    }


    private Stream<InternalTransaction> readForBlock(long blockNumber) {
        try {
            APIBlock block = web3Service.getBlock(blockNumber);
            if (block.getTransactions().isEmpty()) {
                return Stream.empty();
            } else {
                List<InternalTransaction> internalTransactions = new ArrayList<>();

                for (String txHash : block.getTransactions()) {
                    List<APIInternalTransaction> tmp = web3Service.getInternalTransaction(txHash);
                    for (int i = 0; i < tmp.size(); i++) {
                        internalTransactions.add(InternalTransaction.from(tmp.get(i), txHash, i, blockNumber, block.getTimestamp()));
                    }
                }

                return internalTransactions.stream();
            }
        }catch (Exception e){
            GENERAL.debug("Caught an exception while extracting internal transactions: ", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void writeUpdate(UpdateState state, List<InternalTransaction> internalTransactions) throws Exception {
        Map<String, List<InternalTransaction>> internalTransactionMap = new HashMap<>();
        for (var internalTx: internalTransactions){
            if (!internalTransactionMap.containsKey(internalTx.getTransactionHash())){
                internalTransactionMap.put(internalTx.getTransactionHash(), new ArrayList<>());
            }
            internalTransactionMap.get(internalTx.getTransactionHash()).add(internalTx);
        }
        try(Connection connection = DbConnectionPool.getConnection()){
            try (var psInternalTx = internalTransactionService.prepare(connection, internalTransactions);
                var psUpdateService = updateStateService.update(connection, state)){

                for (var entry: internalTransactionMap.entrySet()){
                    try(var psTxUpdate = transactionService.prepareInternalTransactionUpdate(connection, entry.getKey(), entry.getValue().size())){
                        psTxUpdate.execute();
                    }
                }

                psInternalTx.executeBatch();
                psUpdateService.executeBatch();
                connection.commit();
            }
            catch (Exception e){
                connection.rollback();
                throw e;
            }
        }
    }
}
