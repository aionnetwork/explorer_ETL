package aion.dashboard.performance;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.service.ParserStateService;
import aion.dashboard.service.ParserStateServiceImpl;
import aion.dashboard.util.TimeLogger;
import aion.dashboard.worker.BlockchainReaderThread;
import aion.dashboard.worker.DBThread;

import java.sql.Connection;
import java.sql.SQLException;

public class PerformanceTest {

    final long lastBlock = 100_999L;

    /**
     * Tests the time taken for the ETL to reach 100 000 blocks
     */

    void performanceTest() throws SQLException {
        try (Connection con = DbConnectionPool.getConnection()){

            String arr[] = new String[]{
                    "truncate table graphing;",
                    "truncate table balance;",
                    "truncate table block;",
                    "truncate table contract;",
                    "truncate table event;",
                    "truncate table token;",
                    "truncate table token_holders;",
                    "truncate table token_transfers;",
                    "truncate table transaction;",
                    "update parser_state set blockNumber = -1 where id = 1 ;",
                    "update parser_state set blockNumber = -1 where id = 2 ;",
                    "update parser_state set blockNumber = 0 where id = 3 ;",
                    "update parser_state set blockNumber = 0 where id = 4;"
            };

            for (var s: arr){
                con.createStatement().execute(s);
            }

            con.commit();

        }




        BlockchainReaderThread th = new BlockchainReaderThread();
        DBThread dbThread = new DBThread(th);
        TimeLogger logger = new TimeLogger("Performance ");

        th.start();
        dbThread.start();
        ParserStateService stateService = ParserStateServiceImpl.getInstance();

        while (stateService.readDBState().getBlockNumber().longValue() < 999){
            try{
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        logger.start();



        while (stateService.readDBState().getBlockNumber().longValue() < lastBlock){
            try{
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        Thread.currentThread().setName("Performance Test");

        logger.logTime("Took {} to read and write 100K blocks");
        System.out.printf("Took %s to read and write 100K blocks", logger.elapsed().toString());

        th.kill();
        dbThread.kill();


        try{
            th.join();
            dbThread.join();
        }
        catch (Exception e){

        }




    }
}
