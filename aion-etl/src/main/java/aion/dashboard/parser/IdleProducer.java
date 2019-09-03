package aion.dashboard.parser;

import aion.dashboard.parser.type.Message;
import aion.dashboard.parser.type.TokenBatch;
import aion.dashboard.util.Utils;

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
        workQueue.put(messages);
    }

    protected final List<S> task() throws Exception {
        List<Message<T>> records = getMessage();
        final List<S> tokenBatches = doTask(records);
        consumeMessage();
        return tokenBatches;
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

    protected List<Message<T>> getMessage() throws InterruptedException {
        while (keepLooping()){
            var res= workQueue.peek();
            if (res!=null && !res.isEmpty()){
                return res;
            }else {
                if (res != null){
                    workQueue.poll();// discard any empty elements
                }
                Utils.trySleep(100);
            }
        }

        throw new InterruptedException();
    }


    @Override
    protected boolean keepLooping() {
        return super.keepLooping() || !workQueue.isEmpty();
    }
}
