package aion.dashboard.worker;

import aion.dashboard.blockchain.AionService;
import aion.dashboard.config.Config;
import aion.dashboard.domainobject.BatchObject;
import aion.dashboard.email.EmailService;
import aion.dashboard.exception.AionApiException;
import aion.dashboard.exception.DecodeException;
import aion.dashboard.exception.FailedAPIIntegrityCheckException;
import aion.dashboard.parser.BlockParser;
import aion.dashboard.service.ParserStateService;
import aion.dashboard.service.ParserStateServiceImpl;
import aion.dashboard.service.RollingBlockMean;
import aion.dashboard.util.TimeLogger;
import aion.dashboard.util.Utils;
import com.google.common.collect.Comparators;
import org.aion.api.type.BlockDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class BlockchainReaderThread extends Thread{
    private static Config config = Config.getInstance();

    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");

    private static final TimeLogger TIME_LOGGER = new TimeLogger(BlockchainReaderThread.class.getName());

    private static final long ERR_DELAY = config.getReaderErrPollDelay();
    private static final long POLL_DELAY= config.getReaderPollDelay();

    private boolean keepRunning;
    private ArrayBlockingQueue <BatchObject> batchQueue;
    private AionService aionService;
    private final AtomicLong queuePointer = new AtomicLong();
    private final AtomicBoolean shouldReorg = new AtomicBoolean(false);
    private BlockParser blockParser;
    private ParserStateService parserStateService = ParserStateServiceImpl.getInstance();
    private int numReconnectAionService;
    private RollingBlockMean rollingBlockMean;



    public BlockchainReaderThread(){
        super("BlockchainReaderThread");


        batchQueue = new ArrayBlockingQueue<>((int) config.getQueueSize());

        aionService = AionService.getInstance();
        shouldReorg.set(false);
        blockParser = null;
        keepRunning = true;
    }

    @Override
    public void run() {
        GENERAL.info("-------------------------");
        GENERAL.info("Blockchain Reader Thread");
        GENERAL.info("-------------------------");

        try {

            queuePointer.set(parserStateService.readDBState().getBlockNumber().longValue() + 1);
            aionService.reconnect();

            rollingBlockMean = RollingBlockMean.init(
                    parserStateService.readBlockMeanState().getBlockNumber().longValue(),
                    parserStateService.readTransactionMeanState().getBlockNumber().longValue(),
                    parserStateService.readDBState().getBlockNumber().longValue(),
                    aionService);

            blockParser = new BlockParser(aionService, rollingBlockMean);
        } catch (Exception e) {
            GENERAL.error("Error reading initial parser state. ", e);
            GENERAL.error("Failed to start ETL");
            System.exit(-1);
        }

        while (!Thread.interrupted() && keepRunning){

            try {
                if(!aionService.isConnected()) {
                    ++numReconnectAionService;
                    aionService.reconnect();
                }


                if(!Utils.trySleep(ERR_DELAY))
                    break;


                while (!Thread.interrupted() && keepRunning) {
                    setName("BlockchainReaderThread");


                    if(!Utils.trySleep(POLL_DELAY))
                        break;


                    if (shouldReorg.get()) clearQueue();//clear queue in the case of a reorg

                    extractTransformAll();


                }


            }
            catch (AionApiException e){
                GENERAL.debug("Blockchain Reader Thread caught top level exception." , e);
                EmailService.getInstance().send("Blockchain reader threw an exception",
                        String.format("Aion Api threw an exception: %s", e.getMessage()));

            }
            catch (Exception e){
                GENERAL.debug("Blockchain Reader Thread caught top level exception." , e);
                EmailService.getInstance().send("Blockchain reader threw an exception", "Block chain reader threw an exception: " +e.getMessage());
            }

        }

        GENERAL.debug("Shutting down blockchain reader");
        GENERAL.debug("Successfully shutdown blockchain reader");
    }

    // Queries the block chain to determine whether any blocks can be parsed.
    // If any are present they will be loaded into memory and then passed into the parser object
    private void extractTransformAll() throws AionApiException, FailedAPIIntegrityCheckException, SQLException, DecodeException, InterruptedException, ExecutionException {
        if (GENERAL.isTraceEnabled()){
            GENERAL.trace("Polling kernel for a new block");
        }
        while(aionService.getBlockNumber() >= queuePointer.get() && batchQueue.remainingCapacity() > 0 && !shouldReorg.get() && keepRunning){


            if (doExtractTransform()) break;// break out of the loop if the queue is full
        }
    }

    private boolean doExtractTransform() throws AionApiException, FailedAPIIntegrityCheckException, SQLException, DecodeException, InterruptedException, ExecutionException {
        TIME_LOGGER.start();
        long requestPtr;

        if ((aionService.getBlockNumber() - queuePointer.get()) > config.getBlockQueryRange()) {
            requestPtr = queuePointer.get() + (config.getBlockQueryRange()-1);// To get the inclusive range
            // subtract one otherwise the request size will be one greater than the query range
        }
        else {
            requestPtr = aionService.getBlockNumber();
        }


        long requestedRange = 1+requestPtr - queuePointer.get();


        List<BlockDetails> blockDetails = config.isTest() ?
                aionService.getBlockDetailsByRange(queuePointer.get(), Math.min(requestPtr, config.getMaxHeight())):
                aionService.getBlockDetailsByRange(queuePointer.get(), requestPtr);

        // Perform checks to see if the values coming back from the DB are valid
        if (config.isExpectedRangeCheck() && !checkExpectedRange(blockDetails, queuePointer.get(), requestPtr))
            throw new FailedAPIIntegrityCheckException("Aion api failed to return expected range");

        if (config.isRequestedRangeSizeCheck() && requestedRange != blockDetails.size())
            throw new FailedAPIIntegrityCheckException("Aion api failed to return expected range size");

        if (config.isSortedCheck() && !checkBlocksAreSorted(blockDetails))
            throw new FailedAPIIntegrityCheckException("Aion api failed to return a sorted list");


        BatchObject batch = blockParser.parseBlockDetails(blockDetails, requestPtr);

        if ( batchQueue.offer(batch) ){
            queuePointer.set(requestPtr + 1);
        }
        else {
            GENERAL.debug("Batch queue is full.");
            return true;
        }

        TIME_LOGGER.logTime(String.format("Reader extracted %d blocks in {}", requestedRange));
        return false;
    }


    /**
     * Signals that a reorg has just occurred and the queuePointer, blockParser and the queue should all be reset
     */
    public void forceReaderReset(){ shouldReorg.set(true); }


    /**
     * Empty out the queue and reset the transaction id and queue pointer
     */
    private void clearQueue() {

        while (!batchQueue.isEmpty()) if(batchQueue.remove() == null) break;




        queuePointer.set(parserStateService.readDBState().getBlockNumber().longValue() + 1);
        rollingBlockMean.reorg(parserStateService.readDBState().getBlockNumber().longValue()+1);
        blockParser = new BlockParser(aionService, rollingBlockMean);
        GENERAL.debug("Queue pointer is: {}", queuePointer);
        shouldReorg.set(false);
    }

    /**
     * Allows external services to query the state of a reset
     * @return
     */
    public boolean waitingForReset(){return shouldReorg.get();}

    /**
     * Signal this thread to stop execution after it completes its current task
     */
    public void kill(){ keepRunning = false;}

    /**
     * Get the current height of the queue
     * @return
     */
    public long getQueuePointer() {
        return queuePointer.get();
    }


    public ArrayBlockingQueue<BatchObject> getBatchQueue() {
        return batchQueue;
    }

    /**
     * Gets the batch at the front of the queue or null if the queue is empty
     * @return
     */
    public BatchObject getBatch(){
        return batchQueue.poll();
    }

    public boolean hasNext(){
        return !batchQueue.isEmpty();
    }

    /**
     * Tests that the blockdetails list is sorted
     * @param blockDetails
     * @return true if the blockdetails is sorted
     */
    @SuppressWarnings("UnstableApiUsage")
    static boolean checkBlocksAreSorted(List<BlockDetails> blockDetails){

        return Comparators.isInOrder(blockDetails, (BlockDetails p, BlockDetails c) -> {
                if (p.getNumber() - c.getNumber() == 0) return 0;
                else if (p.getNumber() > c.getNumber()) return  1;
                else return -1;
        });
    }


    /**
     * Tests that the list returned has the expected start and end values
     * @param details
     * @param start
     * @param end
     * @return
     */
    static boolean checkExpectedRange(List<BlockDetails> details, long start, long end){
        BlockDetails startBlock = details.get(0);
        BlockDetails endBlock = details.get(details.size() -1);
        return (startBlock.getNumber() == start) && (endBlock.getNumber() == end);
    }
    public int getNumReconnectAionService() {
        return numReconnectAionService;
    }

    public void setNumReconnectAionService(int numReconnectAionService) {
        this.numReconnectAionService = numReconnectAionService;
    }

}
