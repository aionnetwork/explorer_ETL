package aion.dashboard.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * This service provides a convenient interface for interacting with a shared scheduledExecutorService
 */
public class SchedulerService {

    private static SchedulerService Instance = new SchedulerService();
    private ScheduledExecutorService executorService;
    private boolean isClosed;
    private SchedulerService(){
        isClosed=false;
        executorService = Executors.newScheduledThreadPool(2);
    }


    public void add(Runnable task, long delay, TimeUnit unit){
        executorService.scheduleAtFixedRate(task, delay, delay, unit);
    }

    public static SchedulerService getInstance() {
        return Instance;
    }

    public void add(Runnable task, long initialDelay ,long delay, TimeUnit unit){
        executorService.scheduleWithFixedDelay(task, initialDelay, delay, unit);
    }

    public ScheduledFuture addRunOnce(Runnable task, long delay, TimeUnit unit){
        return executorService.schedule(task,delay,unit);
    }


    public void close(){
        if (isClosed){
            throw new IllegalStateException("Service is already closed.");
        }


        //noinspection Duplicates
        try{
            isClosed = true;
            executorService.shutdown();
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        }
        catch (Exception ignored){}
        finally {
            executorService.shutdownNow();
        }
    }

    public boolean isClosed() {
        return isClosed;
    }
}
