package aion.dashboard.integritychecks;

import aion.dashboard.blockchain.interfaces.Web3Service;
import aion.dashboard.config.Config;
import aion.dashboard.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IntegrityCheckManager {

    private static final IntegrityCheckManager INSTANCE=new IntegrityCheckManager();
    private ScheduledExecutorService service = Executors.newScheduledThreadPool(2);


    public static IntegrityCheckManager getInstance() {
        return INSTANCE;
    }
    private static final Logger INTEGRITY_LOGGER = LoggerFactory.getLogger("logger_integrity");


    public void startAll(){
        if (Config.getInstance().isVerifierEnabled()) {
            List<IntegrityCheck> checkList = new ArrayList<>();
            Thread.currentThread().setName("integrity-check-manager");

            INTEGRITY_LOGGER.info("Starting checks");
            checkList.add(new BlockIntegrityCheck(BlockServiceImpl.getInstance(), Web3Service.getInstance(), ParserStateServiceImpl.getInstance()));
            checkList.add(new TransactionIntegrityCheck(BlockServiceImpl.getInstance(), TransactionServiceImpl.getInstance(), ParserStateServiceImpl.getInstance(), Web3Service.getInstance()));
            checkList.add(new AccountIntegrityCheck(AccountServiceImpl.getInstance(), Web3Service.getInstance()));
            for (var integrityCheck : checkList) {
                service.scheduleWithFixedDelay(integrityCheck, 0, 100, TimeUnit.SECONDS);
            }
        }else service.shutdown();
    }


    public void shutdown(){
        try {
            service.shutdown();
            service.awaitTermination(100, TimeUnit.SECONDS);
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
        finally {
            service.shutdownNow();
        }
    }

}
