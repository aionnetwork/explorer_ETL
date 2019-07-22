package aion.dashboard.service;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.db.DbQuery;
import aion.dashboard.domainobject.UpdateState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UpdateStateServiceImpl implements UpdateStateService {


    private static final UpdateStateServiceImpl INSTANCE = new UpdateStateServiceImpl();

    public static UpdateStateServiceImpl getInstance() {
        return INSTANCE;
    }


    private UpdateStateServiceImpl(){}
    @Override
    public Optional<UpdateState> find(int id) {
        try (Connection con = DbConnectionPool.getConnection();
        PreparedStatement ps = con.prepareStatement(DbQuery.SelectFromUpdateState)){
            ps.setInt(1, id);
            try(ResultSet resultSet = ps.executeQuery()){
                if (resultSet.next()){
                    return Optional.ofNullable(
                            UpdateState.builder().setEnd(resultSet.getLong("end"))
                            .setStart(resultSet.getLong("start"))
                            .setId(resultSet.getInt("table_id"))
                            .setTableName(resultSet.getString("table_name"))
                            .setRunUpdate(resultSet.getBoolean("run_update"))
                            .createUpdateState()
                    );


                }else {
                    return Optional.empty();
                }
            }


        }catch (SQLException e){
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public PreparedStatement update(Connection con, UpdateState updateState) throws SQLException {
        var ps = con.prepareStatement(DbQuery.InsertUpdateState);

        ps.setInt(1, updateState.getId());
        ps.setString(2, updateState.getTableName());
        ps.setBoolean(3, updateState.isRunUpdate());
        ps.setLong(4, updateState.getStart());
        ps.setLong(5, updateState.getEnd());


        ps.addBatch();

        return ps;
    }
}
