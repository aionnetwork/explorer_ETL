package aion.dashboard.service;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.domainobject.TransactionStats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TransactionStatsServiceImpl implements TransactionStatsService {

    @Override
    public boolean save(TransactionStats transactionStats) {
        try(Connection con = DbConnectionPool.getConnection();
            PreparedStatement ps = prepare(con, transactionStats)){
            ps.executeBatch();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public PreparedStatement prepare(Connection con, TransactionStats transactionStats)
        throws SQLException {
        PreparedStatement ps = con.prepareStatement(
            "insert into transaction_stats (number_of_transaction, block_number, block_timestamp, number_of_active_addresses, total_transferred) VALUES (?,?,?,?,?)");

        ps.setLong(1, transactionStats.getNumberOfTransactions());
        ps.setLong(2, transactionStats.getBlockNumber());
        ps.setLong(3, transactionStats.getTimestamp());
        ps.setLong(4, transactionStats.getNumberOfActiveAddresses());
        ps.setBigDecimal(5, transactionStats.getTotalSpent());
        ps.addBatch();
        return ps;
    }

    @Override
    public PreparedStatement prepareDelete(Connection con, long blockNumber) throws SQLException {
        PreparedStatement ps = con
            .prepareStatement("delete from transaction_stats where block_number=?");
        ps.setLong(1, blockNumber);
        return ps;
    }
}
