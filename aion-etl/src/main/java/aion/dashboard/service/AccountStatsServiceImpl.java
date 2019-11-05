package aion.dashboard.service;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.domainobject.AccountStats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class AccountStatsServiceImpl implements AccountStatsService {

    @Override
    public boolean saveAll(List<AccountStats> stats) {
        try(Connection connection = DbConnectionPool.getConnection();
            PreparedStatement ps = prepare(connection, stats)){
            ps.executeBatch();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public PreparedStatement prepare(Connection connection, List<AccountStats> transactionStats)
        throws SQLException {
        PreparedStatement ps = connection.prepareStatement("insert into account_stats (block_number, address, aion_in, aion_out, block_timestamp) VALUES (?,?,?,?,?)");
        for (AccountStats stat : transactionStats) {
            ps.setLong(1, stat.getBlockNumber());
            ps.setString(2, stat.getAddress());
            ps.setBigDecimal(3, stat.getAionIn());
            ps.setBigDecimal(4, stat.getAionOut());
            ps.setLong(5, stat.getTimestamp());
            ps.addBatch();
        }
        return ps;
    }

    @Override
    public PreparedStatement prepareDelete(Connection connection, long blockNumber)
        throws SQLException {
        PreparedStatement ps = connection.prepareStatement("delete from account_stats where block_number=?");
        ps.setLong(1, blockNumber);
        return ps;
    }
}
