package aion.dashboard.service;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.db.DbQuery;
import aion.dashboard.domainobject.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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


        try(var con = DbConnectionPool.getConnection()) {
            try (PreparedStatement ps = prepare(con, Collections.singletonList(token))) {
                ps.executeBatch();
                con.commit();
            }
            catch (SQLException e){
                con.rollback();
                return false;
            }
        } catch (SQLException e) {
            return false;

        }
        return true;


    }

    @Override
    public boolean save(List<Token> tokens)   {
        Connection con=null;
        PreparedStatement ps=null;
        try {
            con = DbConnectionPool.getConnection();
            ps = prepare(con, tokens);
            ps.executeBatch();

            con.commit();


        } catch (SQLException e) {
            try {
                Objects.requireNonNull(con).rollback();

            } catch (SQLException e1) {
                GENERAL.debug("Threw exception in token save",e);

            }

            GENERAL.debug("Threw exception in token save", e);
            return false;

        }
        finally {
            try {
                Objects.requireNonNull(con).close();
                Objects.requireNonNull(ps).close();
            } catch (SQLException | NullPointerException e) {
                GENERAL.debug("Threw exception while closing save in TokenService: " ,e);
            }
        }
        return true;


    }

    @Override
    public Token getByContractAddr(String contractAddr) throws SQLException {
        Connection con=DbConnectionPool.getConnection ();
        Token token =null;
        try (PreparedStatement ps = con.prepareStatement(DbQuery.TokenSelect)) {
            ps.setString(1, contractAddr);
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    //result = resultSet.getString(1);
                    //System.out.println("contractAddr:" +resultSet.getString(1));
                    token =new Token.TokenBuilder().contractAddress(resultSet.getString("contract_addr"))
                            .transactionHash(resultSet.getString("transaction_hash"))
                            .name(resultSet.getString("name"))
                            .symbol(resultSet.getString("symbol"))
                            .timestamp(resultSet.getLong("creation_timestamp"))
                            .creatorAddress(resultSet.getString("creator_address"))
                            .totalSupply(resultSet.getBigDecimal("total_supply").toBigInteger())
                            .granularity(resultSet.getBigDecimal("granularity").toBigInteger())
                            .setTokenDecimal(resultSet.getInt("token_decimal"))
                            .totalLiquidSupply(resultSet.getBigDecimal("liquid_supply").toBigInteger())
                            .build();


                }
            }
        } catch (Exception e) {
            GENERAL.debug("Exception in SQL Query", e);
            e.printStackTrace();
            return null;
        }
        finally {
            con.close();
        }
        return token;
    }

    @Override
    public void delete(String contractAddr) throws SQLException{
        try (Connection con = DbConnectionPool.getConnection()) {


            try (PreparedStatement ps = con.prepareStatement(DbQuery.TokenDelete)) {
                ps.setString(1, contractAddr);

                ps.execute();
                con.commit();

            } catch (SQLException e) {
                con.rollback();
                GENERAL.debug("Threw exception in token delete: ", e);
                throw e; // rethrow so it gets bubbled up
            }
        }
    }


    @Override
    public PreparedStatement prepare(Connection con, List<Token> tokens) throws SQLException {
        PreparedStatement ps = con.prepareStatement(DbQuery.TokenInsert);
        for (Token token : tokens) {


            ps.setString(1, token.getContractAddress());
            ps.setString(2, token.getTransactionHash());
            ps.setString(3, token.getTokenName());
            ps.setString(4, token.getSymbol());
            ps.setString(5, token.getCreatorAddress());
            ps.setBigDecimal(6, new BigDecimal(token.getTotalSupply()));
            ps.setLong(7, token.getBlockTimestamp());
            ps.setBigDecimal(8, new BigDecimal(token.getGranularity().longValue()));
            ps.setBigDecimal(9, new BigDecimal(token.getTotalLiquidSupply()));
            ps.setInt(10, token.getTokenDecimal());
            ps.setInt(11, token.getBlockYear());
            ps.setInt(12, token.getBlockMonth());
            ps.setInt(13, token.getBlockDay());
            ps.addBatch();
        }
        return ps;
    }

}