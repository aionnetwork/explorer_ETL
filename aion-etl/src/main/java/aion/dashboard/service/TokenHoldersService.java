package aion.dashboard.service;

import aion.dashboard.domainobject.TokenHolders;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface TokenHoldersService {

    boolean save(TokenHolders tokenHolders);

    boolean save(List<TokenHolders> tokenHoldersList);

    /**
     * Returns all the tokens that have a blocknumber>=b
     *
     * @param b
     * @return
     */
    List<TokenHolders> getTokensByBlockNumber(long b) throws SQLException;

    PreparedStatement prepare(Connection con, List<TokenHolders> tokenHolders) throws SQLException;

}
