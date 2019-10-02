package aion.dashboard.service;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.db.DbQuery;
import aion.dashboard.domainobject.Account;
import aion.dashboard.util.TimeLogger;
import aion.dashboard.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class AccountServiceImpl implements AccountService {
    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");
    private static final AccountServiceImpl INSTANCE = new AccountServiceImpl();
    //TODO change this to 12hrs since this significantly impacts the performance of the ETL
    private final AtomicReference<BigDecimal> currCirculatingSupply = new AtomicReference<>(BigDecimal.ZERO);
    private final AtomicLong lastUpdateInstant = new AtomicLong(0L);
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
            try (PreparedStatement ps = con.prepareStatement(DbQuery.AccountInsert)) {
                ps.setString(1, account.getAddress());
                ps.setBigDecimal(2, account.getBalance());
                ps.setLong(3, account.getLastBlockNumber());
                ps.setInt(4, Objects.requireNonNullElse(comp, account).getContract());
                ps.setString(5, account.getNonce());
                ps.setString(6, Objects.requireNonNullElse(comp, account).getTransactionHash());
                ps.setDouble(7, account.getApproxBalance());
                ps.setLong(8, Objects.requireNonNullElse(comp, account).getFirstBlockNumber());
                ps.execute();
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
    public boolean save(List<Account> accounts) {

        try (var con = DbConnectionPool.getConnection()) {
            try (PreparedStatement ps = prepare(con, accounts)) {
                ps.executeBatch();
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
                if (resultSet.next()) {
                    return deserializeAccount(resultSet);
                }
                else return null;
            }
        }
    }

    private Account deserializeAccount(ResultSet resultSet) throws SQLException {
        Account acc = Account.builder()
                .balance(resultSet.getBigDecimal("balance"))
                .address(resultSet.getString("address"))
                .contract(resultSet.getInt("contract"))
                .nonce(resultSet.getString("nonce"))
                .lastBlockNumber(resultSet.getLong("last_block_number"))
                .transactionHash(resultSet.getString("transaction_hash"))
                .build();
        acc.setFirstBlockNumber(resultSet.getLong("first_block_number"));
        return acc;
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
                list.add(deserializeAccount(resultSet));
            }
        }
        return list;
    }

    @Override
    public PreparedStatement prepare(Connection con, List<Account> accounts) throws SQLException {
        PreparedStatement ps = con.prepareStatement(DbQuery.AccountInsert);

        for(Account account : accounts) {
            Account comp = getByAddress(account.getAddress());
            ps.setString(1, account.getAddress());
            ps.setBigDecimal(2, (account.getBalance()));
            ps.setLong(3, account.getLastBlockNumber());
            ps.setInt(4, Objects.requireNonNullElse(comp, account).getContract());
            ps.setString(5, account.getNonce());
            ps.setString(6, Objects.requireNonNullElse(comp, account).getTransactionHash());
            ps.setDouble(7, account.getApproxBalance());
            ps.setLong(8, Objects.requireNonNullElse(comp, account).getFirstBlockNumber());
            ps.addBatch();
        }

        return ps;
    }

    @Override
    public synchronized BigDecimal sumBalance(Instant instant) throws SQLException {
        if (instant.getEpochSecond() - this.lastUpdateInstant.get() <= (12*60*60)){
            // all data is valid for 12hrs
            return currCirculatingSupply.get();
        }
        else{
            try(Connection con = DbConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT SUM(balance) as circulating_supply from account");
                ResultSet rs = ps.executeQuery()){
                if (rs.next()) {
                    BigDecimal circulatingSupply = Utils.toAion(rs.getBigDecimal(1));
                    this.currCirculatingSupply.set(circulatingSupply);
                    this.lastUpdateInstant.set(instant.getEpochSecond());
                    return circulatingSupply;
                }
                else {
                    return BigDecimal.ONE.negate();
                }
            }
        }
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