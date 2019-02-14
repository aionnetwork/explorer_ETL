package aion.dashboard.service;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.db.DbQuery;
import aion.dashboard.domainobject.Block;
import aion.dashboard.domainobject.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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


        return save(Collections.singletonList(transaction));
    }

    @Override
    public boolean save(List<Transaction> transactions) {

        try(var con=DbConnectionPool.getConnection ()){

            var psArr = prepare(con, transactions);

            try(var ps = psArr[0];
                var psMap = psArr[1]){
                ps.executeBatch();
                psMap.executeBatch();
                con.commit();
            }
            catch (SQLException e){
                con.rollback();
            }

            return true;
        } catch (SQLException e) {
            GENERAL.debug("Threw exception while saving in transaction save: ",e);
            return false;

        }

    }

    @Override
    public List<Long> integrityCheck(long startingBlockNumber) throws SQLException {
        List<Long> result = new ArrayList<>();

        PreparedStatement ps = null;

        try(Connection connection = DbConnectionPool.getConnection()){
            ps = connection.prepareStatement(DbQuery.TRANSACTION_SELECT_BY_ID_COUNT_BLOCK_NUM);
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
    public List<Long> getTransactionIndexByBlockNum(long startNum) throws SQLException {

        List<Long> result = new ArrayList<>();

        try(Connection con = DbConnectionPool.getConnection();
            PreparedStatement ps = con.prepareStatement(DbQuery.TRANSACTION_SELECT_ID_BY_BLOCK_NUM)){
            ps.setLong(1 ,startNum);

            try(var res = ps.executeQuery()){


                while (res.next()){
                    result.add(res.getLong(1));

                }


            }

        }
        return result;
    }

    @Override
    public List<Transaction> getTransactionByBlockNumber(long blockNumber) throws SQLException {

        List<Transaction> result = new ArrayList<>();

        try(Connection con = DbConnectionPool.getConnection()){
            ResultSet res = null;
            try(PreparedStatement ps = con.prepareStatement(DbQuery.TRANSACTION_BY_BLOCK_NUM)){
                ps.setLong(1 ,blockNumber);
                res = ps.executeQuery();

                while (res.next()){
                    result.add(new Transaction.TransactionBuilder().
                            setId(new BigInteger(res.getString("id"))).
                            setTransactionHash(res.getString("transaction_hash")).
                            setBlockHash(res.getString("block_hash")).
                            setBlockNumber(res.getLong("block_number")).
                            setTransactionIndex(res.getLong("transaction_index")).
                            setFromAddr(res.getString("from_addr")).
                            setToAddr(res.getString("to_addr")).
                            setNrgConsumed(res.getLong("nrg_consumed")).
                            setNrgPrice(res.getLong("nrg_price")).
                            setTransactionTimestamp(res.getLong("transaction_timestamp")).
                            setBlockTimestamp(res.getLong("block_timestamp")).
                            setValue(res.getString("value")).
                            setTransactionLog(res.getString("transaction_log")).
                            setData(res.getString("data")).
                            setNonce(res.getString("nonce")).
                            setTxError(res.getString("tx_error")).
                            setContractAddr(res.getString("contract_addr"))
                            .build());

                }


            }
            finally {
                try {
                    Objects.requireNonNull(res).close();
                    Objects.requireNonNull(con).close();
                }
                catch (Exception ignored){

                }
            }
        }
        return result;
    }


    @Override
    public Transaction getTransactionByContractAddress(String contractAddress) throws SQLException {

        Transaction result = null;

        try(Connection con = DbConnectionPool.getConnection()){
            ResultSet res = null;
            try(PreparedStatement ps = con.prepareStatement(DbQuery.TRANSACTION_BY_CONTRACT_ADDRESS)){
                ps.setString(1 ,contractAddress);
                res = ps.executeQuery();

                while (res.next()){
                    result =new Transaction.TransactionBuilder().
                            setId(new BigInteger(res.getString("id"))).
                            setTransactionHash(res.getString("transaction_hash")).
                            setBlockHash(res.getString("block_hash")).
                            setBlockNumber(res.getLong("block_number")).
                            setTransactionIndex(res.getLong("transaction_index")).
                            setFromAddr(res.getString("from_addr")).
                            setToAddr(res.getString("to_addr")).
                            setNrgConsumed(res.getLong("nrg_consumed")).
                            setNrgPrice(res.getLong("nrg_price")).
                            setTransactionTimestamp(res.getLong("transaction_timestamp")).
                            setBlockTimestamp(res.getLong("block_timestamp")).
                            setValue(res.getString("value")).
                            setTransactionLog(res.getString("transaction_log")).
                            setData(res.getString("data")).
                            setNonce(res.getString("nonce")).
                            setTxError(res.getString("tx_error")).
                            setContractAddr(res.getString("contract_addr"))
                            .build();

                }


            } finally {
                try {
                    Objects.requireNonNull(res).close();
                    Objects.requireNonNull(con).close();
                } catch (Exception ignored){

                }
            }
        }
        return result;
    }

    @Override
    public long getTransactionId(String txHash) throws SQLException {
        long result = -1L;

        try (Connection con = DbConnectionPool.getConnection()) {
            ResultSet res = null;
            try (PreparedStatement ps = con.prepareStatement(DbQuery.TRANSACTION_MAP_SELECT_ID_BY_HASH)) {
                ps.setString(1, txHash);
                res = ps.executeQuery();

                while (res.next()) {
                    result = res.getLong("id");
                }


            } finally {
                try {
                    Objects.requireNonNull(res).close();
                    Objects.requireNonNull(con).close();
                } catch (Exception ignored) {

                }
            }
        }
        return result;
    }

    @Override
    public PreparedStatement[] prepare(Connection con, List<Transaction> transactions) throws SQLException {
        PreparedStatement ps = con.prepareStatement(DbQuery.TRANSACTION_INSERT);
        PreparedStatement psMap = con.prepareStatement(DbQuery.INSERT_TRANSACTION_MAP);

        for (Transaction transaction : transactions) {


            ps.setLong(1, transaction.getId().longValue());
            ps.setString(2, transaction.getTransactionHash());
            ps.setString(3, transaction.getBlockHash());
            ps.setLong(4, transaction.getBlockNumber());
            ps.setLong(5, transaction.getTransactionIndex());
            ps.setString(6, transaction.getFromAddr());
            ps.setString(7, transaction.getToAddr());
            ps.setLong(8, transaction.getNrgConsumed());
            ps.setLong(9, transaction.getNrgPrice());
            ps.setLong(10, transaction.getTransactionTimestamp());
            ps.setLong(11, transaction.getBlockTimestamp());
            ps.setString(12, transaction.getValue());
            ps.setString(13, transaction.getTransactionLog());
            ps.setString(14, transaction.getData());
            ps.setString(15, transaction.getNonce());
            ps.setString(16, transaction.getTxError());
            ps.setString(17, transaction.getContractAddr());


            psMap.setString(1, transaction.getTransactionHash());
            psMap.setLong(2, transaction.getId().longValue());


            ps.addBatch();
            psMap.addBatch();
        }

        return new PreparedStatement[]{ps, psMap};

    }
}
