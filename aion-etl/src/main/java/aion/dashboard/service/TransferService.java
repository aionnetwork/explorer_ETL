package aion.dashboard.service;

import aion.dashboard.domainobject.Transfer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface TransferService {

    boolean save(Transfer transfer);

    boolean save(List<Transfer> transfers);

    PreparedStatement prepare(Connection con, List<Transfer> transfers) throws SQLException;

}
