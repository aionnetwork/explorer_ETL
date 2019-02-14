package aion.dashboard.task;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.domainobject.BatchObject;
import aion.dashboard.domainobject.ParserState;
import aion.dashboard.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WriteTask {
    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");
    private static final WriteTask INSTANCE = new WriteTask();
    private BalanceService balanceService = BalanceServiceImpl.getInstance();
    private BlockService blockService = BlockServiceImpl.getInstance();
    private ContractService contractService = ContractServiceImpl.getInstance();
    private EventService eventService = EventServiceImpl.getInstance();
    private ParserStateService parserService= ParserStateServiceImpl.getInstance();
    private TokenBalanceService tokenBalanceService = TokenBalanceServiceImpl.getInstance();
    private TokenService tokenService= TokenServiceImpl.getInstance();
    private TransactionService transactionService = TransactionServiceImpl.getInstance();
    private TransferService transferService = TransferServiceImpl.getInstance();

    public static WriteTask getInstance() {
        return INSTANCE;
    }

    private WriteTask(){
        if(INSTANCE !=null) {
            //IGNORE LINTING RULE
            //This check was added to enforce the singleton

            throw new IllegalStateException("Cannot create another instance");
        }
    }
    public boolean executeTask(BatchObject batchObject, ParserState chainState)  {
        if (GENERAL.isTraceEnabled()){
            GENERAL.trace("Attempting to write to Database...");
        }
        try (Connection con = DbConnectionPool.getConnection()) {
            List<PreparedStatement> statements = new ArrayList<>();

            try {


                //prepare the batch to be written in to the DB
                statements.add(balanceService.prepare(con, batchObject.getBalances()));
                statements.addAll(Arrays.asList(blockService.prepare(con, batchObject.getBlocks())));
                statements.add(contractService.prepare(con, batchObject.getContracts()));
                statements.add(eventService.prepare(con, batchObject.getEvents()));
                statements.add(tokenBalanceService.prepare(con, batchObject.getTokenBalances()));
                statements.add(tokenService.prepare(con, batchObject.getTokens()));
                statements.addAll(Arrays.asList(transactionService.prepare(con, batchObject.getTransactions())));
                statements.add(transferService.prepare(con, batchObject.getTransfers()));
                statements.add(parserService.prepare(con, List.of(batchObject.getParser_state(), chainState)));


                //execute all the batched prepared statements
                for (var statement : statements) {

                    statement.executeBatch();
                }


                //Commit the changes
                con.commit();
            } catch (SQLException e) {
                GENERAL.debug("Threw an exception while trying to write to the DB: ", e);
                con.rollback();//roll back in the case of an error

                throw e;

            } finally {



                for (var statement : statements) {
                    try {
                        statement.close();//close all statements to avoid a memory leak
                    } catch (Exception ignored) {
                        //Nothing needs to be done here

                    }
                }
            }

            return true;
        } catch (SQLException e) {
            GENERAL.debug("Threw an exception while trying to write to the DB: ", e);

            return false;
        }


    }
}
