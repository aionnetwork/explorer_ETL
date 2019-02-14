package aion.dashboard.service;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.db.DbQuery;
import aion.dashboard.domainobject.TokenTransfers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class TokenTransfersServiceImpl implements TokenTransfersService {


    private static final TokenTransfersServiceImpl Instance = new TokenTransfersServiceImpl();


    private TokenTransfersServiceImpl() {
        if (Instance != null) {
            throw new IllegalStateException();
        }
    }

    public static TokenTransfersServiceImpl getInstance() {
        return Instance;
    }

    @Override
    public boolean save(TokenTransfers tokenTransfers) {

        try (Connection con = DbConnectionPool.getConnection()) {
            try (PreparedStatement ps = prepare(con, Collections.singletonList(tokenTransfers))) {
                ps.executeBatch();
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;

            }


        } catch (SQLException e) {

            e.printStackTrace();
            return false;
        }


        return true;
    }

    @Override
    public boolean save(List<TokenTransfers> tokenTransfers) {

        try (Connection con = DbConnectionPool.getConnection()) {
            try (PreparedStatement ps = prepare(con, tokenTransfers)) {
                ps.executeBatch();
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;

            }


        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }


        return true;
    }

    @Override
    public PreparedStatement prepare(Connection con, List<TokenTransfers> tokenTransfers) throws SQLException {
        PreparedStatement ps = con.prepareStatement(DbQuery.TokenTransfersInsert);

        for (var tokenTransfer : tokenTransfers) {
            ps.setString(1, tokenTransfer.getToAddress());
            ps.setString(2, tokenTransfer.getFromAddress());
            ps.setString(3, tokenTransfer.getOperator());
            ps.setBigDecimal(4, tokenTransfer.getScaledValue());
            ps.setString(5, tokenTransfer.getRawValue());
            ps.setBigDecimal(6, tokenTransfer.getGranularity());
            ps.setInt(7, tokenTransfer.getTokendecimal());
            ps.setString(8, tokenTransfer.getContractAddress());
            ps.setString(9, tokenTransfer.getTransactionHash());
            ps.setLong(10, tokenTransfer.getBlockNumber());
            ps.setLong(11, tokenTransfer.getTransferTimestamp());
            ps.setInt(12, tokenTransfer.getBlockYear());
            ps.setInt(13, tokenTransfer.getBlockMonth());
            ps.setInt(14, tokenTransfer.getBlockDay());
            ps.setDouble(15, tokenTransfer.getApproxValue());


            ps.addBatch();
        }

        return ps;
    }


}
