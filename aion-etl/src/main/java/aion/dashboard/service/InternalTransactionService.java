package aion.dashboard.service;

import aion.dashboard.domainobject.InternalTransaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface InternalTransactionService {
    boolean save(List<InternalTransaction> internalTransaction);
    boolean save(InternalTransaction internalTransaction);
    PreparedStatement prepareDelete(long blockNumber, Connection connection) throws SQLException;
    PreparedStatement deleteExisting(long blockNumber, Connection connection) throws Exception;
    PreparedStatement prepare(Connection con,List<InternalTransaction> internalTransactions) throws Exception;
}
