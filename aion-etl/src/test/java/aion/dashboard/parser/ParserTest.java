package aion.dashboard.parser;

import aion.dashboard.blockchain.Web3Extractor;
import aion.dashboard.blockchain.interfaces.Web3Service;
import aion.dashboard.domainobject.ParserState;
import aion.dashboard.exception.Web3ApiException;
import aion.dashboard.service.ParserStateService;
import aion.dashboard.stats.RollingBlockMean;
import aion.dashboard.util.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigInteger;
import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class ParserTest {
    Parser parser;
    Web3Extractor extractor;
    @BeforeEach
    void setUp() throws InterruptedException, Web3ApiException {
        AccountParser accountParser = Mockito.mock(AccountParser.class);
        TokenParser tokenParser = Mockito.mock(TokenParser.class);
        InternalTransactionParser itxParser = Mockito.mock(InternalTransactionParser.class);
        ParserStateService service = Mockito.mock(ParserStateService.class);
        Mockito.doNothing().when(accountParser).submitAll(any());
        Mockito.doNothing().when(itxParser).submitAll(any());
        Mockito.doNothing().when(tokenParser).submitAll(any());
        Mockito.doReturn(new ParserState.ParserStateBuilder().blockNumber(BigInteger.ZERO).id(1).build()).when(service).readDBState();
        Mockito.doReturn(new ParserState.ParserStateBuilder().blockNumber(BigInteger.ZERO).id(5).build()).when(service).readBlockMeanState();
        Mockito.doReturn(new ParserState.ParserStateBuilder().blockNumber(BigInteger.ZERO).id(6).build()).when(service).readTransactionMeanState();
        RollingBlockMean blockMean = RollingBlockMean.init(service, Web3Service.getInstance());
        extractor = new Web3Extractor(Web3Service.getInstance(), service, new ArrayBlockingQueue<>(5), 100);
        extractor.start();
        parser = new ParserBuilder()
                .setAccountProd(accountParser)
                .setTokenProd(tokenParser)
                .setInternalTransactionProducer(itxParser)
                .setQueue(new ArrayBlockingQueue<>(5))
                .setExtractor(extractor)
                .setApiService(Web3Service.getInstance())
                .setRollingBlockMean(blockMean)
                .createParser();
        parser = Mockito.spy(parser);
    }

    @AfterEach
    void tearDown(){
        parser.stop();
        extractor.stop();
    }

    /**
     * Ensures that the parser does not fail when starting at 0
     * @throws InterruptedException
     */
    @Test
    void runParser() throws InterruptedException {
        parser.start();
        assertTimeout(Duration.ofSeconds(10),()->Utils.awaitResult(()-> parser.queue.isEmpty(), t-> !t));
    }

}