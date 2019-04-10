package aion.dashboard.service;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.db.DbQuery;
import aion.dashboard.domainobject.InternalTransfer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class InternalTransferServiceImpl implements InternalTransferService {
    private static InternalTransferServiceImpl Instance = new InternalTransferServiceImpl();

    private InternalTransferServiceImpl(){}
    public static InternalTransferServiceImpl getInstance() {
        return Instance;
    }

    @Override
    public boolean save(InternalTransfer transfer) {
        return save(Collections.singletonList(transfer));
    }

    @Override
    public boolean save(List<InternalTransfer> transfers) {
        try (Connection con = DbConnectionPool.getConnection()){
            try(var ps = prepare(con, transfers)){
                ps.executeBatch();

                con.commit();
            }catch (SQLException e){
                con.rollback();
                throw e;
            }


        } catch (SQLException e) {


            return false;
        }


        return true;
    }

    @Override
    public PreparedStatement prepare(Connection con, List<InternalTransfer> transfers) throws SQLException {
        PreparedStatement ps = con.prepareStatement(DbQuery.InternalTransferInsert);

        for (var transfer: transfers){
            ps.setString(1,transfer.getTransactionHash());
            ps.setString(2, transfer.getToAddr());
            ps.setString(3, transfer.getFromAddr());
            ps.setBigDecimal(4, transfer.getValueTransferred());
            ps.setLong(5, transfer.getTimestamp());
            ps.setLong(6, transfer.getBlockNumber());
            ps.setInt(7, transfer.getTransferCount());
            ps.setDouble(8, transfer.getApproxValue());


            ps.addBatch();
        }

        return ps;

    }

    @Override
    public PreparedStatement prepareDelete(Connection con, long blockNumber) throws SQLException {
        var ps = con.prepareStatement(DbQuery.InternalTransferDelete);
        ps.setLong(1, blockNumber);

        return ps;

    }
}
