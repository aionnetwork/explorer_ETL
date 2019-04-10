package aion.dashboard.service;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.util.TimeLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DBProcedureServiceImpl implements DBProcedureService {

    private static final DBProcedureServiceImpl INSTANCE=new DBProcedureServiceImpl();
    private final TimeLogger TimeLogger;
    private final Logger General = LoggerFactory.getLogger("logger_general");

    public static DBProcedureServiceImpl getINSTANCE() {
        return INSTANCE;
    }

    private DBProcedureServiceImpl() {
        if (INSTANCE != null) {
            throw new IllegalStateException("Illegal attempt to create a new instance of DBProcedureService");
        }
        TimeLogger = new TimeLogger(getClass().getName());

    }


    @Override
    public List<Long> runStoredTransactionIntegrityProcedure(long startNum) {
        CallableStatement callableStatement =null;
        List<Long> result = new ArrayList<>();
        try(Connection con = DbConnectionPool.getConnection()){

            callableStatement = con.prepareCall("{CALL NumTransactionIntegrity(?)}");
            callableStatement.setLong(1,startNum);
            try (ResultSet rs = callableStatement.executeQuery()){
                while (rs.next()){
                    result.add(rs.getLong(1));
                }
            }
        } catch (SQLException e) {


            General.debug("Threw an exception when running transaction integrity procedure: ", e);
        }
        finally {
            try {
                Objects.requireNonNull(callableStatement).close();
            } catch (SQLException | NullPointerException e) {
                General.debug("Threw an exception when running transaction integrity procedure: ", e);

            }
        }


        return result;
    }

    @Override
    public List<Long> runStoredBlockIntegrityProcedure(long startNum) {
        CallableStatement callableStatement =null;
        List<Long> result = new ArrayList<>();
        try(Connection con = DbConnectionPool.getConnection()){

            callableStatement = con.prepareCall("{CALL BlockHashIntegrity(?)}");
            callableStatement.setLong(1,startNum);
            try (ResultSet rs = callableStatement.executeQuery()){
                while (rs.next()){
                    result.add(rs.getLong(1));
                }
            }
        } catch (SQLException e) {


            General.debug("Threw an exception when running block hash integrity procedure: ", e);
        }
        finally {
            try {
                Objects.requireNonNull(callableStatement).close();
            } catch (SQLException | NullPointerException e) {
                General.debug("Threw an exception when running block hash integrity procedure: ", e);

            }
        }



        return result;
    }
}
