package aion.dashboard.task;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.util.TimeLogger;

public class DataBaseTask implements  Runnable{


    private TimeLogger timeLogger;

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
