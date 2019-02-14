package aion.dashboard.service;

import aion.dashboard.domainobject.Balance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface BalanceService {

    boolean save(Balance balance);

    boolean save(List<Balance> balances);
    Balance getByAddress(String address) throws SQLException;

    long getMaxBlock() throws SQLException;

    List<Balance> getByBlockNumber(Long BlockNumber) throws SQLException;

    PreparedStatement prepare(Connection con, List<Balance> balance) throws SQLException;

}
