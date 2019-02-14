package aion.dashboard.service;

import aion.dashboard.domainobject.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface TransactionService {

    boolean save(Transaction transaction);

    boolean save(List<Transaction> transactions);

    List<Long> integrityCheck(long startNum) throws SQLException;

    List<String> getTransactionHashByBlockNum(long startNum) throws SQLException;

    List<Transaction> getTransactionByBlockNumber(long blockNumber) throws SQLException;

    Transaction getTransactionByContractAddress(String contractAddress) throws SQLException;

    PreparedStatement prepare(Connection con, List<Transaction> transactions) throws SQLException;
}
