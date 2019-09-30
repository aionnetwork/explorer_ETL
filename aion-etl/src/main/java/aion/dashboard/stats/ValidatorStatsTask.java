package aion.dashboard.stats;

import aion.dashboard.config.Config;
import aion.dashboard.db.SharedDBLocks;
import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.domainobject.Block;
import aion.dashboard.domainobject.ParserState;
import aion.dashboard.domainobject.SealInfo;
import aion.dashboard.domainobject.ValidatorStats;
import aion.dashboard.service.*;
import aion.dashboard.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ValidatorStatsTask implements Runnable {
    private BlockService blockService;
    private ParserStateService parserStateService;
    private ValidatorService validatorService;
    private static final int BLOCK_TIME_INTERVAL = 360;
    private static final long BLOCK_COUNT = Config.getInstance().getMinerWindowSize();
    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private SharedDBLocks sharedDbLocks = SharedDBLocks.getInstance();

    public ValidatorStatsTask(BlockService blockService, ParserStateService parserStateService, ValidatorService validatorService) {
        this.blockService = blockService;
        this.parserStateService = parserStateService;
        this.validatorService = validatorService;
    }

    public ValidatorStatsTask(){
        blockService= BlockServiceImpl.getInstance();
        parserStateService = ParserStateServiceImpl.getInstance();
        validatorService = ValidatorServiceImpl.getInstance();
    }
    public void start(){
        executorService.submit(this);
    }

    public void stop(){
        executorService.shutdownNow();
    }
    @Override
    public void run(){
        long curr=0;
        // run every minute
        do {
            try {
                curr = parserStateService.readMinerInfoState().getBlockNumber().longValue();
                Thread.currentThread().setName("validator-stats");

                while (!Thread.currentThread().isInterrupted()
                        && canUpdate(curr)) {
                    long end = curr + 359;
                    GENERAL.info("Computing validator stats at block number: {}", end);
                    sharedDbLocks.lockDBWrite();
                    try {
                        List<SealInfo> sealInfo = blockService.getMiningInfo(Math.max(1, end - BLOCK_COUNT+1), end);
                        if (sealInfo.size() > BLOCK_COUNT) {
                            throw new IllegalStateException("Expected " + BLOCK_TIME_INTERVAL +" but found: " + sealInfo.size());
                        }
                        Block block = blockService.getByBlockNumber(end);
                        List<ValidatorStats> stats = MetricsCalc.calculateStats(sealInfo, block);
                        write(new ParserState.ParserStateBuilder()
                                        .blockNumber(BigInteger.valueOf(end))
                                        .id(ParserStateServiceImpl.MINING_INFO_STATE).build(),
                                stats, end);
                        curr = curr + BLOCK_TIME_INTERVAL;
                    }finally {
                        sharedDbLocks.unlockDBWrite();
                    }
                }
            }catch (Exception e){
                GENERAL.warn("Encountered an exception while computing validator metrics at block number: {}", curr + 359);
                GENERAL.warn("Error: ", e);
            }

        }while (Utils.trySleep(60000));
    }

    private boolean canUpdate(long curr) {
        try{
            sharedDbLocks.lockDBWrite();
            return curr + BLOCK_TIME_INTERVAL < parserStateService.readDBState().getBlockNumber().longValue();
        }finally {
            sharedDbLocks.unlockDBWrite();
        }
    }

    private void write(ParserState parserState, List<ValidatorStats> stats, long end) throws SQLException {
        try(Connection con = DbConnectionPool.getConnection()){
            try(PreparedStatement ps = parserStateService.prepare(con, Collections.singletonList(parserState));
                PreparedStatement psDeleteValidators = validatorService.deleteValidatorStats(con, end);
                PreparedStatement psInsert = validatorService.prepare(con, stats)){
                try {
                    ps.executeBatch();
                    psDeleteValidators.executeBatch();
                    psInsert.executeBatch();
                    con.commit();
                }catch (SQLException e){
                    con.rollback();
                    throw e;
                }
            }
        }
    }
}
