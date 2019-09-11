package com.aion.dashboard.db;

import com.aion.dashboard.config.Config;
import com.aion.dashboard.config.HostConfig.HostDetails;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.SQLException;

public class ConnectionHandler {
    private static final ConnectionHandler CONNECTION_HANDLER;

    public static ConnectionHandler getInstance() {
        return CONNECTION_HANDLER;
    }

    static {
        CONNECTION_HANDLER = new ConnectionHandler();
        CONNECTION_HANDLER.initialize();
    }
    private HikariDataSource datasource;

    private ConnectionHandler(){
        if (CONNECTION_HANDLER != null){
            throw new IllegalStateException("Singleton instance already created.");
        }
    }

    private HikariConfig hikariConfigFromHostDetails(HostDetails hostDetails){
        HikariConfig config = new HikariConfig();
        String url = String.format("jdbc:mysql://%s:%s", hostDetails.url,
            hostDetails.port);
        config.setJdbcUrl(url);
        config.setUsername(hostDetails.user);
        config.setPassword(hostDetails.password);
        config.setAutoCommit(false);
        config.setMaximumPoolSize(10);

        return config;
    }

    public java.sql.Connection getConnection() throws SQLException {
        return datasource.getConnection();
    }

    private void initialize(){
        Config config = Config.getInstance();
        datasource = new HikariDataSource(hikariConfigFromHostDetails(config.hosts.host1));
    }
}
