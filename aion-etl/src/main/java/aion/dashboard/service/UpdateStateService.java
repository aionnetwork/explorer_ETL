package aion.dashboard.service;

import aion.dashboard.domainobject.UpdateState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

public interface UpdateStateService {

    Optional<UpdateState> find(int id);

    PreparedStatement update(Connection con, UpdateState updateState) throws SQLException;


}
