package aion.dashboard;

import aion.dashboard.config.Config;
import aion.dashboard.service.BalanceServiceImpl;
import aion.dashboard.service.ParserStateServiceImpl;
import aion.dashboard.service.SchedulerService;
import aion.dashboard.task.AionServiceStatisticsTask;
import aion.dashboard.task.DataBaseTask;
import aion.dashboard.task.GraphingTask;
import aion.dashboard.task.QueueStatisticsTask;
import aion.dashboard.worker.BlockchainReaderThread;
import aion.dashboard.worker.DBThread;
import aion.dashboard.worker.IntegrityCheckThread;
import aion.dashboard.worker.ShutdownHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class AionMain {


	static { System.setProperty("logback.configurationFile", "src/main/resources/logback.xml");}

	public static void main(String[] args) {
		if (args.length == 0)
			start();
		else if (args.length == 1){
			checkArgs(args[0]);
		}
		else {
			System.out.println("Unrecognized argument");
		}
	}

	private static void checkArgs(String arg){
		switch (arg){
			case "-v":
				System.out.println("Aion ETL v4");
				break;

			default:
			    System.out.println("Unrecognized argument");

		}
	}
	private static void start(){
		Logger general = LoggerFactory.getLogger("logger_general");

		((ch.qos.logback.classic.Logger)LoggerFactory.getLogger("general_logger")).setLevel(Config.getInstance().getGeneralLevel());
		general.info("--------------------------------");
		general.info("Starting ETL v4");
		general.info("--------------------------------");
		//starting migration
		try {

            BigInteger headDbBlock = ParserStateServiceImpl.getInstance().readDBState().getBlockNumber();
            //noinspection StatementWithEmptyBody
            if (BalanceServiceImpl.getInstance().getMaxBlock() == 0 && !headDbBlock.equals(BigInteger.valueOf(-1))) {// check if block table is empty


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

		schedule(chainThread,dbThread);
		//statisticsThread.start();
	}

	private static void schedule(BlockchainReaderThread readerThread,DBThread dbThread){


		SchedulerService.getInstance().add(new DataBaseTask(),90, TimeUnit.SECONDS);
		SchedulerService.getInstance().add(new QueueStatisticsTask(readerThread),15, TimeUnit.SECONDS);
		SchedulerService.getInstance().add(new AionServiceStatisticsTask(dbThread,readerThread),60, TimeUnit.SECONDS);

		GraphingTask.getInstance().scheduleNow();





	}

}



