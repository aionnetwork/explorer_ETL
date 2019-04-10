package com.aion.dashboard.etl.tests;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TestConfig {
    private List<Connection> connections = new ArrayList<>();

    private final int RANGE_MIN = Integer.parseInt(Optional.ofNullable(System.getenv("RANGE_MIN")).orElse("1384500"));
    private final int RANGE_MAX = Integer.parseInt(Optional.ofNullable(System.getenv("RANGE_MAX")).orElse("1385000"));

    private final String V3_IP = Optional.ofNullable(System.getenv("V3_IP")).orElse("localhost");
    private final String V3_NAME = Optional.ofNullable(System.getenv("V3_NAME")).orElse("aionv3");
    private final String V3_USER = Optional.ofNullable(System.getenv("V3_USER")).orElse("admin1");
    private final String V3_PASSWORD = Optional.ofNullable(System.getenv("V3_PASSWORD")).orElse("password");

    private final String V4_IP = Optional.ofNullable(System.getenv("V4_IP")).orElse("localhost");
    private final String V4_NAME = Optional.ofNullable(System.getenv("V4_NAME")).orElse("aionv4");
    private final String V4_USER = Optional.ofNullable(System.getenv("V4_USER")).orElse("admin1");
    private final String V4_PASSWORD = Optional.ofNullable(System.getenv("V4_PASSWORD")).orElse("password");

    private final String V5_IP = Optional.ofNullable(System.getenv("V5_IP")).orElse("localhost");
    private final String V5_NAME = Optional.ofNullable(System.getenv("V5_NAME")).orElse("aion");
    private final String V5_USER = Optional.ofNullable(System.getenv("V5_USER")).orElse("admin1");
    private final String V5_PASSWORD = Optional.ofNullable(System.getenv("V5_PASSWORD")).orElse("password");

    Connection getConnection(int dbVersion) throws SQLException {
        for(Connection connection: connections) {
            if(dbVersion == 3 && connection.getCatalog().equalsIgnoreCase("aionv3")) {
                return connection;
            } else if(dbVersion == 4 && connection.getCatalog().equalsIgnoreCase("aionv4")) {
                return connection;
            } else if(dbVersion == 5 && connection.getCatalog().equalsIgnoreCase("aion")) {
                return connection;
            }
        }

        throw new NullPointerException();
    }

    TestConfig(List<Integer> versions) throws SQLException {
        for(Integer version: versions) {
            switch (version) {
                case 3:
                    setV3Connection();
                    break;
                case 4:
                    setV4Connection();
                    break;
                case 5:
                    setV5Connection();
                    break;
                default:
                    throw new NullPointerException();
            }
        }
    }

    private void setV3Connection() throws SQLException {
        connections.add(DriverManager.getConnection("jdbc:mysql://" + V3_IP + "/" + V3_NAME, V3_USER, V3_PASSWORD));
    }
    private void setV4Connection() throws SQLException {
        connections.add(DriverManager.getConnection("jdbc:mysql://" + V4_IP + "/" + V4_NAME, V4_USER, V4_PASSWORD));
    }
    private void setV5Connection() throws SQLException {
        connections.add(DriverManager.getConnection("jdbc:mysql://" + V5_IP + "/" + V5_NAME, V5_USER, V5_PASSWORD));
    }

    void closeConnections() throws SQLException {
        for(Connection connection: connections) {
            connection.close();
        }
    }

    int getRangeMin() {
        return RANGE_MIN;
    }
    int getRangeMax() {
        return RANGE_MAX;
    }
}
