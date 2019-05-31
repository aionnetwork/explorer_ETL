package aion.dashboard.blockchain.interfaces;

import aion.dashboard.blockchain.type.APIBlock;
import aion.dashboard.blockchain.type.APITransaction;
import aion.dashboard.blockchain.type.CallObject;
import aion.dashboard.exception.Web3ApiException;

import java.math.BigInteger;
import java.util.List;

public interface APIService extends AutoCloseable {


    BigInteger getBalance(String address) throws Exception;
    BigInteger getNonce(String address) throws Exception;
    long getBlockNumber() throws Exception;
    byte[] call(CallObject object) throws Exception;
    void close();

    APIBlock getBlock(long blockNumber) throws Exception;
    APITransaction getTransaction(String txHash) throws Exception;
}
