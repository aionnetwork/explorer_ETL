package aion.dashboard.service;

import aion.dashboard.domainobject.Event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface EventService {

    boolean save(Event event);

    boolean save(List<Event> event);


    PreparedStatement prepare(Connection con, List<Event> events) throws SQLException;
}
