package aion.dashboard.service;

import aion.dashboard.domainobject.Graphing;

import java.sql.SQLException;
import java.util.List;


public interface GraphingService {
    boolean save(Graphing graphing);

    boolean save(List<Graphing> graphings);

    Graphing getLastRecord(long blockNumber) throws SQLException;

    long countActiveAddresses(long blockNumber) throws SQLException;

    long checkIntegrity(long blockNumber) throws SQLException;
}
