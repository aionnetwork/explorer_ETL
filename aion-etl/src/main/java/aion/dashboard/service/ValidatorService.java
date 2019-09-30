package aion.dashboard.service;

import aion.dashboard.domainobject.ValidatorStats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface ValidatorService {
    PreparedStatement prepare(Connection con, List<ValidatorStats> stats) throws SQLException;
    PreparedStatement deleteValidatorStats(Connection con, long blockNumber) throws SQLException;
}
