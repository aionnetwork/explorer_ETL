package aion.dashboard.blockchain;

import java.io.Closeable;
import java.math.BigInteger;

public interface APIService extends AutoCloseable {


    BigInteger getBalance(String address) throws Exception;
    BigInteger getNonce(String address) throws Exception;
    long getBlockNumber() throws Exception;
    byte[] call(byte[] data, String from, String to) throws Exception;
    void close();
}
