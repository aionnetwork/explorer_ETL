package aion.dashboard.stats;

import aion.dashboard.domainobject.Graphing;
import aion.dashboard.service.GraphingService;
import aion.dashboard.service.GraphingServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class AbstractGraphingTask<T>  implements Runnable{


    private final ScheduledExecutorService scheduledWorker = Executors.newScheduledThreadPool(1);
    public enum TaskType{
        DB, KERNEL
    }


    public static AbstractGraphingTask getInstance(TaskType type) {

        switch (Objects.requireNonNull(type)){

            case DB:
                return DBGraphingTask.DB_GRAPHING_TASK;
            case KERNEL:
                return GraphingTask.API_GRAPHING_TASK;
            default:
                throw new IllegalArgumentException("Invalid argument");
        }
    }



    protected GraphingService graphingService = GraphingServiceImpl.getInstance();
    protected static final Logger GENERAL = LoggerFactory.getLogger("logger_general");

    abstract List<Graphing> compute(List<T> blocks) throws Exception;
    private static final int INITIAL_DELAY = 5;
    private static final int DELAY_AT_END_OF_HOUR = 2;

    protected static final ZoneId UTCZoneID = ZoneId.of("UTC");

    public ScheduledFuture getFuture() {
        return future;
    }
    private volatile ScheduledFuture future;

    public void stop(){
        future.cancel(true);
    }

    final void scheduleNext() {
        LocalDateTime localNow = LocalDateTime.now();
        ZoneId currentZone = ZoneId.systemDefault();
        ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);

        ZonedDateTime zonedDateTime = zonedNow.withSecond(0).plusHours(1).withMinute(DELAY_AT_END_OF_HOUR);

        GENERAL.debug("Next execution scheduled at {}", zonedDateTime);

        Duration duration = Duration.between(zonedNow, zonedDateTime);
        long delay = duration.getSeconds();//get the next hour in which this task should run
        future=scheduledWorker.schedule(this, delay, TimeUnit.SECONDS);
        //this is a safe guard against running the service before all blocks have been added to the DB

    }

    public final void scheduleNow(){
        ZonedDateTime timeNow = ZonedDateTime.now(UTCZoneID);
        ZonedDateTime timeNextHour = ZonedDateTime.now(UTCZoneID).plusHours(1).withMinute(DELAY_AT_END_OF_HOUR).withSecond(0);
        Duration diff = Duration.between(timeNow, timeNextHour);


        if (diff.toMinutes() <= 10 ) {
            scheduleNext();// If the time to the next hour is less than 10 minutes schedule in the next hour
        } else {
            //Schedule the service to run in the background now
            GENERAL.debug("Next execution of Graphing Task scheduled at {}", timeNow.plusMinutes(INITIAL_DELAY));
            future=scheduledWorker.schedule(this, INITIAL_DELAY, TimeUnit.MINUTES);

        }
    }
}
