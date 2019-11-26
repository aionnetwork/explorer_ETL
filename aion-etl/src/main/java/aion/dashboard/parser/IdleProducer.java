package aion.dashboard.parser;

import aion.dashboard.parser.type.Message;
import aion.dashboard.parser.type.TokenBatch;
import aion.dashboard.util.Utils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 *
 * @param <S> The output that this class must produce
 * @param <T> The input this class must accept
 */
public abstract class IdleProducer<S,T> extends Producer<S> {



    private BlockingQueue<List<Message<T>>> workQueue;

    public IdleProducer(BlockingQueue<List<S>> queue, BlockingQueue<List<Message<T>>> workQueue) {
        super(queue);
        this.workQueue = workQueue;
    }

    public int workQueueSize(){
        return workQueue.size();
    }

    public void submitAll(List<Message<T>> messages) throws InterruptedException {
        while (!Thread.currentThread().isInterrupted()){
            if (!shouldReset.get() && workQueue.offer(messages)) break;
            else Thread.sleep(10);
        }
    }

    protected final List<S> task() throws Exception {
        List<Message<T>> records = getMessage();
        if (records == null) return Collections.emptyList();
        else {
            final List<S> batches = doTask(records);
            consumeMessage();
            return batches;
        }
    }
    protected abstract List<S> doTask(List<Message<T>> messages) throws Exception;
    @Override
    public void doReset(){
        workQueue.clear();
        queue.clear();

        shouldReset.compareAndSet(true, false);
    }

    private List<Message<T>> consumeMessage(){
        return workQueue.poll();
    }

    protected List<Message<T>> getMessage() {
        return workQueue.peek();
    }


    @Override
    protected boolean keepLooping() {
        return super.keepLooping() || !workQueue.isEmpty();
    }
}
