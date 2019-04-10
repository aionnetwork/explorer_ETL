package aion.dashboard.service;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.db.DbQuery;
import aion.dashboard.domainobject.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class EventServiceImpl implements EventService {
    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");
    private static final EventServiceImpl Instance = new EventServiceImpl();


    private EventServiceImpl() {
        if (Instance != null) {
            throw new IllegalStateException();
        }
    }

    public static EventServiceImpl getInstance() {
        return Instance;
    }

    @Override
    public boolean save(Event event) {

        try (Connection con = DbConnectionPool.getConnection()) {

            try (PreparedStatement ps = con.prepareStatement(DbQuery.EventInsert)) {
                ps.setString(1, event.getName());
                ps.setString(2, event.getParameterList());
                ps.setString(3, event.getInputList());
                ps.setString(4, event.getTransactionHash());
                ps.setLong(5, event.getBlockNumber());
                ps.setString(6, event.getContractAddr());
                ps.setLong(7, event.getTimestamp());

                ps.execute();

                con.commit();


            } catch (SQLException e) {
                con.rollback();
                throw e;
            }


        } catch (SQLException e) {

            GENERAL.debug("Threw an exception in event save: ", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean save(List<Event> events) {
        try (Connection con = DbConnectionPool.getConnection()) {

            try (PreparedStatement ps = con.prepareStatement(DbQuery.EventInsert)) {
                for (var event : events) {
                    ps.setString(1, event.getName());
                    ps.setString(2, event.getParameterList());
                    ps.setString(3, event.getInputList());
                    ps.setString(4, event.getTransactionHash());
                    ps.setLong(5, event.getBlockNumber());
                    ps.setString(6, event.getContractAddr());
                    ps.setLong(7, event.getTimestamp());

                    ps.execute();

                }
                con.commit();


            } catch (SQLException e) {
                con.rollback();
                throw e;
            }


        } catch (SQLException e) {

            GENERAL.debug("Threw an exception in event save: ", e);
            return false;
        }
        return true;
    }



    @Override
    public PreparedStatement prepare(Connection con, List<Event> events) throws SQLException {

        PreparedStatement ps = con.prepareStatement(DbQuery.EventInsert);
        for (var event : events) {
            ps.setString(1, event.getName());
            ps.setString(2, event.getParameterList());
            ps.setString(3, event.getInputList());
            ps.setString(4, event.getTransactionHash());
            ps.setLong(5, event.getBlockNumber());
            ps.setString(6, event.getContractAddr());
            ps.setLong(7, event.getTimestamp());

            ps.addBatch();

        }

        return ps;
    }


}
