package aion.dashboard.util;

import aion.dashboard.config.Config;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;


/**
 * Reusable class to log timed events within the etl
 */
public class TimeLogger {

    private static final boolean flag = Config.getInstance().performanceFlag();//A flag indicating whether time analytics should be enabled
    private Stopwatch stopwatch;
    Logger AnalyticsLogger = LoggerFactory.getLogger("logger_analytics");
    String classname = "";

    /**
     *
     */
    public TimeLogger(String classname){
        if (flag)
            stopwatch = Stopwatch.createUnstarted();

        this.classname = classname;
    }


    public static TimeLogger getTimeLogger() {
        return new TimeLogger("");
    }

    public static TimeLogger getTimeLogger(String className) {
        return new TimeLogger(className);
    }
    /**
     * Starts the time
     */
    public void start(){
        if (flag) {
            stopwatch.reset();
            stopwatch.start();
        }
    }


    /**
     * Stops the internally running timer
     */
    public void stop(){
        if (flag && stopwatch.isRunning()) stopwatch.stop();
    }


    /**
     * Logs the time and message to the analytics.log
     * @param message the message is expected to be a formatted string
     */
    public void logTime(String message){
        if (flag) {
            stop();
            AnalyticsLogger.debug("Time Logger "+ classname+": "+message, stopwatch.elapsed().toSeconds()+"s " + stopwatch.elapsed().toNanosPart() + "ns");
        }

    }




    /**
     *
     * @return
     * @throws IllegalStateException
     */
    public Duration elapsed()throws IllegalStateException{
        if (!flag) throw new IllegalStateException("Time logger is disabled");
        return stopwatch.elapsed();
    }




}
