package aion.dashboard.service;

import aion.dashboard.domainobject.Account;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AccountService {

    boolean save(Account account);

    boolean save(List<Account> accounts);
    Account getByAddress(String address) throws SQLException;

    void deleteFrom(Long lastBlockNumber) throws SQLException;

    long getMaxBlock() throws SQLException;

    List<Account> getByBlockNumber(Long BlockNumber) throws SQLException;

    Optional<List<Account>> getRandomAccounts(long limit);

    PreparedStatement prepare(Connection con, List<Account> account) throws SQLException;

    BigDecimal sumBalance(Instant instant) throws SQLException;
}
