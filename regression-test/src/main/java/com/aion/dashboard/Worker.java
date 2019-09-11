package com.aion.dashboard;

import com.aion.dashboard.config.Config;
import com.aion.dashboard.db.ConnectionHandler;
import com.aion.dashboard.db.DatabaseProcedures;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.function.Supplier;

public class Worker implements Supplier<String> {

    private final File dir;
    private final String table;
    private final String column;
    private final String[] primaryKeys;
    private final String db1;
    private final String db2;
    private final String testName;

    public Worker(File dir, String table, String column, String... primaryKeys) {
        this.dir = dir;
        this.table = table;
        this.column = column;
        this.primaryKeys = primaryKeys;
        this.db1 = Config.getInstance().hosts.db1;
        this.db2 = Config.getInstance().hosts.db2;
        testName = table + "-" + column;
    }

    public String get() {
        for (String key : primaryKeys) {
            if (key.equalsIgnoreCase(column)) {
                return testName + " Cannot check integrity of primary key component.";
            }
        }
        File outputFile;
        if (dir.isDirectory()) {
            String parent = dir.getPath();
            String name = String.format("regression_test_%s-%s%s.csv", table, column,
                Instant.now().atZone(ZoneId.of("UTC")).toLocalDateTime().toString());
            outputFile = new File(parent + File.separator + name);
            try {
                if (!outputFile.createNewFile()) {
                    System.out.println("Failed to create file " + name);
                    return testName + " Cannot create a new file.";
                }
            } catch (IOException e) {
                System.out.println("Failed to create file " + name);
                return testName + " Cannot create a new file";
            }
        } else {
            return "";
        }
        try (Connection connection = ConnectionHandler.getInstance().getConnection();
            PrintStream outputStream = new PrintStream(outputFile)) {
            System.out.println("Finished setup starting test for " + table + "->" + column);
            final boolean result = DatabaseProcedures.getInstance()
                .compareTables(connection, outputStream, table, db1,
                    db2, column, primaryKeys);
            if (result) {
                return testName + " Found errors";
            }
        } catch (FileNotFoundException e) {
            System.out.println("Failed to find output file");
        } catch (SQLException e) {
        } catch (Exception e) {
            e.printStackTrace();
            return testName + " Error in check. " + e.getMessage();
        }
        return "";
    }


}
