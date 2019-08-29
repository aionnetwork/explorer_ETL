package aion.dashboard.service;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.db.DbQuery;
import aion.dashboard.domainobject.InternalTransaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class InternalTransactionServiceImpl implements InternalTransactionService {
    private static final InternalTransactionServiceImpl INSTANCE = new InternalTransactionServiceImpl();

    public static InternalTransactionServiceImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean save(List<InternalTransaction> internalTransaction) {
        try(Connection con = DbConnectionPool.getConnection();
            PreparedStatement ps = prepare(con, internalTransaction)){
            try {
                ps.executeBatch();
                con.commit();
                return true;
            }catch (Exception e){
                con.rollback();
                throw e;
            }
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public boolean save(InternalTransaction internalTransaction) {
        return save(Collections.singletonList(internalTransaction));
    }

    @Override
    public PreparedStatement prepareDelete(long blockNumber, Connection connection) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(DbQuery.DeleteFromInternalTransaction);
        ps.setLong(1, blockNumber);
        return ps;
    }

    @Override
    public PreparedStatement deleteExisting(long blockNumber, Connection connection) throws Exception {
        PreparedStatement ps = connection.prepareStatement(DbQuery.DeleteOneFromInternalTransaction);
        ps.setLong(1, blockNumber);
        return ps;
    }

    @Override
    public PreparedStatement prepare(Connection con, List<InternalTransaction> internalTransactions) throws SQLException {
        PreparedStatement ps = con.prepareStatement(DbQuery.InsertInternalTransaction);
        for (var internalTransaction: internalTransactions){
            ps.setString(1,internalTransaction.getTransactionHash());
            ps.setInt(2, internalTransaction.getInternalTransactionIndex());
            ps.setBigDecimal(3, internalTransaction.getNrgPrice());
            ps.setBigDecimal(4, internalTransaction.getNrgLimit());
            ps.setString(5, internalTransaction.getData());
            ps.setBoolean(6, internalTransaction.isRejected());
            ps.setString(7, internalTransaction.getKind());
            ps.setString(8, internalTransaction.getFromAddr());
            ps.setString(9, internalTransaction.getToAddr());
            ps.setBigDecimal(10, internalTransaction.getNonce());
            ps.setLong(11, internalTransaction.getBlockNumber());
            ps.setBigDecimal(12, internalTransaction.getValue());
            ps.setLong(13, internalTransaction.getBlockTimestamp());
            ps.setString(14, internalTransaction.getContractAddress());
            ps.addBatch();
        }
        return ps;
    }


}
