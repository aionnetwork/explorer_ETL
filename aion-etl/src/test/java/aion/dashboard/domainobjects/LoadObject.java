package aion.dashboard.domainobjects;

/**
 * Encapsulates the load associated with a particular load test
 */

public class LoadObject {

    public final int BatchSize;
    public final int TrxCount;
    public final int Delay;

    public LoadObject(int batchSize, int trxCount, int delay) {
        BatchSize = batchSize;
        TrxCount = trxCount;
        Delay = delay;
    }
}
