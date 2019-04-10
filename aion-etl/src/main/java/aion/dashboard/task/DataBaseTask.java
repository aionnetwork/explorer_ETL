package aion.dashboard.task;

import aion.dashboard.config.Config;
import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.util.TimeLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class DataBaseTask implements  Runnable{

    AtomicInteger totalWs = new AtomicInteger(0);
    Logger GENERAL = LoggerFactory.getLogger("logger_general");
    Config config;
    boolean keepRunning = true;

    TimeLogger timeLogger;

    public DataBaseTask(){

        timeLogger = new TimeLogger(this.getClass().getName());
        timeLogger.start();

    }

    @Override
    public void run() {
        Thread.currentThread().setName("DatabaseStatisticsTask");
        timeLogger.logTime("TotalConnections: " + DbConnectionPool.getTotalConnections()+" idleConnections:: " + DbConnectionPool.getIdleConnections()+" Active: " + DbConnectionPool.getActiveConnections()+" ThreadsAwaitingConnection: " + DbConnectionPool.getThreadsAwaitingConnection());


    }

}
