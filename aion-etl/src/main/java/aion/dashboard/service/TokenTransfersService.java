package aion.dashboard.service;

import aion.dashboard.domainobject.TokenTransfers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface TokenTransfersService {

    boolean save(TokenTransfers tokenTransfers);

    boolean save(List<TokenTransfers> tokenTransfers);

    PreparedStatement prepare(Connection con, List<TokenTransfers> tokenTransfers) throws SQLException;

}
