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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class GraphingServiceImpl implements GraphingService {

    private static final GraphingServiceImpl Instance = new GraphingServiceImpl();
    private static final Logger GENERAl = LoggerFactory.getLogger("logger_general");


    private GraphingServiceImpl() {
    }

    public static GraphingServiceImpl getInstance() {
        return Instance;
    }


    @Override
    public boolean save(Graphing graphing) {
        try (Connection con = DbConnectionPool.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(DbQuery.GraphingInsert)) {


                LocalDate date = Instant.ofEpochSecond(graphing.getTimestamp())
                        .atZone(ZoneId.of("UTC"))
                        .toLocalDate();

                ps.setBigDecimal(1, graphing.getValue());
                ps.setString(2, graphing.getGraphType());
                ps.setLong(3, graphing.getTimestamp());
                ps.setLong(4, graphing.getBlockNumber());
                ps.setString(5, graphing.getDetail());
                ps.setInt(6, date.getYear());
                ps.setInt(7, date.getMonthValue());
                ps.setInt(8, date.getDayOfMonth());

                ps.execute();

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }


        } catch (SQLException e) {
            GENERAl.debug("Threw an exception in graphing save: ", e);
            return false;
        }


        return true;
    }

    @Override
    public boolean save(List<Graphing> graphings) {

        try (Connection con = DbConnectionPool.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(DbQuery.GraphingInsert);
                 PreparedStatement parserStatement = ParserStateServiceImpl.getInstance().prepare( con,
                    List.of(new ParserState.ParserStateBuilder()
                            .blockNumber(BigInteger.valueOf(graphings.get(graphings.size() -1).getBlockNumber()))
                            .id(ParserStateServiceImpl.GRAPHING_ID)
                            .build()))
            ) {

                for (var graphing : graphings) {

                    LocalDate date = Instant.ofEpochSecond(graphing.getTimestamp())
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate();

                    ps.setBigDecimal(1, graphing.getValue());
                    ps.setString(2, graphing.getGraphType());
                    ps.setLong(3, graphing.getTimestamp());
                    ps.setLong(4, graphing.getBlockNumber());
                    ps.setString(5, graphing.getDetail());
                    ps.setInt(6, date.getYear());
                    ps.setInt(7, date.getMonthValue());
                    ps.setInt(8, date.getDayOfMonth());

                    ps.execute();
                }


                parserStatement.executeBatch();
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }


        } catch (SQLException e) {
            GENERAl.debug("Threw an exception in graphing save: ", e);
            return false;
        }

        return true;
    }

    @Override
    public Graphing getLastRecord(long blockNumber) throws SQLException {
        Graphing graphingObject = null;
        try (Connection connection = DbConnectionPool.getConnection()) {

            try (PreparedStatement ps = connection.prepareStatement(DbQuery.GraphingSelectLastRecord)) {
                ps.setLong(1, blockNumber);
                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        graphingObject = new Graphing.GraphingBuilder().setBlockNumber(rs.getLong("block_number"))
                                .setDetail(rs.getString("detail"))
                                .setTimestamp(rs.getLong("timestamp"))
                                .setValue(rs.getBigDecimal("value"))
                                .setGraphType(Graphing.GraphType.getByType(rs.getString("graph_type")))
                                .build();
                    }
                }

            }

        } catch (SQLException e) {
            GENERAl.debug("Threw an exception in getLastRecord");
            throw e;
        }
        return graphingObject;
    }


    public long countActiveAddresses(long blockNumber) throws SQLException{
        long out = -1;

        try(Connection con = DbConnectionPool.getConnection()){
            try(PreparedStatement ps = con.prepareStatement(DbQuery.CountActiveAddresses)){
                ps.setLong(1, blockNumber);
                ps.setLong(2, blockNumber);
                ps.setLong(3, blockNumber);
                try(ResultSet rs = ps.executeQuery()) {
                    while (rs.next()){
                        out = rs.getLong("total");
                    }
                }

            }
        }
        catch (SQLException e){

            throw e;
        }

        return out;
    }

    @Override
    public long checkIntegrity(long blockNumber) throws SQLException {
        long inconsistentRecord = -1;

        try(Connection con = DbConnectionPool.getConnection()){
            try(PreparedStatement ps = con.prepareStatement(DbQuery.FindMinInconsistentRecord)){

                ps.setLong(1,blockNumber);
                try(ResultSet rs = ps.executeQuery()){

                    while (rs.next()){
                        inconsistentRecord = rs.getLong(1);

                    }



                }

            }

        }



        return inconsistentRecord;
    }


}