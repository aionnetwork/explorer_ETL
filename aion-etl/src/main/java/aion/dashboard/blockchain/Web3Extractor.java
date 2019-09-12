package aion.dashboard.blockchain;

import aion.dashboard.blockchain.interfaces.Web3Service;
import aion.dashboard.blockchain.type.APIBlockDetails;
import aion.dashboard.exception.Web3ApiException;
import aion.dashboard.parser.Producer;
import aion.dashboard.service.ParserStateService;
import aion.dashboard.util.Utils;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class Web3Extractor extends Producer<APIBlockDetails> {
    private Web3Service apiService;
    private ParserStateService parserStateService;
    private AtomicLong ptr;
    private final int requestSize;
    private static final int MAX_RQ_SIZE=10_000;


    public Web3Extractor(Web3Service apiService, ParserStateService parserStateService, BlockingQueue<List<APIBlockDetails>> queue){
        this(apiService, parserStateService, queue, MAX_RQ_SIZE);
    }


    public Web3Extractor(Web3Service apiService, ParserStateService parserStateService, BlockingQueue<List<APIBlockDetails>> queue, long requestSize) {
        super(queue);
        this.apiService = apiService;
        this.parserStateService = parserStateService;
        this.requestSize = (int) Math.min(requestSize, MAX_RQ_SIZE);
        this.ptr = new AtomicLong(getPointer());
    }

    private long getPointer(){
        return parserStateService.readDBState().getBlockNumber().longValue();
    }

    @Override
    protected List<APIBlockDetails> task() throws Exception {
        Thread.currentThread().setName("extractor");

        try {
            List<APIBlockDetails> records = getBlocks(ptr.get() + 1, requestSize);

            if (GENERAL.isTraceEnabled()) {
                records.forEach(blk-> GENERAL.trace("Retrieved block with block_number: {}", blk.getNumber()));
            }
            ptr.set(Utils.getLastRecord(records).getNumber());
            return records;
        } catch (Web3ApiException e){
            GENERAL.error("Failed to retrieve blocks at block number {}.", ptr.get());
            throw e;
        }
    }

    @Override
    protected void doReset() {
        ptr.set(getPointer());
        queue.clear();
        GENERAL.info("Reset the queue to {}" , ptr.get());
        shouldReset.compareAndSet(true, false);
    }

    private List<APIBlockDetails> getBlocks(long start, int num ) throws Exception {
        while (apiService.getBlockNumber() < start)Thread.sleep(500);
        long end = Math.min(start + num - 1, apiService.getBlockNumber());
        return apiService.getBlockDetailsInRange(start, end);
    }
}
