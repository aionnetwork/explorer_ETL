package aion.dashboard.consumer;

import aion.dashboard.db.SharedDBLocks;
import aion.dashboard.domainobject.ParserState;
import aion.dashboard.parser.Producer;
import aion.dashboard.parser.type.*;
import aion.dashboard.service.ParserStateServiceImpl;
import aion.dashboard.service.ReorgService;
import aion.dashboard.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Consumer {

    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");
    private final Producer<ParserBatch> blockProducer;
    private final Producer<TokenBatch> tokenProducer;
    private final Producer<AccountBatch> accountProducer;
    private final WriteTask<ParserBatch> blockWriter;
    private final WriteTask<AccountBatch> accountWriter;
    private final WriteTask<TokenBatch> tokenWriter;
    private final WriteTask<InternalTransactionBatch> internalTransactionWriterWriteTask;
    private final ReorgService service;
    private final Producer<InternalTransactionBatch> internalTransactionProducer;
    private final ScheduledExecutorService workers = Executors.newScheduledThreadPool(4);
    private AtomicBoolean stopRunning = new AtomicBoolean(false);
    private AtomicReference<BigInteger> DB_HEIGHT = new AtomicReference<>(BigInteger.ZERO);
    private final List<Producer> producerList;
    private SharedDBLocks sharedDbLocks = SharedDBLocks.getInstance();

    Consumer(Producer<ParserBatch> blockProducer,
             Producer<TokenBatch> tokenProducer,
             Producer<AccountBatch> accountProducer,
             WriteTask<ParserBatch> blockWriter,
             WriteTask<AccountBatch> accountWriter,
             WriteTask<TokenBatch> tokenWriter,
             WriteTask<InternalTransactionBatch> internalTransactionWriterWriteTask,
             ReorgService service, Producer<InternalTransactionBatch> internalTransactionProducer) {

        this.blockProducer = blockProducer;
        this.tokenProducer = tokenProducer;
        this.accountProducer = accountProducer;
        this.blockWriter = blockWriter;
        this.accountWriter = accountWriter;
        this.tokenWriter = tokenWriter;
        this.internalTransactionWriterWriteTask = internalTransactionWriterWriteTask;
        this.service = service;
        this.internalTransactionProducer = internalTransactionProducer;
        producerList = List.of(blockProducer, accountProducer, tokenProducer, internalTransactionProducer);
    }


    public void start() {
        workers.scheduleWithFixedDelay(() -> {
            try {
                this.reorg();
            } catch (Exception e) {
                GENERAL.warn("The reorg failed with the exception: ",e);
            }
        }, 0, 10, TimeUnit.SECONDS);
        workers.submit(this::consumeBlocks);
        workers.submit(this::consumeTokens);
        workers.submit(this::consumeAccounts);
        workers.submit(this::consumeItx);
    }


    private boolean keepRunning(Producer producer) {
        return (!Thread.currentThread().isInterrupted() && !stopRunning.get()) || producer.peek().hasNext();
    }

    public void stop() {
        try {
            stopRunning.set(true);
            workers.shutdown();
        } finally {
            try {
                workers.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            workers.shutdownNow();
        }
    }

    private <T extends AbstractBatch> void doWrites(Producer<T> producer, WriteTask<T> writer) throws Exception {
        lockDBWrite();
        var record = producer.peek();
        try {
            while (record!=null && record.hasNext()) {
                var batch = record.next();
                checkpoint(batch.getState());// pause execution if this is a token, account or itx consumer
                GENERAL.debug("Attempting to write batch with block number: {}", batch.getState().getBlockNumber());
                try {
                    writer.write(batch);
                } catch (Exception e) {
                    GENERAL.warn("Caught exception: ", e);
                    throw e;
                }
                if (batch.getState().getId() == ParserStateServiceImpl.DB_ID){
                    DB_HEIGHT.set(batch.getState().getBlockNumber());//signal the new DB height
                }
            }
        }finally {
            unlockDBWrite();
        }
    }

    private void checkpoint(ParserState state) {
        while (state.getId() != ParserStateServiceImpl.DB_ID &&
                state.getBlockNumber().compareTo(DB_HEIGHT.get()) > 0) {// ensure that the main chain is stored first
            if (!Utils.trySleep(50)) Thread.currentThread().interrupt();
        }
    }


    private void consumeItx(){
        Thread.currentThread().setName("itx-loader");
        load(internalTransactionProducer, internalTransactionWriterWriteTask);
        GENERAL.info("Ended Internal transaction loader");
    }

    private void consumeAccounts() {
        Thread.currentThread().setName("accounts-loader");
        load(accountProducer, accountWriter);
        GENERAL.info("Ended Accounts loader");
    }

    private void consumeBlocks() {
        Thread.currentThread().setName("block-loader");
        load(blockProducer, blockWriter);
        GENERAL.info("Ended block loader");
    }

    private void consumeTokens() {
        Thread.currentThread().setName("tokens-loader");
        load(tokenProducer, tokenWriter);
        GENERAL.info("Ended Accounts loader");

    }

    private <T extends AbstractBatch> void load(Producer<T> producer, WriteTask<T> writeTask) {
        while (keepRunning(producer)) {
            try {
                getMessage(producer);

                doWrites(producer, writeTask);
                producer.consume();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                GENERAL.error("Failed to load blocks due to exception:  ",e);
                e.printStackTrace();
            }
        }
    }


    private <T> Iterator<T> getMessage(Producer<T> producer) throws InterruptedException {
        while (keepRunning(producer)) {
            var res = producer.peek();

            if (res != null && res.hasNext()) return res;
            else Utils.trySleep(100);


        }
        throw new InterruptedException();

    }

    private void reset() {
        for (var producer: producerList){
            producer.reset();
        }

        while (producerList.stream().anyMatch(Producer::shouldReset) && !Thread.currentThread().isInterrupted()) {
            Utils.trySleep(100);
        }

    }


    private void reorg() throws Exception {
        lockReorg();

        try {
            Thread.currentThread().setName("Reorg-Th");
            GENERAL.info("Checking for chain inconsistencies.");

            while (service.reorg()) {// if a reorg occured ensure that the blocks below
                //the max depth are also valid
                reset();
            }

        } finally {
            unlockReorg();
        }
    }


    private void lockReorg() {
        sharedDbLocks.lockReorg();
    }

    private void lockDBWrite() {
        sharedDbLocks.lockDBWrite();
    }


    private void unlockReorg() {
        sharedDbLocks.unlockReorg();
    }

    private void unlockDBWrite() {
        sharedDbLocks.unlockDBWrite();
    }


}
