package aion.dashboard.blockchain.interfaces;

import aion.dashboard.blockchain.Web3ServiceImpl;
import aion.dashboard.blockchain.type.APIInternalTransaction;
import aion.dashboard.exception.Web3ApiException;

import java.math.BigInteger;
import java.util.List;

public interface Web3Service extends APIService {


    public static Web3Service getInstance() {
        return Web3ServiceImpl.getInstance();
    }
    BigInteger getBalance(String address) throws Web3ApiException;
    BigInteger getBalanceAt(String address, long blockNumber) throws Web3ApiException;
    BigInteger getNonce(String address) throws Web3ApiException;
    BigInteger getNonceAt(String address, long blockNumber) throws Web3ApiException;
    boolean ping(String ep);
    List<APIInternalTransaction> getInternalTransaction(String transactionHash) throws Web3ApiException;



}
