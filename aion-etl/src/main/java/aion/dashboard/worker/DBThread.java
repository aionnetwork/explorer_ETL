package aion.dashboard.worker;

import aion.dashboard.blockchain.AionService;
import aion.dashboard.config.Config;
import aion.dashboard.domainobject.BatchObject;
import aion.dashboard.domainobject.ParserState;
import aion.dashboard.email.EmailService;
import aion.dashboard.exception.AionApiException;
import aion.dashboard.exception.DbServiceException;
import aion.dashboard.exception.ReorganizationLimitExceededException;
import aion.dashboard.service.ParserStateService;
import aion.dashboard.service.ParserStateServiceImpl;
import aion.dashboard.service.ReorgService;
import aion.dashboard.service.ReorgServiceImpl;
import aion.dashboard.task.WriteTask;
import aion.dashboard.util.TimeLogger;
import aion.dashboard.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.SQLException;


public class DBThread extends Thread {

    private static final TimeLogger TimeLogger = new TimeLogger("DBThread");
    private BlockchainReaderThread readerThread;
    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");
    private static final long POLL_DELAY;
    private static final long ERR_DELAY;
    private EmailService emailService;
    private boolean keepRunning;
    private AionService aionService;

    private int numReconnectAionService;

    private ReorgService reorgService;


    static {
        Config config = Config.getInstance();
        POLL_DELAY = config.getReaderPollDelay();
        ERR_DELAY = config.getReaderErrPollDelay();
    }
    public DBThread(BlockchainReaderThread readerThread){
        super("DB Thread");
        this.readerThread = readerThread;
        aionService = AionService.getInstance();
        emailService = EmailService.getInstance();

        ParserStateService parserService = ParserStateServiceImpl.getInstance();
        reorgService = new ReorgServiceImpl(aionService, parserService);
        keepRunning = true;

    }

    @Override
    public void run(){
        WriteTask writeTask = WriteTask.getInstance();




        while (readerThread.getState() == State.NEW && keepRunning){
            GENERAL.debug("Waiting for reader to start");
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();

            }
        }
        while (!currentThread().isInterrupted() && readerThread.isAlive() && keepRunning){
            try {
                GENERAL.info("-------------------------");
                GENERAL.info("Database Thread");
                GENERAL.info("-------------------------");

                Utils.trySleep(ERR_DELAY);

                if(!aionService.isConnected())
                    ++numReconnectAionService;
                aionService.reconnect();
                readQueue(writeTask);
            }
            catch (ReorganizationLimitExceededException e){
                GENERAL.error("Exceeded Reorg Limit", e);

                emailService.send("EXCEEDED REORG LIMIT", "Threw an exception in reorg service.");
            }
            catch (Exception e){

                GENERAL.debug("Caught top level exception: ", e);
            }

        }

        GENERAL.info("Shutting down DBThread");

        aionService.close();
        emailService.close();

        GENERAL.info("Successfully shutdown DBThread");

    }

    // wait for objects in the batch and attempt to save them in the DB
    private void readQueue(WriteTask writeTask) throws DbServiceException,
            AionApiException,
            SQLException,
            ReorganizationLimitExceededException {
        while (!currentThread().isInterrupted() && readerThread.isAlive() && keepRunning){





            if (!Utils.trySleep(POLL_DELAY)) break;

            checkReorg();
            BatchObject batchObject=readerThread.getBatch();

            if (GENERAL.isTraceEnabled() && batchObject == null){
                GENERAL.trace("Waiting for batch...");
            }

            while (batchObject!=null && keepRunning){
                boolean status = doSave(writeTask, batchObject);

                if( status ){//only get the next batch if the write was successful otherwise reattempt
                    batchObject = readerThread.getBatch();
                }

            }

        }
    }


    // attempt to save the batch object retrieved from the queue
    private boolean doSave(WriteTask writeTask, BatchObject batchObject) throws AionApiException {
        //Set the new parser state for the kernel at this height
        ParserState kernelState = new ParserState.ParserStateBuilder().id(ParserStateServiceImpl.BLKCHAIN_ID)
                .blockNumber(BigInteger.valueOf(aionService.getBlockNumber()))
                .transactionID(BigInteger.valueOf(-1))
                .build();

        long startBlock = batchObject.getBlocks().get(0).getBlockNumber();
        long endBlock = batchObject.getBlocks().get(batchObject.getBlocks().size() - 1).getBlockNumber();
        TimeLogger.start();
        boolean status = writeTask.executeTask(batchObject, kernelState);
        TimeLogger.logTime(String.format("Wrote blocks in range [(%d, %d)] in {}", startBlock, endBlock ) );

        logSave(String.format("Wrote blocks in range [(%d, %d)]", startBlock, endBlock ) , status);
        return status;
    }


    private boolean checkReorg() throws DbServiceException, AionApiException, SQLException, ReorganizationLimitExceededException {


        boolean shouldReset = reorgService.reorg();// Do reorg and return boolean indicating whether a reset should occur
        GENERAL.trace("Started check for reorg");

        //noinspection ConstantConditions
        if (shouldReset) {

            GENERAL.debug("Reorg occurred");
            readerThread.forceReaderReset();//Force the reader thread to reset and empty its queue

            while (readerThread.waitingForReset()){
                try{
                    GENERAL.debug("Waiting for reader reset.");

                    Thread.sleep(250);

                }
                catch (InterruptedException e){
                    GENERAL.debug("Caught exception: ", e);
                    Thread.currentThread().interrupt();
                }
            }

            GENERAL.debug("Successfully completed the reorg.");

        }

        GENERAL.trace("Finished Check for reorg.");
        //Return the reorg status
        return shouldReset;


    }


    /**
     *
     * @param actionMessage the action that was attempted by the save method
     * @param result a boolean indicating whether an exception was thrown by the save method.
     * @return
     */
    private boolean logSave(String actionMessage, final boolean result){
        if (result) {
            GENERAL.debug("Successful call to save {}", actionMessage);
        } else {
            GENERAL.debug("Threw an exception in save. Failed to {}", actionMessage);

            emailService.send("Threw an exception in save", "Failed to " + actionMessage);
        }

        return result;
    }


    public void kill(){
        keepRunning =false;
    }

    public int getNumReconnectAionService() {
        return numReconnectAionService;
    }

    public void setNumReconnectAionService(int numReconnectAionService) {
        this.numReconnectAionService = numReconnectAionService;
    }

}
