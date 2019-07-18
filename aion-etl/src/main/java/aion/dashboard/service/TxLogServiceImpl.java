package aion.dashboard.service;

import aion.dashboard.db.DbQuery;
import aion.dashboard.domainobject.TxLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class TxLogServiceImpl implements TxLogService {
    private static TxLogServiceImpl ourInstance = new TxLogServiceImpl();

    public static TxLogServiceImpl getInstance() {
        return ourInstance;
    }

    private TxLogServiceImpl() {
    }

    @Override
    public PreparedStatement prepare(Connection con, List<TxLog> logs) throws SQLException {
        var stmt = con.prepareStatement(DbQuery.InsertTxLog);

        for (var log : logs){
            stmt.setString(1, log.getTransactionHash());
            stmt.setInt(2, log.getIndex());
            stmt.setLong(3, log.getBlockNumber());
            stmt.setLong(4, log.getBlockTimestamp());
            stmt.setString(5, log.getTopics());
            stmt.setString(6, log.getData());
            stmt.setString(7, log.getAddress());
            stmt.setString(8, log.getFrom());
            stmt.setString(9, log.getTo());
            stmt.setString(10, log.getContractType());
            stmt.addBatch();
        }
        return stmt;
    }

    @Override
    public PreparedStatement prepareDelete(Connection con, long blockNumber) throws SQLException {
        var stmt = con.prepareStatement(DbQuery.DeleteTxLogByBlockNumber);

        stmt.setLong(1,blockNumber);
        return stmt;
    }
}
