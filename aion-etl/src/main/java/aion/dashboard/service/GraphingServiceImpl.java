package aion.dashboard.service;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.db.DbQuery;
import aion.dashboard.domainobject.Graphing;
import aion.dashboard.domainobject.ParserState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class GraphingServiceImpl implements GraphingService {

    private static GraphingServiceImpl Instance = new GraphingServiceImpl();
    private static Logger GENERAl = LoggerFactory.getLogger("logger_general");


    private GraphingServiceImpl() {
    }

    public static GraphingServiceImpl getInstance() {
        return Instance;
    }


    @Override
    public boolean save(Graphing graphing) {

        return save(Collections.singletonList(graphing));
    }

    @Override
    public boolean save(List<Graphing> graphingList) {

        try (Connection con = DbConnectionPool.getConnection();
             var parserStatement = ParserStateServiceImpl.getInstance().prepare(
                con,
                List.of(new ParserState.ParserStateBuilder()
                        .blockNumber(BigInteger.valueOf(graphingList.get(graphingList.size() -1).getBlockNumber()))
                        .transactionID(BigInteger.ONE.negate())
                        .id(ParserStateServiceImpl.GRAPHING_ID)
                        .build()));
             PreparedStatement ps = con.prepareStatement(DbQuery.GRAPHING_INSERT)) {
            try {


                for (var graphing : graphingList) {
                    ps.setBigDecimal(1, graphing.getValue());
                    ps.setString(2, graphing.getGraphType());
                    ps.setLong(3, graphing.getTimestamp());
                    ps.setLong(4, graphing.getBlockNumber());
                    ps.setString(5, graphing.getDetail());
                    ps.setInt(6, graphing.getYear());
                    ps.setInt(7, graphing.getMonth());
                    ps.setInt(8, graphing.getDate());

                    ps.execute();
                }


                parserStatement.executeBatch();
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }


            return true;
        } catch (SQLException e) {
            GENERAl.debug("Threw an exception in graphing save: ", e);
            return false;
        }
    }

    @Override
    public Graphing getLastRecord(long blockNumber) throws SQLException {
        Graphing graphingObject = null;
        try (Connection connection = DbConnectionPool.getConnection()) {
            ResultSet rs = null;
            try (PreparedStatement ps = connection.prepareStatement(DbQuery.GRAPHING_SELECT_LAST_RECORD)) {
                ps.setLong(1, blockNumber);
                rs = ps.executeQuery();

                while (rs.next()) {
                    graphingObject = new Graphing.GraphingBuilder().setBlockNumber(rs.getLong("block_number"))
                            .setDate(rs.getInt("date"))
                            .setMonth(rs.getInt("month"))
                            .setYear(rs.getInt("year"))
                            .setDetail(rs.getString("detail"))
                            .setTimestamp(rs.getLong("timestamp"))
                            .setValue(rs.getBigDecimal("value"))
                            .setGraphType(Graphing.GraphType.getByType(rs.getString("graph_type")))
                            .build();
                }

            } finally {
                try {
                    Objects.requireNonNull(rs).close();
                } catch (SQLException| NullPointerException ignored) {

                }
            }
        } catch (SQLException e) {
            GENERAl.debug("Threw an exception in getLastRecord");
            throw e;
        }
        return graphingObject;
    }


    public long countActiveAddresses(long blockNumber) throws SQLException {
        long out = -1;
        long txIndex = BlockServiceImpl.getInstance().getMaxTransactionIdForBlock(blockNumber);

        try (Connection con = DbConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(DbQuery.COUNT_ACTIVE_ADDRESSES)) {

            ps.setLong(1, txIndex);
            ps.setLong(2, txIndex);
            ps.setLong(3, txIndex);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out = rs.getLong("total");
                }
            }

        }


        return out;
    }

    @Override
    public long checkIntegrity(long blockNumber) throws SQLException {
        long inconsistentRecord = -1;

        try(Connection con = DbConnectionPool.getConnection();
            PreparedStatement ps = con.prepareStatement(DbQuery.FIND_MIN_INCONSISTENT_RECORD)) {
            ps.setLong(1, blockNumber);
            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    inconsistentRecord = rs.getLong(1);

                }


            }
        }
        return inconsistentRecord;
    }


}
