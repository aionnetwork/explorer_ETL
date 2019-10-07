package aion.dashboard.consumer;

import aion.dashboard.db.SharedDBLocks;
import aion.dashboard.domainobject.ParserState;
import aion.dashboard.parser.Producer;
import aion.dashboard.parser.type.*;
import aion.dashboard.service.ParserStateServiceImpl;
import aion.dashboard.service.ReorgService;
import aion.dashboard.util.Tuple2;
import aion.dashboard.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Consumer {

    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");
    private final ReorgService service;
    private final ExecutorService workers = Executors.newSingleThreadScheduledExecutor();
    private AtomicBoolean stopRunning = new AtomicBoolean(false);
    private AtomicReference<BigInteger> DB_HEIGHT = new AtomicReference<>(BigInteger.ZERO);
    private final List<Tuple2<Producer, WriteTask>> producerWriteTask;
    private SharedDBLocks sharedDbLocks = SharedDBLocks.getInstance();

    Consumer(Producer<ParserBatch> blockProducer,
             Producer<TokenBatch> tokenProducer,
             Producer<AccountBatch> accountProducer,
             WriteTask<ParserBatch> blockWriter,
             WriteTask<AccountBatch> accountWriter,
             WriteTask<TokenBatch> tokenWriter,
             WriteTask<InternalTransactionBatch> internalTransactionWriterWriteTask,
             ReorgService service, Producer<InternalTransactionBatch> internalTransactionProducer) {
        this.service = service;
        producerWriteTask = List.of(
                new Tuple2<>(blockProducer, blockWriter),
                new Tuple2<>(accountProducer, accountWriter),
                new Tuple2<>(tokenProducer, tokenWriter),
                new Tuple2<>(internalTransactionProducer, internalTransactionWriterWriteTask));
    }


    public void start() {
        workers.submit(this::consumeAll);
    }

    private void consumeAll(){
        final String threadName = "Consumer";
        Thread.currentThread().setName(threadName);
        GENERAL.info("Starting Consumer");
        while (keepRunning()){
            try {
                reorg();
                for(var producerWriterPair: producerWriteTask){
                    //noinspection unchecked
                    doWrites(producerWriterPair._1(),producerWriterPair._2());
                }
            } catch (Exception e) {
                Thread.currentThread().setName("Loader");
                GENERAL.error("Caught exception: ", e);
            }
        }
        Thread.currentThread().setName(threadName);
        GENERAL.debug(threadName);
    }

    private boolean keepRunning(){
        return Utils.trySleep(1_000L) ||
                producerWriteTask.stream().map(Tuple2::_1).anyMatch(p-> p.queueSize()>0) ||
                !stopRunning.get();
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
        Thread.currentThread().setName(writer.toString());
        var record = producer.peek();
        try {
            while (record!=null && record.hasNext()) {
                var batch = record.next();
                if (shouldWait(batch.getState())) return;// pause execution if this is a token, account or itx consumer
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
            producer.consume();
        }finally {
            unlockDBWrite();
        }
    }

    private boolean shouldWait(ParserState state) {
        return state.getId() != ParserStateServiceImpl.DB_ID &&
                state.getBlockNumber().compareTo(DB_HEIGHT.get()) > 0;
    }

    private void reset() {
        List<Producer> producerList = producerWriteTask.stream()
                .map(Tuple2::_1)
                .collect(Collectors.toUnmodifiableList());
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
            Thread.currentThread().setName("Reorg");
            while (service.reorg()) {// if a reorg occurred ensure that the blocks below
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
