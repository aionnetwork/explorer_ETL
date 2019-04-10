package aion.dashboard.service;

import aion.dashboard.domainobject.Metrics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface MetricsService {

    PreparedStatement prepare(Connection con, List<Metrics> metrics) throws SQLException;

    PreparedStatement prepareDelete(Connection con) throws SQLException;
}
