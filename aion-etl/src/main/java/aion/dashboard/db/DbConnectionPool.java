package aion.dashboard.db;


import aion.dashboard.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;

public class DbConnectionPool {
    private static  HikariDataSource datasource;
    private static HikariConfig configHK = new HikariConfig();


    private static HikariPool pool;




    // JDBC Driver Name & Database URL
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static String JDBC_DB_URL = "";

    // JDBC Database Credentials
    private static String JDBC_USER;
    private static String JDBC_PASS;
    private static Connection connection;
    private static Connection writeConnection;






    static {

        Config config = Config.getInstance();
        JDBC_DB_URL = "jdbc:mysql://" + config.getSqlIp() + "/" + config.getSqlDbName() +
                "?user=" + config.getSqlUsername() +
                "&password=" + config.getSqlPassword() +
                "&rewriteBatchedStatements=true&useSSL=false";
        JDBC_USER = config.getSqlUsername();
        JDBC_PASS = config.getSqlPassword();


        configHK.setJdbcUrl(JDBC_DB_URL);
        configHK.setUsername(JDBC_USER);
        configHK.setPassword(JDBC_PASS);
        configHK.setAutoCommit(false);
        configHK.setLeakDetectionThreshold(10_000);
        configHK.setConnectionTestQuery("SELECT 1");


        configHK.addDataSourceProperty("autoReconnect", true);
        configHK.addDataSourceProperty("cachePrepStmts", "true");
        configHK.addDataSourceProperty("prepStmtCacheSize", "250");
        configHK.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        configHK.addDataSourceProperty("rewriteBatchedStatements", true);
        configHK.setMinimumIdle(5);
        configHK.setMaximumPoolSize(10);



        // Creates an Instance of GenericObjectPool That Holds Our Pool of Connections Object!

        datasource = new HikariDataSource(configHK);


        pool = getPool(datasource);

    }


    public  static Connection getConnection() throws SQLException {
        return datasource.getConnection();


    }

    public static Connection getWriteConnection() throws SQLException {

        if(writeConnection==null || writeConnection.isClosed())
            writeConnection = datasource.getConnection();

        return writeConnection;
    }

    private static HikariPool getPool(final HikariDataSource ds) {
        try {
            Field field = ds.getClass().getDeclaredField("pool");
            field.setAccessible(true);
            return (HikariPool) field.get(ds);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }




    public static int getActiveConnections() {

        return pool.getActiveConnections();

    }

    public static int getTotalConnections() {

        return pool.getTotalConnections();

    }

    public static int getThreadsAwaitingConnection() {

        return pool.getThreadsAwaitingConnection();

    }

    public static int getIdleConnections() {

        return pool.getIdleConnections();

    }

}