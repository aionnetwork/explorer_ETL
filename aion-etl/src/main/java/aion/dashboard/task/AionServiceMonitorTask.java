package aion.dashboard.task;

import aion.dashboard.blockchain.AionService;
import aion.dashboard.exception.AionApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AionServiceMonitorTask implements Runnable{
    private static AionServiceMonitorTask Instance = null;
    private AionService aionService;
    private long lastNumber;
    private Logger logger = LoggerFactory.getLogger("logger_general");
    private AionServiceMonitorTask() throws AionApiException {
        aionService = AionService.getInstance();
        aionService.reconnect();
        lastNumber = aionService.getBlockNumber();
    }

    public static AionServiceMonitorTask getInstance() throws AionApiException {
        if (Instance == null) {
            Instance = new AionServiceMonitorTask();
        }
        return Instance;

    }

    @Override
    public void run() {
       try {

           if (!aionService.isConnected()){
               aionService.reconnect();
           }
           long updatedBlockNumber = aionService.getBlockNumber();


           if (updatedBlockNumber == lastNumber){
               aionService.reconnect();
           }
           else {
               lastNumber = updatedBlockNumber;
           }
       } catch (AionApiException e) {
            logger.debug("Caught exception in AionServiceMonitorTask:",e);
       }
    }
}
