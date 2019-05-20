package aion.dashboard.consumer;

import aion.dashboard.parser.Producer;
import aion.dashboard.parser.type.AbstractBatch;
import aion.dashboard.parser.type.AccountBatch;
import aion.dashboard.parser.type.ParserBatch;
import aion.dashboard.parser.type.TokenBatch;
import aion.dashboard.service.ReorgService;
import aion.dashboard.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Consumer {

    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");
    private final Producer<ParserBatch> blockProducer;
    private final Producer<TokenBatch> tokenProducer;
    private final Producer<AccountBatch> accountProducer;
    private final WriteTask<ParserBatch> blockWriter;
    private final WriteTask<AccountBatch> accountWriter;
    private final WriteTask<TokenBatch> tokenWriter;
    private final ReorgService service;
    private final ReadWriteLock dbLock = new ReentrantReadWriteLock();
    private final ScheduledExecutorService workers = Executors.newScheduledThreadPool(4);
    private AtomicBoolean stopRunning = new AtomicBoolean(false);

    Consumer(Producer<ParserBatch> blockProducer,
             Producer<TokenBatch> tokenProducer,
             Producer<AccountBatch> accountProducer,
             WriteTask<ParserBatch> blockWriter,
             WriteTask<AccountBatch> accountWriter,
             WriteTask<TokenBatch> tokenWriter,
             ReorgService service) {

        this.blockProducer = blockProducer;
        this.tokenProducer = tokenProducer;
        this.accountProducer = accountProducer;
        this.blockWriter = blockWriter;
        this.accountWriter = accountWriter;
        this.tokenWriter = tokenWriter;
        this.service = service;
    }


    public void start() {
        workers.submit(this::consumeBlocks);
        workers.submit(this::consumeTokens);
        workers.submit(this::consumeAccounts);
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

    private <T extends AbstractBatch> void doWrites(Iterator<T> record, WriteTask<T> writer) throws Exception {
        lockDBWrite();
        try {
            while (record.hasNext()) {
                var batch = record.next();
                GENERAL.debug("Attempting to write batch with block number: {}", batch.getState().getBlockNumber());
                try {
                    writer.write(batch);
                } catch (Exception e) {
                    GENERAL.warn("Caught exception: ", e);
                    throw e;
                }
            }
        }finally {
            unlockDBWrite();
        }
    }

    private void consumeAccounts() {
        Thread.currentThread().setName("accounts-loader");
        load(accountProducer, accountWriter);
        GENERAL.info("Ended Accounts loader");
    }

    private void consumeBlocks() {
        while (keepRunning(blockProducer)) {
            try {
                reorg();
                Thread.currentThread().setName("blocks-loader");

                var message = getMessage(blockProducer);

                doWrites(message, blockWriter);
                blockProducer.consume();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                // ignore for now
            }
        }        GENERAL.info("Ended block loader");
    }

    private void consumeTokens() {
        Thread.currentThread().setName("tokens-loader");
        load(tokenProducer, tokenWriter);
        GENERAL.info("Ended Accounts loader");

    }

    private <T extends AbstractBatch> void load(Producer<T> producer, WriteTask<T> writeTask) {
        while (keepRunning(producer)) {
            try {
                var message = getMessage(producer);

                doWrites(message, writeTask);
                producer.consume();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                // ignore for now
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
        blockProducer.reset();
        tokenProducer.reset();
        accountProducer.reset();

        while (blockProducer.shouldReset() && !Thread.currentThread().isInterrupted()) {
            Utils.trySleep(100);
        }

    }


    private void reorg() throws Exception {
        lockReorg();

        try {
            Thread.currentThread().setName("Reorg-Th");

            if (service.reorg()) {
                reset();
            }

        } finally {
            unlockReorg();
        }
    }


    private void lockReorg() {
        dbLock.writeLock().lock();
    }

    private void lockDBWrite() {
        dbLock.readLock().lock();
    }


    private void unlockReorg() {
        dbLock.writeLock().unlock();
    }

    private void unlockDBWrite() {
        dbLock.readLock().unlock();
    }


}
