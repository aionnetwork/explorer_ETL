package aion.dashboard.service;

import aion.dashboard.domainobject.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface AccountService {

    boolean save(Account account);

    boolean save(List<Account> accounts);
    Account getByAddress(String address) throws SQLException;

    void deleteFrom(Long lastBlockNumber) throws SQLException;

    long getMaxBlock() throws SQLException;

    List<Account> getByBlockNumber(Long BlockNumber) throws SQLException;

    PreparedStatement prepare(Connection con, List<Account> account) throws SQLException;

}
