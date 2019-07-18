package aion.dashboard.consumer;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.parser.type.ParserBatch;
import aion.dashboard.service.*;

import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlockWriter implements WriteTask<ParserBatch> {

    private BlockService blockService;
    private TransactionService transactionService;
    private InternalTransferService transferService;
    private ContractService contractService;
    private ParserStateService parserStateService;
    private EventService eventService;
    private MetricsService metricsService;
    private TxLogService txLogService;

    public BlockWriter(BlockService blockService, TransactionService transactionService, InternalTransferService transferService, ContractService contractService, ParserStateService parserStateService, EventService eventService, MetricsService metricsService) {
        this.blockService = blockService;
        this.transactionService = transactionService;
        this.transferService = transferService;
        this.contractService = contractService;
        this.parserStateService = parserStateService;
        this.eventService = eventService;
        this.metricsService = metricsService;
    }

    public BlockWriter() {
        metricsService = MetricsServiceImpl.getInstance();
        txLogService = TxLogServiceImpl.getInstance();
        blockService = BlockServiceImpl.getInstance();
        transactionService = TransactionServiceImpl.getInstance();
        transferService = InternalTransferServiceImpl.getInstance();
        contractService = ContractServiceImpl.getInstance();
        parserStateService = ParserStateServiceImpl.getInstance();
        eventService = EventServiceImpl.getInstance();

    }

    @Override
    public void write(ParserBatch record) throws Exception {
        var states = Stream.concat(Stream.of(record.getBlockChainState(), record.getState()),
                record.getMeanStates().stream())
                .collect(Collectors.toList());
        try (Connection connection = DbConnectionPool.getConnection()) {
            try (var block = (blockService.prepare(connection, record.getBlocks()));
                 var tx = (transactionService.prepare(connection, record.getTransactions()));
                 var transfer = (transferService.prepare(connection, record.getInternalTransfers()));
                 var con = (contractService.prepare(connection, record.getContracts()));
                 var parser = (parserStateService.prepare(connection, states));
                 var event = (eventService.prepare(connection, record.getEvents()));
                 var metrics = metricsService.prepare(connection, record.getMetrics());
                 var txLog = txLogService.prepare(connection, record.getLogs())
            ) {
                block.executeBatch();
                tx.executeBatch();
                transfer.executeBatch();
                con.executeBatch();
                parser.executeBatch();
                event.executeBatch();
                metrics.executeBatch();
                txLog.executeBatch();
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }

        }
    }
}
