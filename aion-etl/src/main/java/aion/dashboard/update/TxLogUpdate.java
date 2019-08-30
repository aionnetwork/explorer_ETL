package aion.dashboard.update;

import aion.dashboard.blockchain.interfaces.APIService;
import aion.dashboard.blockchain.type.APIBlock;
import aion.dashboard.blockchain.type.APITransactionLog;
import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.domainobject.TxLog;
import aion.dashboard.domainobject.UpdateState;
import aion.dashboard.service.TxLogService;
import aion.dashboard.service.TxLogServiceImpl;
import aion.dashboard.service.UpdateStateService;
import aion.dashboard.service.UpdateStateServiceImpl;
import aion.dashboard.util.Utils;
import jdk.jshell.execution.Util;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class TxLogUpdate extends UpdateTask<TxLog> {
    private final APIService service;
    private final TxLogService txLogService = TxLogServiceImpl.getInstance();
    private final ExecutorService workers = Executors.newFixedThreadPool(2);

    protected TxLogUpdate(APIService service) {
        super(1);
        this.service = service;
    }

    @Override
    protected List<TxLog> readForBlock(long blockNumber, long end) throws Exception {

        var futures = LongStream.range(blockNumber, end + 1).mapToObj(i-> CompletableFuture.supplyAsync(() -> readForBlock(i), workers)).collect(Collectors.toList());

        return futures.stream().map(CompletableFuture::join).flatMap(Collection::stream).collect(Collectors.toList());
    }


    private List<TxLog> readForBlock(long blockNumber){

        try{
            APIBlock block=service.getBlock(blockNumber);

            List<TxLog> txLogs = new ArrayList<>();
            for (var hash: block.getTransactions()){
                var receipt = service.getTransactionReceipt(hash);

                if (receipt != null) {
                    List<APITransactionLog> logs = receipt.getLogs();
                    for (APITransactionLog log : logs) {
                        txLogs.add(TxLog
                                .builder()
                                .setTxLog(log)
                                .setBlockTimestamp(block.getTimestamp())
                                .setBlockNumber(block.getNumber())
                                .setTransactionHash(hash)
                                .setFrom(Utils.sanitizeHex(receipt.getFrom()))
                                .setTo(Utils.sanitizeHex(receipt.getTo()))
                                .build()
                        );

                    }
                }
            }
            return txLogs;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }


    @Override
    protected void writeUpdate(UpdateState state, List<TxLog> logs) throws Exception {
        Connection con = DbConnectionPool.getConnection();
        try (
             var txLog = txLogService.prepare(con, logs);
             var updateState = updateStateService.update(con, state)
        ){
            GENERAL.info("Writing {} logs", logs.size());
            txLog.executeBatch();
            updateState.executeBatch();
            con.commit();
        }catch (Exception e){
            e.printStackTrace();
            con.rollback();
            throw e;
        }
        finally {
            con.close();
        }
    }
}
