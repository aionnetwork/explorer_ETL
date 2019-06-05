package aion.dashboard.service;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.db.DbQuery;
import aion.dashboard.domainobject.Account;
import aion.dashboard.util.TimeLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountServiceImpl implements AccountService {
    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");
    private static final AccountServiceImpl INSTANCE = new AccountServiceImpl();
    TimeLogger timeLogger;

    public static AccountServiceImpl getInstance() {
        return INSTANCE;
    }

    private AccountServiceImpl(){
        timeLogger = TimeLogger.getTimeLogger();

    }


    @Override
    public boolean save(Account account)  {


        try (var con = DbConnectionPool.getConnection()) {
            Account comp=getByAddress(account.getAddress());

            if(comp==null ||!comp.equals(account)) {

                try (PreparedStatement ps = con.prepareStatement(DbQuery.AccountInsert)) {
                    ps.setString(1, account.getAddress());
                    ps.setBigDecimal(2, account.getBalance());
                    ps.setLong(3, account.getLastBlockNumber());
                    ps.setInt(4, comp == null ? account.getContract() : comp.getContract());
                    ps.setString(5, account.getNonce());
                    ps.setString(6, comp == null ? account.getTransactionHash() : comp.getTransactionHash());
                    ps.setDouble(7, account.getApproxBalance());
                    ps.execute();
                    con.commit();
                }
                catch (SQLException e){
                    con.rollback();
                    throw e;
                }

            }

        } catch (SQLException e) {
            return false;

        }

        return true;

    }

    @Override
    public boolean save(List<Account> accounts) {

        try (var con = DbConnectionPool.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(DbQuery.AccountInsert)) {

                for (Account account : accounts) {
                    Account comp = getByAddress(account.getAddress());
                    if (comp == null || !comp.equals(account)) {
                        ps.setString(1, account.getAddress());
                        ps.setBigDecimal(2, account.getBalance());
                        ps.setLong(3, account.getLastBlockNumber());
                        ps.setInt(4, comp == null ? account.getContract() : comp.getContract());
                        ps.setString(5, account.getNonce());
                        ps.setString(6, comp == null || comp.getTransactionHash() == null ? account.getTransactionHash() : comp.getTransactionHash());
                        ps.setDouble(7, account.getApproxBalance());

                        ps.execute();
                    }
                }

                con.commit();
            }
            catch (SQLException e){
                con.rollback();
                throw e;
            }


        } catch (SQLException e) {
            return false;
        }
        return true;
    }

    @Override
    public Account getByAddress(String address) throws SQLException {

        try(Connection con = DbConnectionPool.getConnection ();
            PreparedStatement ps = con.prepareStatement(DbQuery.AccountSelectByAddress)) {
            ps.setString(1,address);
            try (ResultSet resultSet = ps.executeQuery()) {

                while (resultSet.next()) {
                    return new Account.AccountBuilder()
                            .balance(resultSet.getBigDecimal("balance"))
                            .address(resultSet.getString("address"))
                            .contract(resultSet.getInt("contract"))
                            .nonce(resultSet.getString("nonce"))
                            .lastBlockNumber(resultSet.getLong("last_block_number"))
                            .transactionHash(resultSet.getString("transaction_hash"))
                            .build();
                }
            }

            return null;
        }
    }


    public List<Account> getByBlockNumber(Long blockNumber) throws SQLException {

        try(Connection con=DbConnectionPool.getConnection ();
            PreparedStatement ps = con.prepareStatement(DbQuery.AccountSelectGreaterThanBlockNumber)) {

            ps.setLong(1, blockNumber);

            return extractResults(ps);
        }
    }

    @Override
    public Optional<List<Account>> getRandomAccounts(long limit) {

        try(Connection con=DbConnectionPool.getConnection ();
            PreparedStatement ps = con.prepareStatement(DbQuery.AccountSelectRandom)) {

            ps.setLong(1, limit);
            List<Account> list = extractResults(ps);

            return Optional.of(list);
        }
        catch (Exception e){
            return Optional.empty();
        }
    }

    private List<Account> extractResults(PreparedStatement ps) throws SQLException {
        List<Account> list = new ArrayList<>();
        try (ResultSet resultSet = ps.executeQuery()) {
            while (resultSet.next()) {
                list.add(new Account.AccountBuilder()
                        .balance((resultSet.getBigDecimal("balance")))
                        .address(resultSet.getString("address"))
                        .contract(resultSet.getInt("contract"))
                        .lastBlockNumber(resultSet.getLong("last_block_number"))
                        .transactionHash(resultSet.getString("transaction_hash"))
                        .nonce(resultSet.getString("nonce"))
                        .build());


            }
        }
        return list;
    }

    @Override
    public PreparedStatement prepare(Connection con, List<Account> accounts) throws SQLException {
        PreparedStatement ps = con.prepareStatement(DbQuery.AccountInsert);

        for(Account account : accounts) {
            Account comp = getByAddress(account.getAddress());
            if (comp == null || !comp.equals(account)) {
                ps.setString(1, account.getAddress());
                ps.setBigDecimal(2, (account.getBalance()));
                ps.setLong(3, account.getLastBlockNumber());
                ps.setInt(4, comp == null ? account.getContract() : comp.getContract());
                ps.setString(5, account.getNonce());
                ps.setString(6, comp == null ? account.getTransactionHash() : comp.getTransactionHash());
                ps.setDouble(7, account.getApproxBalance());
                ps.addBatch();
            }
        }

        return ps;
    }

    @Override
    public void deleteFrom(Long lastBlockNumber) throws SQLException {

        try( Connection con=DbConnectionPool.getConnection ();
             PreparedStatement ps = con.prepareStatement(DbQuery.AccountDeleteFromBlock)) {
            try{
                ps.setLong(1, lastBlockNumber);
                ps.execute();
                con.commit();
            }
            catch (SQLException e){
                con.rollback();
                throw e;
            }
        }
    }

    @Override
    public long getMaxBlock() throws SQLException {

        long out = 0;
        try(Connection con = DbConnectionPool.getConnection();
            PreparedStatement ps = con.prepareStatement(DbQuery.AccountCount);
            ResultSet resultSet = ps.executeQuery()) {

            while (resultSet.next()) out= resultSet.getLong(1);


        } catch (SQLException e) {
            GENERAL.debug("",e );
            throw e;
        }

        return out;
    }

}