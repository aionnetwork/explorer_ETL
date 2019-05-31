package aion.dashboard.integritychecks;

import aion.dashboard.domainobject.Block;
import aion.dashboard.service.BlockService;
import aion.dashboard.service.ParserStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public abstract class IntegrityCheck<U,T> implements Runnable{

    private final String threadName;
    private final String checkName;

    protected static final Logger INTEGRITY_LOGGER = LoggerFactory.getLogger("logger_integrity");



    /**
     *
     * @param threadName
     * @param checkName
     */
    protected IntegrityCheck(String threadName, String checkName){

        this.threadName = threadName;
        this.checkName = checkName;
    }

    static List<Block> getRandomBlocks(ParserStateService parserStateService, BlockService blockService) {
        long blockHead = parserStateService.readDBState().getBlockNumber().longValue();
        if (blockHead >0) {
            ThreadLocalRandom random = ThreadLocalRandom.current();

            var start = random.nextLong(blockHead);
            var end = random.nextLong(start, start+1000);

            INTEGRITY_LOGGER.info("Using candidates in range: ({},{})", start, end);
            return blockService.getBlocksByRange(start,end).orElse(Collections.emptyList());
        }
        else {
            return Collections.emptyList();
        }
    }


    @Override
    public final void run() {
        Thread.currentThread().setName(threadName);
        INTEGRITY_LOGGER.info("Starting integrity check.");
        try {

            var candidates = findCandidates();
            var res = integrityCheck(candidates);
            if (res.isEmpty()) {
                INTEGRITY_LOGGER.info("{} check succeeded. ", checkName);
                printSuccess(candidates);
            } else {
                INTEGRITY_LOGGER.warn("Integrity check failed. ");
                printFailure(res);
            }


        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            INTEGRITY_LOGGER.warn("Caught exception while performing integrity check: ", e);
        }
    }



    protected abstract List<T> integrityCheck(List<U> candidates) throws Exception;

    /**
     *
     * @return the random list of values to use for the integrity check
     */
    protected abstract List<U> findCandidates() throws Exception;

    protected abstract void printFailure(List<T> failedCandidates);
    protected abstract void printSuccess(List<U> succeededCandidates);
}
