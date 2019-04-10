package aion.dashboard.service;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.db.DbQuery;
import aion.dashboard.domainobject.Block;
import aion.dashboard.domainobject.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TransactionServiceImpl implements TransactionService {
    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");
    private static final TransactionServiceImpl INSTANCE = new TransactionServiceImpl();


    public static TransactionServiceImpl getInstance() {
        return INSTANCE;
    }

    private TransactionServiceImpl(){

    }

    @Override
    public boolean save(Transaction transaction) {


        Connection con = null;
        PreparedStatement ps = null;

        try {

            con = DbConnectionPool.getConnection();
            ps = con.prepareStatement(DbQuery.TransactionInsert);

            LocalDate date = Instant.ofEpochSecond(transaction.getBlockTimestamp())
                    .atZone(ZoneId.of("UTC"))
                    .toLocalDate();

            ps.setString(1, transaction.getTransactionHash());
            ps.setString(2, transaction.getBlockHash());
            ps.setLong(3, transaction.getBlockNumber());
            ps.setLong(4, transaction.getBlockTimestamp());
            ps.setLong(5, transaction.getTransactionIndex());
            ps.setString(6, transaction.getFromAddr());
            ps.setString(7, transaction.getToAddr());
            ps.setLong(8, transaction.getNrgConsumed());
            ps.setLong(9, transaction.getNrgPrice());
            ps.setBigDecimal(10, BigDecimal.valueOf(transaction.getTransactionTimestamp()));
            ps.setBigDecimal(11, transaction.getValue());
            ps.setDouble(12, transaction.getApproxValue());
            ps.setString(13, transaction.getTransactionLog());
            ps.setString(14, transaction.getData());
            ps.setString(15, transaction.getNonce());
            ps.setString(16, transaction.getTxError());
            ps.setString(17, transaction.getContractAddr());
            ps.setInt(18, transaction.getBlockYear());
            ps.setInt(19, transaction.getBlockMonth());
            ps.setInt(20, transaction.getBlockDay());
            ps.execute();

            con.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                Objects.requireNonNull(con).rollback();


            } catch (SQLException |NullPointerException e1) {
                GENERAL.debug("Threw exception while saving in transaction save: ",e);

            }
            return false;

        }
        finally {
            try {
                Objects.requireNonNull(ps).close();
                Objects.requireNonNull(con).close();
            } catch (SQLException |NullPointerException e) {
                GENERAL.debug("Threw exception while closing resources in transaction save: ",e);
            }
        }
        return true;
    }

    @Override
    public boolean save(List<Transaction> transactions) {
        Connection con = null;
        PreparedStatement ps = null;

        try{
            con=DbConnectionPool.getConnection ();
            ps = con.prepareStatement(DbQuery.TransactionInsert);

            for(Transaction transaction:transactions) {

                ps.setString(1, transaction.getTransactionHash());
                ps.setString(2, transaction.getBlockHash());
                ps.setLong(3, transaction.getBlockNumber());
                ps.setLong(4, transaction.getBlockTimestamp());
                ps.setLong(5, transaction.getTransactionIndex());
                ps.setString(6, transaction.getFromAddr());
                ps.setString(7, transaction.getToAddr());
                ps.setLong(8, transaction.getNrgConsumed());
                ps.setLong(9, transaction.getNrgPrice());
                ps.setBigDecimal(10, BigDecimal.valueOf(transaction.getTransactionTimestamp()));
                ps.setBigDecimal(11, (transaction.getValue()));
                ps.setDouble(12, transaction.getApproxValue());
                ps.setString(13, transaction.getTransactionLog());
                ps.setString(14, transaction.getData());
                ps.setString(15, transaction.getNonce());
                ps.setString(16, transaction.getTxError());
                ps.setString(17, transaction.getContractAddr());
                ps.setInt(18, transaction.getBlockYear());
                ps.setInt(19, transaction.getBlockMonth());
                ps.setInt(20, transaction.getBlockDay());
                ps.execute();
            }



            con.commit();




        } catch (SQLException e) {
            GENERAL.debug("Threw exception while saving in transaction save: ",e);
            try {
                Objects.requireNonNull(con).rollback();


            } catch (SQLException | NullPointerException e1) {
                GENERAL.debug("Threw exception while saving in transaction save: ",e1);

            }
            return false;

        }
        finally {
            try {
                Objects.requireNonNull(ps).close();
                Objects.requireNonNull(con).close();
            } catch (SQLException |NullPointerException e) {
                GENERAL.debug("Threw exception while closing resources in transaction save: ",e);
            }
        }
        return true;
    }

    @Override
    public List<Long> integrityCheck(long startingBlockNumber) throws SQLException {
        List<Long> result = new ArrayList<>();

        PreparedStatement ps = null;

        try(Connection connection = DbConnectionPool.getConnection()){
            ps = connection.prepareStatement(DbQuery.TransactionSelectByBlockNumCountBlockNum);
            ps.setLong(1, startingBlockNumber);
            try (ResultSet rs = ps.executeQuery()){
                while (rs.next()){
                    long numTransaction = rs.getLong(1);
                    long blockNum = rs.getLong(2);

                    Block block = BlockServiceImpl.getInstance().getByBlockNumber(blockNum);

                    if ( block ==null || block.getNumTransactions() != numTransaction){
                        result.add(blockNum);
                    }
                }
            }


        }
        catch (SQLException | NullPointerException e){
            GENERAL.debug("Transaction integrity check threw exception: ", e);
            throw e;
        }
        finally {
            try {
                Objects.requireNonNull(ps).close();
            } catch (SQLException | NullPointerException e1) {
                GENERAL.debug("Threw exception in integrity check", e1);
            }
        }

        return result;
    }

    @Override
    public List<String> getTransactionHashByBlockNum(long startNum) throws SQLException {

        List<String> result = new ArrayList<>();

        try(Connection con = DbConnectionPool.getConnection()){
            try(PreparedStatement ps = con.prepareStatement(DbQuery.TransactionSelectIDByBlockNum)){
                ps.setLong(1, startNum);
                try (var res = ps.executeQuery()) {

                    while (res.next())
                        result.add(res.getString(1));
                }

            }
        }
        return result;
    }

    @Override
    public List<Transaction> getTransactionByBlockNumber(long blockNumber) throws SQLException {

        List<Transaction> result = new ArrayList<>();

        try(Connection con = DbConnectionPool.getConnection()){

            try(PreparedStatement ps = con.prepareStatement(DbQuery.TransactionByBlockNum)){
                ps.setLong(1 ,blockNumber);
                try (var res = ps.executeQuery()) {

                    while (res.next()) {
                        result.add(new Transaction.TransactionBuilder()
                                .setTransactionHash(res.getString("transaction_hash"))
                                .setBlockHash(res.getString("block_hash"))
                                .setBlockNumber(res.getLong("block_number"))
                                .setBlockTimestamp(res.getLong("block_timestamp"))
                                .setTransactionIndex(res.getLong("transaction_index"))
                                .setFromAddr(res.getString("from_addr"))
                                .setToAddr(res.getString("to_addr"))
                                .setNrgConsumed(res.getLong("nrg_consumed"))
                                .setNrgPrice(res.getLong("nrg_price"))
                                .setApproxValue(res.getDouble("approx_value"))
                                .setTransactionTimestamp(res.getBigDecimal("transaction_timestamp").longValue())
                                .setValue(res.getBigDecimal("value"))
                                .setTransactionLog(res.getString("transaction_log"))
                                .setData(res.getString("data"))
                                .setNonce(res.getString("nonce"))
                                .setTxError(res.getString("tx_error"))
                                .setContractAddr(res.getString("contract_addr"))
                                .build());

                    }
                }


            }
        }
        return result;
    }


    @Override
    public Transaction getTransactionByContractAddress(String contractAddress) throws SQLException {

        Transaction result = null;

        try(Connection con = DbConnectionPool.getConnection();
            PreparedStatement ps = con.prepareStatement(DbQuery.TransactionByContractAddress)){
            ps.setString(1 ,contractAddress);

            try(var res = ps.executeQuery()){


                while (res.next()){
                    result =new Transaction.TransactionBuilder()
                            .setTransactionHash(res.getString("transaction_hash"))
                            .setBlockHash(res.getString("block_hash"))
                            .setBlockNumber(res.getLong("block_number"))
                            .setBlockTimestamp(res.getLong("block_timestamp"))
                            .setTransactionIndex(res.getLong("transaction_index"))
                            .setFromAddr(res.getString("from_addr"))
                            .setToAddr(res.getString("to_addr"))
                            .setNrgConsumed(res.getLong("nrg_consumed"))
                            .setNrgPrice(res.getLong("nrg_price"))
                            .setTransactionTimestamp(res.getBigDecimal("transaction_timestamp").longValue())
                            .setValue(res.getBigDecimal("value"))
                            .setApproxValue(res.getDouble("approx_value"))
                            .setTransactionLog(res.getString("transaction_log"))
                            .setData(res.getString("data"))
                            .setNonce(res.getString("nonce"))
                            .setTxError(res.getString("tx_error"))
                            .setContractAddr(res.getString("contract_addr"))
                            .build();

                }


            }
        }
        return result;
    }

    @Override
    public PreparedStatement prepare(Connection con, List<Transaction> transactions) throws SQLException {
        PreparedStatement ps = con.prepareStatement(DbQuery.TransactionInsert);

        for (Transaction transaction : transactions) {

            ps.setString(1, transaction.getTransactionHash());
            ps.setString(2, transaction.getBlockHash());
            ps.setLong(3, transaction.getBlockNumber());
            ps.setLong(4, transaction.getBlockTimestamp());
            ps.setLong(5, transaction.getTransactionIndex());
            ps.setString(6, transaction.getFromAddr());
            ps.setString(7, transaction.getToAddr());
            ps.setLong(8, transaction.getNrgConsumed());
            ps.setLong(9, transaction.getNrgPrice());
            ps.setBigDecimal(10, BigDecimal.valueOf(transaction.getTransactionTimestamp()));
            ps.setBigDecimal(11, (transaction.getValue()));
            ps.setDouble(12, transaction.getApproxValue());
            ps.setString(13, transaction.getTransactionLog());
            ps.setString(14, transaction.getData());
            ps.setString(15, transaction.getNonce());
            ps.setString(16, transaction.getTxError());
            ps.setString(17, transaction.getContractAddr());
            ps.setInt(18, transaction.getBlockYear());
            ps.setInt(19, transaction.getBlockMonth());
            ps.setInt(20, transaction.getBlockDay());
            ps.addBatch();
        }

        return ps;

    }
}