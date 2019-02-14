package aion.dashboard.service;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.db.DbQuery;
import aion.dashboard.domainobject.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class TokenServiceImpl implements TokenService {

    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");
    private static final TokenServiceImpl INSTANCE = new TokenServiceImpl();

    public static TokenServiceImpl getInstance() {
        return INSTANCE;
    }

    private TokenServiceImpl(){

    }

    @Override
    public boolean save(Token token) {
        return save(Collections.singletonList(token));


    }

    @Override
    public boolean save(List<Token> tokens)   {


        try(Connection con = DbConnectionPool.getConnection()) {

            try (PreparedStatement ps = prepare(con, tokens)) {

                ps.executeBatch();
                con.commit();
            }
            catch (SQLException e){
                con.rollback();
                throw e;
            }
            return true;
        } catch (SQLException e) {
            GENERAL.debug("Threw exception in token save", e);
            return false;

        }


    }

    @Override
    public Token getByContractAddr(String contractAddr) throws SQLException {
        Connection con=DbConnectionPool.getConnection ();
        Token token =null;
        try (PreparedStatement ps = con.prepareStatement(DbQuery.TOKEN_SELECT)) {
            ps.setString(1, contractAddr);
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    token = new Token.TokenBuilder().contractAddress(resultSet.getString("contract_addr")).
                            transactionHash(resultSet.getString("transaction_hash")).
                            name(resultSet.getString("name")).
                            symbol(resultSet.getString("symbol")).
                            timestamp(resultSet.getLong("creation_timestamp")).
                            creatorAddress(resultSet.getString("creator_address")).
                            totalSupply(resultSet.getBigDecimal("total_supply").toBigInteger()).
                            granularity(resultSet.getBigDecimal("granularity")).
                            totalLiquidSupply(resultSet.getBigDecimal("liquid_supply").toBigInteger()).build();


                }
            }
        } catch (SQLException e) {
            GENERAL.debug("Exception in SQL Query", e);

            return null;
        } finally {
            con.close();
        }
        return token;
    }


    @Override
    public PreparedStatement prepare(Connection con, List<Token> tokens) throws SQLException {
        PreparedStatement ps = con.prepareStatement(DbQuery.TOKEN_INSERT);
        for (Token token : tokens) {
            ps.setString(1, token.getContractAddress());
            ps.setString(2, token.getTransactionHash());
            ps.setString(3, token.getTokenName());
            ps.setString(4, token.getSymbol());
            ps.setString(5, token.getCreatorAddress());
            ps.setLong(6, token.getTotalSupply().longValue());
            ps.setLong(7, token.getTimestamp());
            ps.setFloat(8, token.getGranularity().floatValue());
            ps.setLong(9, token.getTotalLiquidSupply().longValue());
            ps.addBatch();
        }
        return ps;
    }

}
