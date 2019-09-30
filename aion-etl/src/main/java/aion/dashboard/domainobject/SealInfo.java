package aion.dashboard.domainobject;

import aion.dashboard.blockchain.type.APIBlock;

public class SealInfo {
    private final String minerAddress;
    private final long blockNumber;
    private final APIBlock.SealType sealType;

    public SealInfo(String minerAddress, long blockNumber, String sealType) {
        this.minerAddress = minerAddress;
        this.blockNumber = blockNumber;
        this.sealType = APIBlock.SealType.valueOf(sealType.toUpperCase());
    }

    public String getMinerAddress() {
        return minerAddress;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public APIBlock.SealType getSealType() {
        return sealType;
    }
}
