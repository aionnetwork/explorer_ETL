package aion.dashboard.service;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.db.DbQuery;
import aion.dashboard.domainobject.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class EventServiceImpl implements EventService {
    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");
    private static final EventServiceImpl Instance = new EventServiceImpl();


    private EventServiceImpl() {
        if (Instance != null) {// IGNORE linting here because this check is used to enforce the singleton
            throw new IllegalStateException();
        }
    }

    public static EventServiceImpl getInstance() {
        return Instance;
    }

    @Override
    public boolean save(Event event) {

        return save(Collections.singletonList(event));
    }

    @Override
    public boolean save(List<Event> events) {
        try (Connection con = DbConnectionPool.getConnection();
             PreparedStatement ps = prepare(con, events)) {

            try  {
                ps.executeBatch();
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

        PreparedStatement ps = con.prepareStatement(DbQuery.EVENT_INSERT);
        for (var event : events) {
            ps.setString(1, event.getName());
            ps.setString(2, event.getParameterList());
            ps.setString(3, event.getInputList());
            ps.setLong(4, event.getTransactionId());
            ps.setLong(5, event.getBlockNumber());
            ps.setString(6, event.getContractAddr());
            ps.setLong(7, event.getTimestamp());

            ps.addBatch();

        }

        return ps;
    }


}
