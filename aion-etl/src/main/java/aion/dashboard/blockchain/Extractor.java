package aion.dashboard.blockchain;

import aion.dashboard.exception.AionApiException;
import aion.dashboard.parser.Producer;
import aion.dashboard.service.ParserStateService;
import aion.dashboard.util.Utils;
import org.aion.api.type.BlockDetails;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public final class Extractor extends Producer<BlockDetails>{

    private AionService apiService;
    private ParserStateService parserStateService;
    private AtomicLong ptr;


    public Extractor(AionService apiService, ParserStateService parserStateService, BlockingQueue<List<BlockDetails>> queue){
        super(queue);
        this.apiService = apiService;
        this.parserStateService = parserStateService;
    }




    @Override
    public void run() {
        Thread.currentThread().setName("extractor");
        ptr = new AtomicLong(getPointer());
        super.run();
    }

    private long getPointer(){
        return parserStateService.readDBState().getBlockNumber().longValue();
    }

    @Override
    protected List<BlockDetails> task() throws Exception {
        super.task();// check the preconditions
        try {
            List<BlockDetails> records = getBlocks(ptr.get() + 1, 1000);
            ptr.set(Utils.getLastRecord(records).getNumber());
            return records;
        } catch (AionApiException e){
            apiService.reconnect();
            throw e;
        }
    }

    @Override
    protected void doReset() {

        ptr.set(getPointer());
        queue.clear();

        shouldReset.compareAndSet(true, false);

    }



    private List<BlockDetails> getBlocks(long start, int num ) throws AionApiException, InterruptedException {
        while (apiService.getBlockNumber() < start)Thread.sleep(500);

        long end = Math.min(start + num - 1, apiService.getBlockNumber());
        return apiService.getBlockDetailsByRange(start, end);
    }


}
