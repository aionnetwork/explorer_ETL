package aion.dashboard.service;

import aion.dashboard.blockchain.AionService;
import aion.dashboard.config.Config;
import aion.dashboard.domainobject.Graphing;
import aion.dashboard.domainobject.Metrics;
import aion.dashboard.domainobject.ParserState;
import aion.dashboard.exception.AionApiException;
import org.aion.api.type.BlockDetails;

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
    static RollingBlockMean init(long blkPointer,long transactionMeanPointer,long dbPointer, AionService service) throws AionApiException {
        Config config = Config.getInstance();

        long transactionTimeWindow = config.getTransactionWindowSize() * 60L; // in minutes
        int blockTimeWindow = config.getBlockWindowStableSize();
        int blockCountWindow = config.getBlockWindowCountSize();
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





    /**
     * Adds a new block to the moving window
     * @param blockDetails
     */
    void add(BlockDetails blockDetails);
    void reorg(long consistentBlock);

    /**
     *
     * Compute the mean from the current first block
     * This method is not thread-safe as no guarantees can be made as to which is the first block
     * @return the metrics at this time
     */
    Optional<List<Graphing>> compute();


    long getStartOfBlockWindow();
    long getStartOfTransactionWindow();

    Optional<Metrics> computeStableMetricsFrom(long blockNumber);
    Optional<Metrics> computeRTMetricsFrom(long blockNumber);

    List<ParserState> getStates();

}
