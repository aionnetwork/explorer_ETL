package aion.dashboard.service;

import aion.dashboard.domainobject.InternalTransfer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface InternalTransferService {

    boolean save(InternalTransfer transfer);
    boolean save(List<InternalTransfer> transfers);
    PreparedStatement prepare(Connection con, List<InternalTransfer> transfers) throws SQLException;
    PreparedStatement prepareDelete(Connection con, long blockNumber) throws SQLException;

}
