package aion.dashboard.task;

import aion.dashboard.blockchain.AionService;
import aion.dashboard.exception.AionApiException;
import aion.dashboard.service.AccountServiceImpl;
import aion.dashboard.service.ParserStateServiceImpl;
import aion.dashboard.service.ReorgServiceImpl;
import aion.dashboard.service.SchedulerService;
import aion.dashboard.worker.BlockchainReaderThread;
import aion.dashboard.worker.DBThread;
import aion.dashboard.worker.IntegrityCheckThread;
import aion.dashboard.worker.ShutdownHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class InitTask {


    private static final String VersionNumber = "v4.02";
    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");

    private static void revert(String blk) {

        ParserStateServiceImpl ps = ParserStateServiceImpl.getInstance();
        ReorgServiceImpl reorgService = new ReorgServiceImpl(AionService.getInstance(), ps);
        long blknum = Long.parseLong(blk) + 1;
        try {
            reorgService.performReorg(blknum);
            GENERAL.info("New database head: {}", ps.readDBState());

        } catch (SQLException e) {
            String message = "unable to delete the blocks in the database.";

            GENERAL.error("Revert failed: {}", message);
        } catch (AionApiException e) {
            String message = "unable to reach the API.";

            GENERAL.error("Revert failed: {}", message);
        }

    }

    private static void printVersion() {
        System.out.println("Aion ETL "+VersionNumber);
    }


    public static void checkArgs(String[] arg) {
        switch (arg[0]) {
            case "-v":
                printVersion();
                break;
            case "-r":
                if (arg.length == 2)
                    revert(arg[1]);
                else
                    System.out.println("Wrong number of arguments passed to revert! \nExpected 2 got " + arg.length + ".");
                break;
            default:
                System.out.println("Unrecognized argument");

        }
    }

    public static void start() throws AionApiException {
        Logger general = GENERAL;
        general.info("--------------------------------");
        general.info("Starting ETL {}", VersionNumber);
        general.info("--------------------------------");
        //starting migration
        try {
            AionService.getInstance().reconnect();

            BigInteger headDbBlock = ParserStateServiceImpl.getInstance().readDBState().getBlockNumber();
            //noinspection StatementWithEmptyBody
            if (AccountServiceImpl.getInstance().getMaxBlock() == 0 && !headDbBlock.equals(BigInteger.valueOf(-1))) {// check if block table is empty


            //new MigrationThread().start();// start migration if balance table is empty and the block table is not empty
            }
        } catch (SQLException e) {
            general.debug("Failed to read state of balance table. Exiting.");
            System.exit(-1);
        }

        BlockchainReaderThread chainThread = new BlockchainReaderThread();
        DBThread dbThread = new DBThread(chainThread);
        //StatisticsThread statisticsThread = new StatisticsThread();
        IntegrityCheckThread integrityCheckThread = new IntegrityCheckThread();
        ShutdownHook shutdownHook = new ShutdownHook.Builder()
                .dbThread(dbThread)
                .integrityCheckThread(integrityCheckThread)
                .readerThread(chainThread)
                .build();


        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(shutdownHook);


        chainThread.start();
        dbThread.start();
        integrityCheckThread.start();

        schedule(chainThread, dbThread);
        //statisticsThread.start();
    }

    private static void schedule(BlockchainReaderThread readerThread, DBThread dbThread) throws AionApiException {


        SchedulerService.getInstance().add(new DataBaseTask(), 90, TimeUnit.SECONDS);
        SchedulerService.getInstance().add(new QueueStatisticsTask(readerThread), 15, TimeUnit.SECONDS);
        SchedulerService.getInstance().add(new AionServiceStatisticsTask(dbThread, readerThread), 60, TimeUnit.SECONDS);
        SchedulerService.getInstance().add(AionServiceMonitorTask.getInstance(), 10, TimeUnit.MINUTES);

        GraphingTask.getInstance().scheduleNow();


    }
}
