package aion.dashboard.worker;

import aion.dashboard.blockchain.AionService;
import aion.dashboard.blockchain.MockAdmin;
import aion.dashboard.blockchain.MockAionApi;
import aion.dashboard.domainobject.BatchObject;
import aion.dashboard.exception.AionApiException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Iterator;

import static aion.dashboard.worker.BlockchainReaderThread.checkBlocksAreSorted;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlockchainReaderThreadTest {


    /**
     * Tests that the queue is being populated by the thread
     */
    void blocksAddedTest() throws InterruptedException {
        BlockchainReaderThread th = new BlockchainReaderThread();


        th.start();

        while (true) {// force sleep in the test to allow the thread to build the queue

            try {
                Thread.sleep(60000);
                break;
            } catch (InterruptedException e) {

            }
        }

        assertTrue(th.getBatchQueue().size() > 0);
        boolean hasBlock =true;
        boolean hasTx = false;
        boolean hasToken = false;


        for (BatchObject b : th.getBatchQueue()) {
            hasBlock = !b.getBlocks().isEmpty();
            if (!hasBlock) break;
            hasTx = hasTx || !b.getTransactions().isEmpty();
            hasToken = hasToken || !b.getTokens().isEmpty();
        }


        assertTrue(hasBlock, "Failed to find blocks in all bactches");
        assertTrue(hasToken, "No tokens found");
        assertTrue(hasTx, "No transactions found");
        th.kill();
        th.join();
    }


    /**
     * Tests that the thread can be cleared by sending a signal externally
     */
    @Test
    void clearQueueTest(){
        BlockchainReaderThread th = new BlockchainReaderThread();

        long initialQueuePointer = th.getQueuePointer();
        th.start();


        while (true) {// force sleep in the test to allow the thread to build the queue

            try {
                Thread.sleep(60000);
                break;
            } catch (InterruptedException e) {

            }
        }

        assertTrue(th.getBatchQueue().size() > 0);//check that the queue has values
        System.out.println("Populated the queue");

        th.forceReaderReset();

        assertTimeout(Duration.ofMillis(1000), ()-> {
            while (true) {// force sleep in the test to allow the thread to build the queue

                if (th.getBatchQueue().isEmpty() && th.getQueuePointer() == initialQueuePointer) {

                    System.out.println("Queue was cleared");
                    break;
                }
            }
        });




    }

    /**
     * Guava comparators is currently in beta however this test proves that checkBlocksAreSorted method works.
     * @throws AionApiException
     */

    @Test
    void blockDetailsSortedTest() throws AionApiException {
        AionService service =AionService.getInstance();
        service.api =  new MockAionApi(MockAdmin.createBuilder().setMode(MockAdmin.AdminMode.UNSORTED));

        assertFalse(checkBlocksAreSorted(service.getBlockDetailsByRange(0L,10L)));

        service.api =  new MockAionApi(MockAdmin.createBuilder().setMode(MockAdmin.AdminMode.VALID));
        assertTrue(checkBlocksAreSorted(service.getBlockDetailsByRange(0L,10L)));
    }
}
