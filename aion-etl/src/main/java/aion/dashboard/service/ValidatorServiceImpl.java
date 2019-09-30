package aion.dashboard.service;

import aion.dashboard.db.DbQuery;
import aion.dashboard.domainobject.ValidatorStats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class ValidatorServiceImpl implements ValidatorService {
    private static final ValidatorService instance = new ValidatorServiceImpl();

    public static ValidatorService getInstance() {
        return instance;
    }
    public PreparedStatement prepare(Connection con, List<ValidatorStats> stats) throws SQLException {
        PreparedStatement ps = con.prepareStatement(DbQuery.INSERT_MINER_STATS);
        for (var stat: stats){
            ps.setLong(1, stat.getBlockNumber());
            ps.setString(2, stat.getMinerAddress());
            ps.setString(3, stat.getSealType());
            ps.setInt(4, stat.getBlockCount());
            ps.setLong(5, stat.getBlockTimestamp());
            ps.setBigDecimal(6, stat.getPercentageOfBlocksValidated());
            ps.addBatch();
        }
        return ps;
    }

    public PreparedStatement deleteValidatorStats(Connection con, long blockNumber) throws SQLException {
        PreparedStatement ps = con.prepareStatement(DbQuery.DELETE_VALIDATORS);
        ps.setLong(1,blockNumber);
        ps.addBatch();
        return ps;
    }
}
