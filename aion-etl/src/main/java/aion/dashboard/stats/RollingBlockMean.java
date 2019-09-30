package aion.dashboard.stats;

import aion.dashboard.blockchain.interfaces.Web3Service;
import aion.dashboard.blockchain.type.APIBlockDetails;
import aion.dashboard.config.Config;
import aion.dashboard.domainobject.Metrics;
import aion.dashboard.domainobject.ParserState;
import aion.dashboard.exception.AionApiException;
import aion.dashboard.exception.Web3ApiException;
import aion.dashboard.service.ParserStateService;

import java.util.List;
import java.util.Optional;

/**
 * Creates a moving window that contains the last max size records
 * The underlying data structure is a concurrentLinkedDeque
 *
 */
public interface RollingBlockMean {

    /**
     * TODO consider replacing factory method with a builder
     * Creates a new rolling block mean with a max size of 4000 and timeWindow of 60 minutes
     * @param blkPointer the start of the moving window
     * @param transactionMeanPointer the start of the transaction moving window
     * @param service the service used to extract the blocks
     * @return an instance of the rolling mean
     * @throws AionApiException
     */
    static RollingBlockMean init(long blkPointer,long transactionMeanPointer,long dbPointer, Web3Service service) throws Web3ApiException {
        Config config = Config.getInstance();

        long transactionTimeWindow = config.getTransactionWindowSize() * 60L; // in minutes
        int blockTimeWindow = config.getBlockWindowStableSize();
        int blockCountWindow = config.getBlockWindowRTSize();
        int blockMaxSize = config.getBlockMaxWindowSize();


        return new RollingBlockMeanImpl(
                blkPointer,
                transactionMeanPointer,
                dbPointer, service,
                blockMaxSize,
                blockTimeWindow,
                transactionTimeWindow,
                blockCountWindow);
    }

    static RollingBlockMean init(ParserStateService psService, Web3Service apiService) throws Web3ApiException {
        Config config = Config.getInstance();

        long transactionTimeWindow = config.getTransactionWindowSize() * 60L; // in minutes
        int blockTimeWindow = config.getBlockWindowStableSize();
        int blockCountWindow = config.getBlockWindowRTSize();
        int blockMaxSize = config.getBlockMaxWindowSize();

        return new RollingBlockMeanImpl(
                psService.readBlockMeanState().getBlockNumber().longValue(),
                psService.readTransactionMeanState().getBlockNumber().longValue(),
                psService.readDBState().getBlockNumber().longValue(),
                apiService,
                blockMaxSize,
                blockTimeWindow,
                transactionTimeWindow,
                blockCountWindow);
    }
    /**
     * Adds a new block to the moving window
     * @param blockDetails
     * @param blockReward
     */
    void add(APIBlockDetails apiBlock);
    void reorg(long consistentBlock);
    long getStartOfBlockWindow();
    long getStartOfTransactionWindow();

    Optional<Metrics> computeStableMetricsFrom(long blockNumber);
    Optional<Metrics> computeRTMetricsFrom(long blockNumber);
    void updateStates();
    List<ParserState> getStates();

}
