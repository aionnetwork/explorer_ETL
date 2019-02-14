package aion.dashboard.task;

import aion.dashboard.config.Config;
import aion.dashboard.worker.BlockchainReaderThread;
import aion.dashboard.worker.DBThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AionServiceStatisticsTask implements Runnable {

    private Logger ANALYTICS_LOGGER = LoggerFactory.getLogger("logger_analytics");
    private DBThread dbThread;
    BlockchainReaderThread readerThread;
    public AionServiceStatisticsTask(DBThread dbThread,BlockchainReaderThread readerThread){
        this.dbThread = dbThread;
        this.readerThread=readerThread;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("AionServiceStatisticsTask");

        ANALYTICS_LOGGER.debug("DBThread last AionService reconection times: {}", dbThread.getNumReconnectAionService());
        dbThread.setNumReconnectAionService(0);
        ANALYTICS_LOGGER.debug("BlockchainReaderThread last AionService reconection times: {}", readerThread.getNumReconnectAionService());
        readerThread.setNumReconnectAionService(0);
    }
}
