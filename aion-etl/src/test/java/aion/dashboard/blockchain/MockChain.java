package aion.dashboard.blockchain;

import org.aion.api.IChain;
import org.aion.api.type.ApiMsg;
import org.aion.api.type.Block;
import org.aion.api.type.BlockDetails;
import org.aion.base.type.Address;
import org.aion.base.type.Hash256;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
/**
 * Reads the blkDetails list that is used by the mock admin to determine the block number and the block details by number.
 */
public class MockChain implements IChain {

    List<BlockDetails> blkDetails;
    public MockChain(List<BlockDetails> blockDetails){
        blkDetails = blockDetails;
    }
    @Override
    public ApiMsg blockNumber() {
        Long l = blkDetails.get(blkDetails.size() -1).getNumber();

        return new ApiMsg(l, ApiMsg.cast.LONG);
    }

    @Override
    public ApiMsg getBalance(Address address) {
        throw new UnsupportedOperationException("Call to unsupported method in mocking class");
    }

    @Override
    public ApiMsg getBalance(Address address, long l) {
        throw new UnsupportedOperationException("Call to unsupported method in mocking class");
    }

    @Override
    public ApiMsg getNonce(Address address) {
        return null;
    }

    @Override
    public ApiMsg getBlockByNumber(long l) {
        Optional<BlockDetails> b = blkDetails.parallelStream().filter(e -> e.getNumber() == l).findAny();
        if (b.isPresent()){
            BlockDetails details = b.get();
            Block.BlockBuilder builder = new Block.BlockBuilder();
            builder.txHash(new ArrayList<>())
                    .size(details.getSize())
                    .receiptTxRoot(details.getReceiptTxRoot())
                    .txTrieRoot(details.getTxTrieRoot())
                    .stateRoot(details.getStateRoot())
                    .miner(details.getMinerAddress())
                    .totalDifficulty(details.getTotalDifficulty())
                    .nonce(details.getNonce())
                    .difficulty(details.getDifficulty())
                    .parentHash(details.getParentHash())
                    .hash(details.getHash())
                    .solution(details.getSolution())
                    .extraData(details.getExtraData())
                    .nrgLimit(details.getNrgLimit())
                    .nrgConsumed(details.getNrgConsumed())
                    .timestamp(details.getTimestamp())
                    .size(details.getSize())
                    .bloom(details.getBloom())
                    .number(details.getNumber());
            return new ApiMsg(builder.createBlock(),ApiMsg.cast.OTHERS);

        }else {
            return new ApiMsg(-1);
        }
    }

    @Override
    public ApiMsg getTransactionByBlockHashAndIndex(Hash256 hash256, int i) {
        throw new UnsupportedOperationException("Call to unsupported method in mocking class");
    }

    @Override
    public ApiMsg getTransactionByBlockNumberAndIndex(long l, int i) {
        throw new UnsupportedOperationException("Call to unsupported method in mocking class");
    }

    @Override
    public ApiMsg getBlockByHash(Hash256 hash256) {
        throw new UnsupportedOperationException("Call to unsupported method in mocking class");
    }

    @Override
    public ApiMsg getBlockTransactionCountByHash(Hash256 hash256) {
        throw new UnsupportedOperationException("Call to unsupported method in mocking class");
    }

    @Override
    public ApiMsg getBlockTransactionCountByNumber(long l) {
        throw new UnsupportedOperationException("Call to unsupported method in mocking class");
    }

    @Override
    public ApiMsg getTransactionCount(Address address, long l) {
        throw new UnsupportedOperationException("Call to unsupported method in mocking class");
    }

    @Override
    public ApiMsg getTransactionByHash(Hash256 hash256) {
        throw new UnsupportedOperationException("Call to unsupported method in mocking class");
    }

    @Override
    public ApiMsg getStorageAt(Address address, int i) {
        return null;
    }

    @Override
    public ApiMsg getStorageAt(Address address, int i, long l) {
        return null;
    }
}
