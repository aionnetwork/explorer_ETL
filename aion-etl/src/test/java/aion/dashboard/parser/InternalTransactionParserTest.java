package aion.dashboard.parser;

import aion.dashboard.blockchain.AionService;
import aion.dashboard.blockchain.Web3ServiceImpl;
import aion.dashboard.blockchain.interfaces.Web3Service;
import aion.dashboard.exception.AionApiException;
import aion.dashboard.parser.type.Message;
import aion.dashboard.util.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class InternalTransactionParserTest {
    private AccountParser spiedAccountParser = Mockito.spy(new AccountParser(new LinkedBlockingDeque<>(), Web3ServiceImpl.getInstance(), new LinkedBlockingDeque<>()));
    private InternalTransactionParser parser = new InternalTransactionParser(new LinkedBlockingDeque<>(), new LinkedBlockingDeque<>(), Web3Service.getInstance(), spiedAccountParser);

    private AionService service = AionService.getInstance();

    @BeforeEach
    void setUp(){
        parser.start();
        Mockito.doNothing().when(spiedAccountParser).submitAll(any());
    }
    @AfterEach
    void tearDown(){
        parser.reset();
    }

    @Test
    void testParseOne() throws AionApiException {
        var blockDetails = service.getBlockDetailsByRange(3671014,3671014);

        parser.submitAll(Collections.singletonList(new Message<Void>(null, blockDetails.get(0), null)));
        assertTimeout(Duration.ofSeconds(60), ()-> Utils.awaitResult(parser::peek, Iterator::hasNext));
        var results = parser.peek();
        while (results.hasNext()){
            var res = results.next();
            assertFalse(res.getInternalTransactions().isEmpty());
            System.out.println(res.getInternalTransactions());
        }
    }

    @Test
    void testParseEmptyTx() throws AionApiException {
        var blockDetails = service.getBlockDetailsByRange(778366,778366);

        parser.submitAll(Collections.singletonList(new Message<Void>(null, blockDetails.get(0), null)));
        assertTimeout(Duration.ofSeconds(60), ()-> Utils.awaitResult(parser::workQueueSize, i-> i==0));
        var results = parser.peek();
        assertFalse(results.hasNext());
    }

    @Test
    void testParseEmptyBlock() throws AionApiException{
        var blockDetails = service.getBlockDetailsByRange(778367,778367);

        parser.submitAll(Collections.singletonList(new Message<Void>(null, blockDetails.get(0), null)));
        assertTimeout(Duration.ofSeconds(60), ()-> Utils.awaitResult(parser::workQueueSize, i-> i==0));
        var results = parser.peek();
        assertFalse(results.hasNext());
    }

    @Test
    void testPerformance() throws AionApiException {
        long timeToComplete = 500;
        parser.stop();
        for (long i = 1_300_000; i< 1_400_000;){
            List<Message<Void>> messages = new ArrayList<>();
            var blockDetails = service.getBlockDetailsByRange(i, i+999);
            for (var blockDetail : blockDetails){
                messages.add(new Message<>(null, blockDetail, null));
            }
            i+=1000;
            parser.submitAll(messages);
        }
        parser.start();
        assertTimeout(Duration.ofSeconds(timeToComplete), ()-> Utils.awaitResult(parser::workQueueSize, i-> i==0));
    }
}