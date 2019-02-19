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
import aion.dashboard.task.WriteTaskImpl;
import aion.dashboard.task.WriteTask;
import aion.dashboard.util.TimeLogger;
import aion.dashboard.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.SQLException;


public class DBThread extends Thread {

    private static final TimeLogger TIME_LOGGER = new TimeLogger("DBThread");
    private BlockchainReaderThread readerThread;
    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");
    private final long pollDelay;
    private final long errDelay;
    private EmailService emailService;
    private boolean keepRunning;
    private AionService aionService;

    private int numReconnectAionService;
    private final WriteTask writeTask;

    private ReorgService reorgService;

    public DBThread(BlockchainReaderThread readerThread){
        super("DB Thread");
        Config config = Config.getInstance();
        this.readerThread = readerThread;
        aionService = AionService.getInstance();


        pollDelay = config.getReaderPollDelay();
        errDelay = config.getReaderErrPollDelay();
        emailService = EmailService.getInstance();

        ParserStateService parserService = ParserStateServiceImpl.getInstance();
        reorgService = new ReorgServiceImpl(aionService, parserService);
        keepRunning = true;
        writeTask = WriteTaskImpl.getInstance();
    }


    public DBThread(BlockchainReaderThread readerThread, WriteTaskImpl writeTask){
        super("DB Thread");
        Config config = Config.getInstance();
        this.readerThread = readerThread;
        aionService = AionService.getInstance();


        pollDelay = config.getReaderPollDelay();
        errDelay = config.getReaderErrPollDelay();
        emailService = EmailService.getInstance();

        ParserStateService parserService = ParserStateServiceImpl.getInstance();
        reorgService = new ReorgServiceImpl(aionService, parserService);
        keepRunning = true;
        this.writeTask = writeTask;
    }



    @Override
    public void run(){


        waitForInit();
        while (!currentThread().isInterrupted() && readerThread.isAlive() && keepRunning){
            try {
                GENERAL.info("-------------------------");
                GENERAL.info("Database Thread");
                GENERAL.info("-------------------------");

                if (!Utils.trySleep(errDelay)) break;

                if(!aionService.isConnected()) ++numReconnectAionService;

                aionService.reconnect();

                readQueue(writeTask);
            }
            catch (ReorganizationLimitExceededException e){
                GENERAL.error("Exceeded Reorg Limit", e);

                emailService.send("EXCEEDED REORG LIMIT", "Threw an exception in reorg service.");
            }
            catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
            catch (Exception e){

                GENERAL.debug("Caught top level exception: ", e);
            }

        }

        GENERAL.info("Shutting down DBThread");



        GENERAL.info("Successfully shutdown DBThread");

    }

    //Continuously check the queue for any values that can be saved
    private void readQueue(WriteTask writeTask) throws DbServiceException, AionApiException, SQLException,
            ReorganizationLimitExceededException, InterruptedException {
        while (!currentThread().isInterrupted() && readerThread.isAlive() && keepRunning){

            if(!Utils.trySleep(pollDelay)) break;


            BatchObject batchObject=readerThread.getBatch();


            doSave(writeTask, batchObject);

        }
    }

    //Wait for the reader thread to start running
    private void waitForInit() {
        while (readerThread.getState() == State.NEW && keepRunning){
            GENERAL.debug("Waiting for reader to start");
            if (!Utils.trySleep(10)) break;
        }
    }

    //Attempt to save all stored values in the queue if the first is not null
    private void doSave(WriteTask writeTask, BatchObject batchObject) throws DbServiceException, AionApiException,
            SQLException, ReorganizationLimitExceededException, InterruptedException {


        while (batchObject!=null && keepRunning && !checkReorg()){
            ParserState kernelState = readKernelState();


            long startBlock = batchObject.getBlocks().get(0).getBlockNumber();
            long endBlock = batchObject.getBlocks().get(batchObject.getBlocks().size() - 1).getBlockNumber();
            TIME_LOGGER.start();
            boolean status = writeTask.executeTask(batchObject, kernelState);
            TIME_LOGGER.logTime(String.format("Wrote blocks in range [(%d, %d)] in {}", startBlock, endBlock ) );

            logSave(String.format("Wrote blocks in range [(%d, %d)]", startBlock, endBlock ) , status);

            if( status ){//only get the next batch if the write was successful otherwise reattempt
                batchObject = readerThread.getBatch();
            }



        }
    }

    private ParserState readKernelState() throws InterruptedException {
        do {
           try {
               //Set the new parser state for the kernel at this height
               return new ParserState.parserStateBuilder().id(ParserStateServiceImpl.BLKCHAIN_ID)
                       .blockNumber(BigInteger.valueOf(aionService.getBlockNumber()))
                       .build();
           }
           catch (AionApiException e){
               try {
                   aionService.reconnect();
               } catch (AionApiException ignore) {
                   //Just ignore this exception
               }
               GENERAL.error("Caught an API exception while calling getBlockNumber.");

           }
       } while(!Thread.currentThread().isInterrupted());
        Thread.currentThread().interrupt();
        throw new InterruptedException("Request to update parser state was interrupted;");
    }


    private boolean checkReorg() throws DbServiceException, AionApiException, SQLException, ReorganizationLimitExceededException {


        boolean shouldReset = reorgService.reorg();// Do reorg and return boolean indicating whether a reset should occur


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
