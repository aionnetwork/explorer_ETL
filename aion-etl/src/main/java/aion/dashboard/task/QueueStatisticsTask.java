package aion.dashboard.task;

import aion.dashboard.config.Config;
import aion.dashboard.worker.BlockchainReaderThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueStatisticsTask implements Runnable {

    private Logger ANALYTICS_LOGGER = LoggerFactory.getLogger("logger_analytics");
    private BlockchainReaderThread readerThread;
    public QueueStatisticsTask(BlockchainReaderThread readerThread){
        this.readerThread = readerThread;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("QueueStatisticsTask");

        if (readerThread.getBatchQueue().remainingCapacity() <= 1) {
            ANALYTICS_LOGGER.debug("Reader thread remaining capacity: {}", readerThread.getBatchQueue().remainingCapacity());
        }
        else if (readerThread.getBatchQueue().remainingCapacity() == Config.getInstance().getQueueSize()){
            ANALYTICS_LOGGER.debug("Reader thread queue is empty. Queue Size: {}", readerThread.getBatchQueue().remainingCapacity());

        }
    }
}
