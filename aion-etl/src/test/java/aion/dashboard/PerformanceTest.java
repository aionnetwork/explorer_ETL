package aion.dashboard;

import aion.dashboard.config.Config;
import aion.dashboard.domainobject.BatchObject;
import aion.dashboard.domainobject.ParserState;
import aion.dashboard.service.ParserStateService;
import aion.dashboard.service.ParserStateServiceImpl;
import aion.dashboard.task.WriteTask;
import aion.dashboard.util.Utils;
import aion.dashboard.worker.BlockchainReaderThread;
import aion.dashboard.worker.DBThread;
import com.google.common.base.Stopwatch;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PerformanceTest {


    @Test
    void performanceTest() throws InterruptedException {
        Stopwatch stopwatch = Stopwatch.createStarted();



        var readerThread = new BlockchainReaderThread(new ParserStateService() {
            @Override
            public boolean update(ParserState parser_state) {
                return false;
            }

            @Override
            public boolean updateHeadBlockChain(BigInteger blkNum) {
                return false;
            }

            @Override
            public boolean updateHeadDataBase(BigInteger blkNum, BigInteger txNum) {
                return false;
            }

            @Override
            public boolean updateHeadIntegrity(BigInteger blkNum) {
                return false;
            }

            @Override
            public boolean updateBlockMeanState(BigInteger blkNum) {
                return false;
            }

            @Override
            public boolean updateGraphingState(BigInteger blkNum) {
                return false;
            }

            @Override
            public boolean updateAll(List<ParserState> parser_stateList) {
                return false;
            }

            @Override
            public List<ParserState> readState() {
                return null;
            }

            @Override
            public ParserState readDBState() {
                return new ParserState.parserStateBuilder().blockNumber(BigInteger.valueOf(-1L)).id(1).build();
            }

            @Override
            public ParserState readChainState() {
                return new ParserState.parserStateBuilder().blockNumber(BigInteger.valueOf(-0L)).id(1).build();
            }

            @Override
            public ParserState readIntegrityState() {
                return new ParserState.parserStateBuilder().blockNumber(BigInteger.valueOf(-0L)).id(1).build();
            }

            @Override
            public ParserState readGraphingState() {
                return new ParserState.parserStateBuilder().blockNumber(BigInteger.valueOf(-1L)).id(1).build();
            }

            @Override
            public ParserState readBlockMeanState() {
                return new ParserState.parserStateBuilder().blockNumber(BigInteger.valueOf(-1L)).id(1).build();
            }

            @Override
            public ParserState readTransactionMeanState() {
                return new ParserState.parserStateBuilder().blockNumber(BigInteger.valueOf(-1L)).id(1).build();
            }

            @Override
            public PreparedStatement prepare(Connection con, List<ParserState> parser_stateList) {
                return null;
            }
        });
        var dbThread = new DBThread(readerThread, (batchObject, chainState) -> true);



        readerThread.start();
        dbThread.start();


        Utils.awaitResult(readerThread::getQueuePointer, (t)-> t == Config.getInstance().getMaxHeight());


        stopwatch.stop();

        System.out.println("Read and parsed blocks in: "+ stopwatch.elapsed(TimeUnit.SECONDS)+"s");


        readerThread.interrupt();
        dbThread.interrupt();

    }

}
