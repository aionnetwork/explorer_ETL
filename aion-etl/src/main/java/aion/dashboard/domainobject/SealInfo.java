package aion.dashboard.domainobject;

import aion.dashboard.blockchain.type.APIBlock;

public class SealInfo {
    private final String minerAddress;
    private final long blockNumber;
    private final APIBlock.SealType sealType;
    private final int transactionNum;

    public SealInfo(String minerAddress, long blockNumber, String sealType, int transactionNum) {
        this.minerAddress = minerAddress;
        this.blockNumber = blockNumber;
        this.sealType = APIBlock.SealType.valueOf(sealType.toUpperCase());
        this.transactionNum = transactionNum;
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

    public int getTransactionNum() {
        return transactionNum;
    }
}
