package aion.dashboard.domainobject;

import java.math.BigDecimal;
import java.math.MathContext;

public class ValidatorStats {
    private final long blockNumber;
    private final String minerAddress;
    private final String sealType;
    private final int blockCount;
    private final long blockTimestamp;
    private final BigDecimal percentageOfBlocksValidated;

    public ValidatorStats(long blockNumber, String minerAddress, String sealType, int blockCount, long blockTimestamp, long totalBlockCount) {
        this.blockNumber = blockNumber;
        this.minerAddress = minerAddress;
        this.sealType = sealType;
        this.blockCount = blockCount;
        this.blockTimestamp = blockTimestamp;
        this.percentageOfBlocksValidated = BigDecimal.valueOf(blockCount)
                .scaleByPowerOfTen(2)
                .divide(BigDecimal.valueOf(totalBlockCount), MathContext.DECIMAL32);
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public String getMinerAddress() {
        return minerAddress;
    }

    public String getSealType() {
        return sealType;
    }

    public int getBlockCount() {
        return blockCount;
    }

    public long getBlockTimestamp() {
        return blockTimestamp;
    }

    public BigDecimal getPercentageOfBlocksValidated() {
        return percentageOfBlocksValidated;
    }

}
