package aion.dashboard.service;

import aion.dashboard.domainobject.TxLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface TxLogService {

    PreparedStatement prepare(Connection con, List<TxLog> logs) throws SQLException;

    PreparedStatement prepareDelete(Connection con, long blockNumber) throws SQLException;
}
