package aion.dashboard.blockchain;

import aion.dashboard.blockchain.type.APIBlockDetails;
import aion.dashboard.domainobject.ParserState;
import aion.dashboard.parser.Producer;
import aion.dashboard.service.ParserStateService;
import aion.dashboard.util.Utils;

import com.google.common.base.Stopwatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static aion.dashboard.blockchain.interfaces.Web3Service.getInstance;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

public class ExtractorTest {
    Logger test_logger = LoggerFactory.getLogger("logger_test");

    private Producer<APIBlockDetails> blockDetailsProducer;
    private BlockingQueue<List<APIBlockDetails>> spyQueue;
    private static final long REQUEST_SIZE = 1000;
    private static final int QUEUE_SIZE = 20;

    @BeforeEach
    void setUp(){
        ParserStateService mock = mock(ParserStateService.class);
        spyQueue = Mockito.spy(new ArrayBlockingQueue<>(QUEUE_SIZE));
        doReturn(new ParserState.ParserStateBuilder().blockNumber(BigInteger.valueOf(-1)).id(1).build()).when(mock).readDBState();
        blockDetailsProducer = new Web3Extractor(getInstance(), mock, spyQueue, REQUEST_SIZE);
    }

    @AfterEach
    void tearDown(){
        blockDetailsProducer.stop();
    }

    @Test
    void testMissingBlocks() throws InterruptedException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        blockDetailsProducer.start();
        Utils.awaitResult(spyQueue::size, (i) -> i == QUEUE_SIZE);
        blockDetailsProducer.stop();
        stopwatch.stop();

        test_logger.info("verifying blocks");
        long num = 0L;
        Set<Long> outOfOrderBlocks = new HashSet<>();
        Iterator<APIBlockDetails> blockDetailsIterator;
        Set<Long> expectedBlocks = LongStream.rangeClosed(0, REQUEST_SIZE * QUEUE_SIZE - 1).boxed().collect(Collectors.toSet());
        Set<Long> missingBlocks = new HashSet<>();
        while ((blockDetailsIterator = blockDetailsProducer.peek()) != null && blockDetailsIterator.hasNext()){
            do {
                var block = blockDetailsIterator.next();
                if (block.getNumber() != num) {
                    outOfOrderBlocks.add(num);
                }
                if (!expectedBlocks.contains(block.getNumber())){
                    missingBlocks.add(block.getNumber());
                }
                num++;
            } while (blockDetailsIterator.hasNext());
            blockDetailsProducer.consume();
        }

        if (!outOfOrderBlocks.isEmpty() || !missingBlocks.isEmpty() || num != REQUEST_SIZE * QUEUE_SIZE ) {
            if (!outOfOrderBlocks.isEmpty()) {
                test_logger.info("Expected blocks in order. But blocks were out of order {}.",
                        outOfOrderBlocks.stream().map(Object::toString).collect(Collectors.joining(", ")));
            }

            if (!missingBlocks.isEmpty()){
                test_logger.info("Expected no missing blocks. But no blocks were found for {}.",
                        missingBlocks.stream().map(Object::toString).collect(Collectors.joining(", ")));
            }

            if (num != REQUEST_SIZE * QUEUE_SIZE ){
                test_logger.info("Did not receive the expected # of responses, {}", num);
            }

            fail("API response was incorrect");
        }
        test_logger.info("Completed extraction in: {}ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        test_logger.info("Passed");
    }

    @Test
    void testReset() throws InterruptedException {
        blockDetailsProducer.start();
        Utils.awaitResult(blockDetailsProducer::queueSize, l-> l>1);
        blockDetailsProducer.reset();
        Utils.awaitResult(blockDetailsProducer::shouldReset, b-> !b);
        verify(spyQueue,atLeastOnce()).clear();
        blockDetailsProducer.stop();
    }
}
