package aion.dashboard.stats;

import aion.dashboard.blockchain.interfaces.Web3Service;
import aion.dashboard.domainobject.ParserState;
import aion.dashboard.exception.Web3ApiException;
import aion.dashboard.service.ParserStateService;
import aion.dashboard.stats.RollingBlockMean;
import aion.dashboard.stats.RollingBlockMeanImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class RollingBlockMeanImplTest {

    private RollingBlockMeanImpl rollingBlockMean;
    private final static ParserStateService mock = Mockito.mock(ParserStateService.class);
    @BeforeAll
    static void setUpAll(){
        Mockito.doReturn(new ParserState.ParserStateBuilder().blockNumber(BigInteger.valueOf(10_000L)).build()).when(mock).readDBState();
        Mockito.doReturn(new ParserState.ParserStateBuilder().blockNumber(BigInteger.valueOf(8000)).build()).when(mock).readBlockMeanState();
        Mockito.doReturn(new ParserState.ParserStateBuilder().blockNumber(BigInteger.valueOf(6000)).build()).when(mock).readTransactionMeanState();
    }

    @BeforeEach
    void setup() throws Web3ApiException {
        rollingBlockMean = (RollingBlockMeanImpl) RollingBlockMean.init(mock, Web3Service.getInstance());
    }

    @Test
    void findBlocksInRange() {
        var rtBlocks = rollingBlockMean.findBlocksInRange(10_000L - 32, 10_000L);
        assertEquals(32, rtBlocks.size());
        var listOf2kBlocks = rollingBlockMean.findBlocksInRange(0, 10_000L);
        assertEquals(2001, listOf2kBlocks.size());
    }
}