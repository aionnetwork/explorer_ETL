package aion.dashboard.worker;

import aion.dashboard.config.Config;
import aion.dashboard.email.EmailService;
import aion.dashboard.service.*;
import aion.dashboard.util.TimeLogger;
import aion.dashboard.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class IntegrityCheckThread extends Thread {

	private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");



	private ParserStateService parserStateService;
	private BlockService blockService= BlockServiceImpl.getInstance();
	private TransactionService transactionService= TransactionServiceImpl.getInstance();
	private static final long POLL_DELAY;

	static {

		Config config = Config.getInstance();
		POLL_DELAY = config.getDelayIntegrityCheck();
	}
	private TimeLogger timeLogger;

	private boolean keepRunning;

	public IntegrityCheckThread() {
		super("integrity-check");
		timeLogger = new TimeLogger("");


		parserStateService = ParserStateServiceImpl.getInstance();
		keepRunning = true;
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted() && keepRunning) {
			try {
				if (Utils.trySleep(POLL_DELAY)) {


					long startBlock = parserStateService.readIntegrityState().getBlockNumber().longValue();
					long endBlock = parserStateService.readDBState().getBlockNumber().longValue();

					// no need to run integrity check on empty database
					if (endBlock <= 0) {
						GENERAL.debug("Integrity Check: Block table empty. Not running integrity check.");
					} else {
						// Run the integrity check on the block and transaction table
						List<Long> inconsistentBlocks = runBlkTxCheck(startBlock);
						// Run the integrity check on the graphing table
						long inconsistentGraphingRecord = GraphingServiceImpl.getInstance().checkIntegrity(startBlock);
						// log the integrity check response to a file and send any emails
						logIntegrityCheck(endBlock, inconsistentBlocks, inconsistentGraphingRecord);
					}
				}

			} catch (Exception e) {
				EmailService.getInstance().send("Integrity check",  "Caught Exception in integrity check: " + e.getMessage());
				GENERAL.debug("Caught top-level exception ", e);
			}
		}
		GENERAL.debug("Shutdown integrity check thread.");
	}

	private List<Long> runBlkTxCheck(long startBlock) throws SQLException {
		timeLogger.start();
		List<Long> b0 = blockService.blockHashIntegrity(startBlock);
		timeLogger.logTime("Block hash integrity check completed in: {}");
		timeLogger.start();
		List<Long> b1 = transactionService.integrityCheck(startBlock);
		timeLogger.logTime("Transaction num integrity check completed in: {}");
		List<Long> inconsistentBlocks = new ArrayList<>();
		inconsistentBlocks.addAll(b0);
		inconsistentBlocks.addAll(b1);
		return inconsistentBlocks;
	}

	private void logIntegrityCheck(long endBlock, List<Long> inconsistentBlocks, long inconsistentGraphingRecord) {
		if (!inconsistentBlocks.isEmpty() || inconsistentGraphingRecord > 0) {
			EmailService.getInstance().send("Failed integrity check",
					String.format("Found inconsistent at height: %d%nBlocks: %s%n", endBlock, inconsistentBlocks)
							+ String.format("Graphing table found inconsistent at height: %d", inconsistentGraphingRecord));
			GENERAL.error("Found inconsistent at height: {}\nBlocks: {}", endBlock, inconsistentBlocks);
		} else {
			GENERAL.debug("Blockchain consistent at height: {}", endBlock);
			parserStateService.updateHeadIntegrity(BigInteger.valueOf(endBlock));//only update the head if it is consistent
		}
	}


	public void kill(){
		keepRunning = false;
	}
}
