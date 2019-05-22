package aion.dashboard.parser;

import aion.dashboard.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

public abstract class Producer<T> implements Runnable {

    protected static final Logger GENERAL;

    static {
        GENERAL = LoggerFactory.getLogger("logger_general");
    }

    private final ExecutorService service;
    private final AtomicBoolean running;
    protected BlockingQueue<List<T>> queue;
    protected AtomicBoolean shouldReset;
    private Future<?> future;

    protected Producer(BlockingQueue<List<T>> queue) {
        this.queue = queue;
        future = null;
        shouldReset = new AtomicBoolean(false);
        running = new AtomicBoolean(false);
        service = newSingleThreadExecutor();
    }

    public boolean shouldReset() {
        return shouldReset.get();
    }

    public Iterator<T> peek() {
        List<T> res = queue.peek();
        if (res == null || res.isEmpty()) {
            return Collections.emptyIterator();
        } else {
            return res.iterator();
        }
    }

    /**
     * This operation should trigger a reset in the main task operation.
     */
    public void reset() {
        shouldReset.compareAndSet(false, true);
    }

    public final void consume() {
        queue.poll();
    }

    /**
     * @return the result of this operation
     * @throws InterruptedException if the thread is interrupted during a sleep operation
     */
    protected abstract List<T> task() throws Exception ;

    protected abstract void doReset();

    public final void run() {// declares the event loop that will run the task
        do {
            try {
                if (shouldReset.get()) {//check preconditions
                    throw new ResetException();
                }

                List<T> res = task();// run the task and store the result only if it is valid
                if (res != null && !res.isEmpty()) queue.put(res);
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                GENERAL.debug("Thread interrupted. Ending execution.");
            } catch (ResetException e) {
                doReset();
                GENERAL.debug("Reset triggered.");
            } catch (Exception e) {
                GENERAL.error("Caught top level exception: ", e);
            }

        } while (keepLooping());
        running.compareAndSet(true, false);
        GENERAL.info("Shutdown producer thread");
    }

    public final boolean awaitTermination() {
        try{
            while (running.get()) {
                Utils.trySleep(100);
            }
            return true;
        }finally {
            service.shutdown();
            try {
                service.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                GENERAL.debug("Caught interrupted exception: ",e);
            }
            finally {
                service.shutdownNow();
            }
        }
    }

    /**
     * This method starts the producer
     * @return A boolean indicating whether the process succeeded
     */
    public final boolean start() {
        if (!running.get()) {
            future = service.submit(this);
            running.compareAndSet(false, true);

            return true;
        } else {
            return false;
        }
    }

    private final AtomicBoolean shouldStop = new AtomicBoolean(false);
    public final boolean stop() {
        try {
            if (running.get() && future != null) {

                shouldStop.set(true);

                return true;
            } else {
                return false;
            }
        }catch (RuntimeException e){
            return false;
        }

    }

    protected boolean keepLooping() {
        return (!Thread.currentThread().isInterrupted() && !shouldStop.get());
    }

    private static class ResetException extends Exception {

    }
}
