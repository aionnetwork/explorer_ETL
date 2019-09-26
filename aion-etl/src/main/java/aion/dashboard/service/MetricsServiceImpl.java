package aion.dashboard.service;

import aion.dashboard.db.DbQuery;
import aion.dashboard.domainobject.Metrics;

import java.math.BigDecimal;
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
            ps.setInt(1,metrics.getId());
            ps.setBigDecimal(2, new BigDecimal(metrics.getTotalTransactions()));
            ps.setBigDecimal(3, metrics.getTransactionsPerSecond());
            ps.setLong(4, metrics.getPeakTransactionsPerBlock());
            ps.setLong(5, metrics.getStartBlock());
            ps.setLong(6, metrics.getEndBlock());
            ps.setBigDecimal(7, metrics.getAverageNrgConsumed());
            ps.setBigDecimal(8, metrics.getAverageNrgLimit());
            ps.setBigDecimal(9, metrics.getAverageBlockTime());
            ps.setBigDecimal(10, metrics.getAverageDifficulty());
            ps.setLong(11, metrics.getEndTimeStamp());
            ps.setLong(12, metrics.getStartTimeStamp());
            ps.setBigDecimal(13, metrics.getAverageHashPower());
            ps.setBigDecimal(14, metrics.getLastBlockReward());
            ps.setBigDecimal(15, metrics.getPowBlockDifficulty());
            ps.setBigDecimal(16, metrics.getPosBlockDifficulty());
            ps.setBigDecimal(17, metrics.getPowBlockTime());
            ps.setBigDecimal(18, metrics.getPosBlockTime());
            ps.setBigDecimal(19, metrics.getAveragePOSIssuance());
            ps.setBigDecimal(20, metrics.getPercentageOfNetworkStaking());
            ps.setBigDecimal(21, metrics.getTotalStake());
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
