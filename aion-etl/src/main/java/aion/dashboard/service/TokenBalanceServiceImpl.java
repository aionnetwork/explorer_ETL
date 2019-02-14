package aion.dashboard.service;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.db.DbQuery;
import aion.dashboard.domainobject.TokenBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TokenBalanceServiceImpl implements TokenBalanceService {
    private static final TokenBalanceService INSTANCE = new TokenBalanceServiceImpl();
    private static Logger GENERAL = LoggerFactory.getLogger("logger_general");

    private TokenBalanceServiceImpl() {
        if (INSTANCE != null) {
            throw new IllegalStateException();
        }
    }

    public static TokenBalanceService getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean save(TokenBalance tokenBalance) {
        try (Connection con = DbConnectionPool.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(DbQuery.TOKEN_BALANCE_INSERT)) {
                ps.setBigDecimal(1, tokenBalance.getBalance());
                ps.setString(2, tokenBalance.getHolderAddress());
                ps.setString(3, tokenBalance.getContractAddress());
                ps.setLong(4, tokenBalance.getBlockNumber());
                ps.execute();

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
    public boolean save(List<TokenBalance> tokenBalanceList) {
        try (Connection con = DbConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(DbQuery.TOKEN_BALANCE_INSERT)) {
            try {
                for (var tokenBalance : tokenBalanceList) {
                    ps.setBigDecimal(1, tokenBalance.getBalance());
                    ps.setString(2, tokenBalance.getHolderAddress());
                    ps.setString(3, tokenBalance.getContractAddress());
                    ps.setLong(4, tokenBalance.getBlockNumber());
                    ps.execute();
                }

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
    public List<TokenBalance> getTokensByBlockNumber(long blockNumber) throws SQLException {
        List<TokenBalance> results = new ArrayList<>();


        try(Connection con = DbConnectionPool.getConnection();
            PreparedStatement ps = con.prepareStatement(DbQuery.TOKEN_BALANCE_SELECT)) {
            ps.setLong(1, blockNumber);

            try (ResultSet rs = ps.executeQuery()) {
                TokenBalance.TokenBalanceBuilder builder = new TokenBalance.TokenBalanceBuilder();
                while (rs.next()) {
                    builder.setBalance(rs.getBigDecimal("tkn_balance"));
                    builder.setBlockNumber(rs.getLong("block_number"));
                    builder.setContractAddress(rs.getString("contract_addr"));
                    builder.setHolderAddress(rs.getString("holder_addr"));

                    results.add(builder.build());
                }
            }


        }


        return results;
    }

    @Override
    public PreparedStatement prepare(Connection con, List<TokenBalance> tokenBalances) throws SQLException {
        PreparedStatement ps = con.prepareStatement(DbQuery.TOKEN_BALANCE_INSERT);

        for (var tokenBalance : tokenBalances) {
            ps.setBigDecimal(1, tokenBalance.getBalance());
            ps.setString(2, tokenBalance.getHolderAddress());
            ps.setString(3, tokenBalance.getContractAddress());
            ps.setLong(4, tokenBalance.getBlockNumber());
            ps.addBatch();
        }

        return ps;
    }


}
