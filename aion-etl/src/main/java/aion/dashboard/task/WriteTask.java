package aion.dashboard.task;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.domainobject.BatchObject;
import aion.dashboard.domainobject.ParserState;
import aion.dashboard.service.*;
import aion.dashboard.util.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WriteTask {
    Logger GENERAL = LoggerFactory.getLogger("logger_general");
    private static WriteTask Instance = new WriteTask();
    private AccountService accountService = AccountServiceImpl.getInstance();
    private BlockService blockService = BlockServiceImpl.getInstance();
    private ContractService contractService = ContractServiceImpl.getInstance();
    private EventService eventService = EventServiceImpl.getInstance();
    private ParserStateService parserService= ParserStateServiceImpl.getInstance();
    private TokenHoldersService tokenHolderService = TokenHoldersServiceImpl.getInstance();
    private TokenService tokenService= TokenServiceImpl.getInstance();
    private TransactionService transactionService = TransactionServiceImpl.getInstance();
    private TokenTransfersService tokenTransfersService = TokenTransfersServiceImpl.getInstance();
    private MetricsService metricsService = MetricsServiceImpl.getInstance();
    private InternalTransferService transferService = InternalTransferServiceImpl.getInstance();

    public static WriteTask getInstance() {
        return Instance;
    }

    private WriteTask(){
        if(Instance!=null) throw new IllegalStateException("Cannot create another instance");
    }
    public boolean executeTask(BatchObject batchObject, ParserState chainState)  {
        try (Connection con = DbConnectionPool.getConnection()) {
            List<Tuple2<String,PreparedStatement>> statements = new ArrayList<>();

            try {


                //prepare the batch to be written in to the DB
                statements.add(new Tuple2<>("Accounts ",accountService.prepare(con, batchObject.getAccounts())));
                statements.add(new Tuple2<>("Blocks",blockService.prepare(con, batchObject.getBlocks())));
                statements.add(new Tuple2<>("Contracts",contractService.prepare(con, batchObject.getContracts())));
                statements.add(new Tuple2<>("Events",eventService.prepare(con, batchObject.getEvents())));
                statements.add(new Tuple2<>("Token Holders",tokenHolderService.prepare(con, batchObject.getTokenBalances())));
                statements.add(new Tuple2<>("Tokens",tokenService.prepare(con, batchObject.getTokens())));
                statements.add(new Tuple2<>("Transactions",transactionService.prepare(con, batchObject.getTransactions())));
                statements.add(new Tuple2<>("InternalTransfer" , transferService.prepare(con, batchObject.getInternalTransfers())));
                statements.add(new Tuple2<>("Token Transfers",tokenTransfersService.prepare(con, batchObject.getTokenTransfers())));
                List<ParserState> states = batchObject.getParserState();
                states.add(chainState);
                statements.add(new Tuple2<>("Parser State",parserService.prepare(con, states)));

                if (batchObject.getMetrics() != null) {
                    statements.add(new Tuple2<>("Metrics", metricsService.prepare(con, batchObject.getMetrics())));

                }


                //execute all the batched prepared statements
                for (var stringStmtTuple : statements) {
                    if (GENERAL.isTraceEnabled()){
                        GENERAL.trace("Exectuted statement: {}", stringStmtTuple._1());
                    }
                    stringStmtTuple._2().executeBatch();
                }


                //Commit the changes
                con.commit();
            } catch (SQLException e) {
                GENERAL.debug("Threw an exception while trying to write to the DB: ", e);
                try {
                    Objects.requireNonNull(con).rollback();//roll back in the case of an error
                } catch (Exception ignored) {

                }
                throw e;

            } finally {



                for (var stringStmtTuple : statements) {
                    try {
                        stringStmtTuple._2().close();//close all statements to avoid a memory leak
                    } catch (Exception ignored) {

                    }
                }
            }


        } catch (SQLException e) {
            GENERAL.debug("Threw an exception while trying to write to the DB: ", e);

            return false;
        }

        return true;
    }
}
