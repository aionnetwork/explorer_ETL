package aion.dashboard.service;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.db.DbQuery;
import aion.dashboard.domainobject.TokenHolders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TokenHoldersServiceImpl implements TokenHoldersService {
    private static final TokenHoldersService Instance = new TokenHoldersServiceImpl();
    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");

    private TokenHoldersServiceImpl() {
        if (Instance != null) {// Enforce singleton instance
            throw new IllegalStateException();
        }
    }

    public static TokenHoldersService getInstance() {
        return Instance;
    }

    @Override
    public boolean save(TokenHolders tokenHolders) {
        try (Connection con = DbConnectionPool.getConnection()) {
            try (PreparedStatement ps = prepare(con, Collections.singletonList(tokenHolders))) {
                ps.executeBatch();
                con.commit();

            } catch (SQLException e) {
                GENERAL.debug("Threw an exception in save", e);

                con.rollback();
                throw e;
            }
        } catch (SQLException e) {

            return false;
        }


        return true;
    }

    @Override
    public boolean save(List<TokenHolders> tokenHoldersList) {
        try (Connection con = DbConnectionPool.getConnection()) {
            try (PreparedStatement ps = prepare(con, tokenHoldersList)) {
                ps.executeBatch();

                con.commit();

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            GENERAL.debug("Threw an exception in save", e);
            return false;
        }


        return true;
    }

    @Override
    public List<TokenHolders> getTokensByBlockNumber(long blockNumber) throws SQLException {
        List<TokenHolders> results = new ArrayList<>();
        Connection con = null;

        try {
            con = DbConnectionPool.getConnection();
            try (PreparedStatement ps = con.prepareStatement(DbQuery.TokenHoldersSelect)) {
                ps.setLong(1, blockNumber);

                try (ResultSet rs = ps.executeQuery()) {
                    TokenHolders.TokenBalanceBuilder builder = new TokenHolders.TokenBalanceBuilder();
                    while (rs.next()) {
                        builder.setScaledBalance(rs.getBigDecimal("scaled_balance"));
                        builder.setBlockNumber(rs.getLong("block_number"));
                        builder.setContractAddress(rs.getString("contract_addr"));
                        builder.setHolderAddress(rs.getString("holder_addr"));
                        builder.setRawBalance(rs.getString("raw_balance"));
                        builder.setTokenGranularity(rs.getBigDecimal("granularity").toBigInteger());
                        builder.setTokenDecimal(rs.getInt("token_decimal"));
                        results.add(builder.build());
                    }
                }


            }


        } catch (SQLException | NullPointerException e) {


            throw e;
        } finally {
            try {
                Objects.requireNonNull(con).close();
            } catch (SQLException | NullPointerException ignored) {

            }
        }


        return results;
    }

    @Override
    public PreparedStatement prepare(Connection con, List<TokenHolders> tokenHolders) throws SQLException {
        PreparedStatement ps = con.prepareStatement(DbQuery.TokenHoldersInsert);

        for (var tokenBalance : tokenHolders) {
            ps.setBigDecimal(1, tokenBalance.getScaledBalance());
            ps.setString(2, tokenBalance.getHolderAddress());
            ps.setString(3, tokenBalance.getContractAddress());
            ps.setLong(4, tokenBalance.getBlockNumber());
            ps.setString(5, tokenBalance.getRawBalance());
            ps.setInt(6, tokenBalance.getTokenDecimal());
            ps.setBigDecimal(7, new BigDecimal(tokenBalance.getGranularity()));
            ps.addBatch();
        }

        return ps;
    }


}
