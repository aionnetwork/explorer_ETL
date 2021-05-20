package aion.dashboard.stats;

import aion.dashboard.service.*;
import aion.dashboard.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CirculatingSupplyTask implements Runnable {
    private DBService dbService;
    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public CirculatingSupplyTask(DBService dbService) {
        this.dbService=dbService;

    }

    public CirculatingSupplyTask(){
        dbService=DBService.getInstance();
    }
    public void start(){
        executorService.submit(this);
    }

    public void stop(){
        executorService.shutdownNow();
    }
    @Override
    public void run(){

        do {
            try {
                Thread.currentThread().setName("circulating supply task");

                if (!Thread.currentThread().isInterrupted()) {
                    dbService.generateCirculatingSupply();
                }
            }catch (Exception e){
                GENERAL.warn("Error: ", e);
            }
// run every hour
        }while (Utils.trySleep(60000*60));
    }




}
