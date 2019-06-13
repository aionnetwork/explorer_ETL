package aion.dashboard.service;

import aion.dashboard.db.DbQuery;
import aion.dashboard.domainobject.Metrics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

public class MetricsServiceImpl implements MetricsService {
    private static MetricsServiceImpl Instances = new MetricsServiceImpl();

    public static MetricsServiceImpl getInstance() {
        return Instances;
    }

    private MetricsServiceImpl(){}


    @Override
    public PreparedStatement prepare(Connection con, List<Metrics> metrics0) throws SQLException {
        PreparedStatement ps = con.prepareStatement(DbQuery.MetricsInsert);
        for (var metrics: metrics0) {
            ps.setObject(1, metrics.getTotalTransactions());
            ps.setBigDecimal(2, metrics.getTransactionsPerSecond());
            ps.setLong(3, metrics.getPeakTransactionsPerBlock());
            ps.setLong(4, metrics.getStartBlock());
            ps.setLong(5, metrics.getEndBlock());
            ps.setBigDecimal(6, metrics.getAverageNrgConsumed());
            ps.setBigDecimal(7, metrics.getAverageNrgLimit());
            ps.setBigDecimal(8, metrics.getAverageBlockTime());
            ps.setBigDecimal(9, metrics.getAverageDifficulty());
            ps.setLong(10, metrics.getEndTimeStamp());
            ps.setLong(11, metrics.getStartTimeStamp());
            ps.setBigDecimal(12, metrics.getAverageHashPower());
            ps.setBigDecimal(13, metrics.getLastBlockReward());
            ps.setInt(14, metrics.getId());
            ps.addBatch();
        }
        return ps;
    }

    @Override
    public PreparedStatement prepareDelete(Connection con) throws SQLException {
        PreparedStatement ps = con.prepareStatement(DbQuery.MetricsDelete);

        return ps;
    }
}
