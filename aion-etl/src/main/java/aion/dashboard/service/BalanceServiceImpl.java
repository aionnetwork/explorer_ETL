package aion.dashboard.service;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.db.DbQuery;
import aion.dashboard.domainobject.Balance;
import aion.dashboard.util.TimeLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BalanceServiceImpl implements BalanceService {
    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");
    private static final BalanceServiceImpl INSTANCE = new BalanceServiceImpl();
    TimeLogger timeLogger;

    public static BalanceServiceImpl getInstance() {
        return INSTANCE;
    }

    private BalanceServiceImpl(){
        timeLogger = TimeLogger.getTimeLogger();

    }


    @Override
    public boolean save(Balance balance)  {
        boolean out = true;
        Connection con= null;

        try {
            Balance comp=getByAddress(balance.getAddress());
            con = DbConnectionPool.getConnection ();
            if(comp==null ||!comp.equals(balance)) {

                PreparedStatement ps = con.prepareStatement(DbQuery.BALANCE_INSERT);
                ps.setString(1, balance.getAddress());
                ps.setLong(2, balance.getBalance().longValue());
                ps.setLong(3, balance.getLastBlockNumber());
                ps.setInt(4, comp == null ? balance.getContract() : comp.getContract());
                ps.setLong(5,balance.getNonce().longValue());
                ps.setLong(6, comp == null ? balance.getTransactionId() : comp.getTransactionId());


                ps.execute();

                con.commit();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                con.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();

            }
            out=false;

        }
        finally {
            try {
                Objects.requireNonNull(con).close();
            } catch (SQLException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        return out;

    }

    @Override
    public boolean save(List<Balance> balances) {
        Connection con = null;
        boolean out = true;
        try{
            con=DbConnectionPool.getConnection ();
            PreparedStatement ps = con.prepareStatement(DbQuery.BALANCE_INSERT);

            for(Balance balance:balances) {
                Balance comp = getByAddress(balance.getAddress());
                if(comp == null || !comp.equals(balance)) {
                    ps.setString(1, balance.getAddress());
                    ps.setBigDecimal(2, balance.getBalance());
                    ps.setLong(3, balance.getLastBlockNumber());
                    ps.setInt(4, comp == null ? balance.getContract() : comp.getContract());
                    ps.setLong(5, balance.getNonce().longValue());
                    ps.setLong(6, comp == null || comp.getTransactionId() == null ? balance.getTransactionId() : comp.getTransactionId());
                    ps.execute();
                }
            }

            con.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            try { con.rollback(); }
            catch (SQLException e1) { e1.printStackTrace(); }
            out = false;
        } finally {
            try { Objects.requireNonNull(con).close(); }
            catch (SQLException | NullPointerException e) { e.printStackTrace(); }
        }
        return out;
    }

    @Override
    public Balance getByAddress(String address) throws SQLException {

        try(Connection con = DbConnectionPool.getConnection ();
            PreparedStatement ps = con.prepareStatement(DbQuery.BALANCE_SELECT_BY_ADDRESS)) {
            ps.setString(1,address);
            try (ResultSet resultSet = ps.executeQuery()) {

                if (resultSet.next()) {
                    return new Balance.BalanceBuilder().balance(resultSet.getBigDecimal("balance")).
                            address(resultSet.getString("address")).
                            contract(resultSet.getInt("contract")).nonce(BigInteger.valueOf(resultSet.getLong("nonce"))).
                            lastBlockNumber(resultSet.getLong("last_block_number")).
                            transactionId(resultSet.getLong("transaction_id")).build();
                }
                else return null;
            }

        }
    }


    public List<Balance> getByBlockNumber(Long blockNumber) throws SQLException {
        Connection con=DbConnectionPool.getConnection ();
        try(PreparedStatement ps = con.prepareStatement(DbQuery.BALANCE_SELECT_GREATER_THAN_BLOCK_NUMBER)) {

            ps.setLong(1, blockNumber);
            List<Balance> list = new ArrayList<>();
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    list.add(new Balance.BalanceBuilder().balance((resultSet.getBigDecimal("balance"))).
                            address(resultSet.getString("address")).
                            contract(resultSet.getInt("contract")).
                            lastBlockNumber(resultSet.getLong("last_block_number"))
                            .transactionId(resultSet.getLong("transaction_id")).nonce(BigInteger.valueOf(resultSet.getLong("nonce"))).build());


                }
            }

            return list;
        } finally {
            try { Objects.requireNonNull(con).close(); }
            catch (SQLException | NullPointerException e) { e.printStackTrace(); }
        }
    }

    @Override
    public PreparedStatement prepare(Connection con, List<Balance> balances) throws SQLException {
        PreparedStatement ps = con.prepareStatement(DbQuery.BALANCE_INSERT);

        for(Balance balance:balances) {
            Balance comp = getByAddress(balance.getAddress());
            if (comp == null || !comp.equals(balance)) {
                ps.setString(1, balance.getAddress());
                ps.setBigDecimal(2, balance.getBalance());
                ps.setLong(3, balance.getLastBlockNumber());
                ps.setInt(4, comp == null ? balance.getContract() : comp.getContract());
                ps.setLong(5, balance.getNonce().longValue());
                ps.setLong(6, comp == null ? balance.getTransactionId() : comp.getTransactionId());
                ps.addBatch();
            }
        }

        return ps;
    }

    @Override
    public long getMaxBlock() throws SQLException {
        Connection con=DbConnectionPool.getConnection ();

        long out = 0;
        try(PreparedStatement ps = con.prepareStatement(DbQuery.BALANCE_COUNT)) {

            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) out= resultSet.getLong(1);
            con.close();

        } catch (SQLException e) { e.printStackTrace(); }
        finally { con.close(); }

        return out;
    }

//    private Long getLastBlockNumber() throws SQLException {
//        Connection con=DbConnectionPool.getConnection ();
//        PreparedStatement ps = con.prepareStatement("select max(last_block_number) from balance ");
//        Long out=0l;
//        List<Balance> b=new ArrayList<Balance>();
//        ResultSet resultSet = ps.executeQuery();
//        while (resultSet.next())
//            out=resultSet.getLong(1);
//
//        resultSet.close();
//        return out;
//    }



}
