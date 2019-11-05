package aion.dashboard.service;

import aion.dashboard.domainobject.TransactionStats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface TransactionStatsService {

    boolean save(TransactionStats transactionStats);

    PreparedStatement prepare(Connection con, TransactionStats transactionStats)
        throws SQLException;

    PreparedStatement prepareDelete(Connection con, long blockNumber) throws SQLException;
}
