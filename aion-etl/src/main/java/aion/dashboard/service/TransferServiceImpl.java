package aion.dashboard.service;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.db.DbQuery;
import aion.dashboard.domainobject.Transfer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class TransferServiceImpl implements TransferService {


    private static final TransferServiceImpl Instance = new TransferServiceImpl();


    private TransferServiceImpl() {
        if (Instance != null) {
            throw new IllegalStateException();
        }
    }

    public static TransferServiceImpl getInstance() {
        return Instance;
    }

    @Override
    public boolean save(Transfer transfer) {

        try (Connection con = DbConnectionPool.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(DbQuery.TRANSFER_INSERT)) {
                ps.setString(1, transfer.getToAddress());
                ps.setString(2, transfer.getFromAddress());
                ps.setString(3, transfer.getOperator());
                ps.setBigDecimal(4, transfer.getTokenValue());
                ps.setString(5, transfer.getContractAddress());
                ps.setLong(6, transfer.getTransactionId());
                ps.setLong(7, transfer.getBlockNumber());
                ps.setLong(8, transfer.getTransactionTimestamp());


                ps.execute();
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;

            }


        } catch (SQLException e) {

            return false;
        }


        return true;
    }

    @Override
    public boolean save(List<Transfer> transfers) {

        try (Connection con = DbConnectionPool.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(DbQuery.TRANSFER_INSERT)) {
                for (var transfer : transfers) {
                    ps.setString(1, transfer.getToAddress());
                    ps.setString(2, transfer.getFromAddress());
                    ps.setString(3, transfer.getOperator());
                    ps.setBigDecimal(4, transfer.getTokenValue());
                    ps.setString(5, transfer.getContractAddress());
                    ps.setLong(6, transfer.getTransactionId());
                    ps.setLong(7, transfer.getBlockNumber());
                    ps.setLong(8, transfer.getTransactionTimestamp());


                    ps.execute();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;

            }


        } catch (SQLException e) {

            return false;
        }


        return true;
    }

    @Override
    public PreparedStatement prepare(Connection con, List<Transfer> transfers) throws SQLException {
        PreparedStatement ps = con.prepareStatement(DbQuery.TRANSFER_INSERT);

        for (var transfer : transfers) {
            ps.setString(1, transfer.getToAddress());
            ps.setString(2, transfer.getFromAddress());
            ps.setString(3, transfer.getOperator());
            ps.setBigDecimal(4, transfer.getTokenValue());
            ps.setString(5, transfer.getContractAddress());
            ps.setLong(6, transfer.getTransactionId());
            ps.setLong(7, transfer.getBlockNumber());
            ps.setLong(8, transfer.getTransactionTimestamp());


            ps.addBatch();
        }

        return ps;
    }


}
