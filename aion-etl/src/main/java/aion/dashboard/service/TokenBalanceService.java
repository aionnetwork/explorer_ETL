package aion.dashboard.service;

import aion.dashboard.domainobject.TokenBalance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface TokenBalanceService {

    boolean save(TokenBalance tokenBalance);

    boolean save(List<TokenBalance> tokenBalanceList);

    /**
     * Returns all the tokens that have a blocknumber>=b
     *
     * @param b
     * @return
     */
    List<TokenBalance> getTokensByBlockNumber(long b) throws SQLException;

    PreparedStatement prepare(Connection con, List<TokenBalance> tokenBalances) throws SQLException;

}
