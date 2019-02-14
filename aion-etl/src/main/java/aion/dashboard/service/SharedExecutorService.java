package aion.dashboard.service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SharedExecutorService {

    private ExecutorService executorService;
    private boolean isClosed;
    private static SharedExecutorService Instance = new SharedExecutorService();

    public static SharedExecutorService getInstance() {
        return Instance;
    }


    private SharedExecutorService(){
        if (Instance != null){
            throw  new IllegalStateException("An instance of the shared executor service already exists");

        }
        isClosed = false;
        executorService = Executors.newFixedThreadPool(10,
                new ThreadFactoryBuilder().setNameFormat("SharedExecutor-%d").build());
    }


    public ExecutorService getExecutorService() {
        if (isClosed) throw new IllegalStateException("Shared Executor is closed");
        return executorService;
    }

    public void close() {
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
