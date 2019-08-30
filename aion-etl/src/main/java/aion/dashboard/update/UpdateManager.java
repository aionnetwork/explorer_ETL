package aion.dashboard.update;

import aion.dashboard.blockchain.Web3ServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class UpdateManager {


    private static final UpdateManager INSTANCE = new UpdateManager();
    List<UpdateTask> updateTasks;
    ExecutorService executors;

    public static UpdateManager getInstance() {
        return INSTANCE;
    }

    private UpdateManager(){
        updateTasks = new ArrayList<>();
        updateTasks.add(new TxLogUpdate(Web3ServiceImpl.getInstance()));
        updateTasks.add(new InternalTransactionUpdate(Web3ServiceImpl.getInstance()));
        executors = Executors.newFixedThreadPool(updateTasks.size());
    }


    public void start(){
        for (var task :updateTasks){
            executors.execute(task);
        }

    }
    void stop(){
        try {
            executors.shutdown();
            executors.awaitTermination(1, TimeUnit.MINUTES);
        }
        catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
        finally {
            executors.shutdownNow();
        }
    }

}
