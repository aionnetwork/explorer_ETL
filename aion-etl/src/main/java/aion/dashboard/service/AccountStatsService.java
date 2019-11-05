package aion.dashboard.service;

import aion.dashboard.domainobject.AccountStats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public interface AccountStatsService {

    default boolean save(AccountStats stats){
        return saveAll(Collections.singletonList(stats));
    }
    boolean saveAll(List<AccountStats> stats);

    PreparedStatement prepare(Connection connection, List<AccountStats> transactionStats)
        throws SQLException;

    PreparedStatement prepareDelete(Connection connection, long blockNumber) throws SQLException;
}
