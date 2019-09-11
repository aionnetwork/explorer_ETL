package com.aion.dashboard;

import com.aion.dashboard.config.Config;
import com.aion.dashboard.db.ConnectionHandler;
import com.aion.dashboard.db.DatabaseProcedures;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws SQLException, IOException {
        ExecutorService exectutor = Executors.newFixedThreadPool(4);
        CompletableFuture<String> finished = CompletableFuture.supplyAsync(()-> "", exectutor);
        DatabaseProcedures databaseProcedures = DatabaseProcedures.getInstance();
        ConnectionHandler handler = ConnectionHandler.getInstance();
        Config config = Config.getInstance();
        List<Supplier<String>> runnables = new ArrayList<>();
        File outputDir = new File("test_output" + File.separator + Instant.now().atZone(ZoneId.of("UTC")).format(
            DateTimeFormatter.ISO_LOCAL_DATE) + "-" + System.currentTimeMillis());

        if ((outputDir.getParentFile().exists() || !outputDir.getParentFile().mkdir()) && !outputDir.mkdir()) {
            System.out.println("Failed to create file");
            System.exit(255);
        }


        try (Connection con = handler.getConnection()) {

            // get the table names
            Set<String> exempt = new HashSet<>(Arrays.asList(config.exemptTables));
            String database1 = config.hosts.db1;
            String database2 = config.hosts.db2;
            Set<String> tableNamesDB1 = databaseProcedures.getTableNames(con, database1);
            tableNamesDB1.removeAll(exempt);
            Set<String> tableNamesDB2 = databaseProcedures.getTableNames(con, database2);
            tableNamesDB2.removeAll(exempt);
            if (!tableNamesDB1.equals(tableNamesDB2)){
                System.out.println("Test databases do not have the same tables.");
                System.out.printf("Database 1 => [%s]%n ", String.join(",", tableNamesDB1));
                System.out.printf("Database 2 => [%s]%n ", String.join(",", tableNamesDB2));
            }
            // get columns for each table
            System.out.println("Using tables listed in " + database1 + " tables: ");
            System.out.println(String.join("\n", tableNamesDB1));
            AtomicLong count = new AtomicLong(0);
            AtomicLong completed = new AtomicLong(0);
            for (String table: tableNamesDB1){
                // get primary key for each table
                Set<String> primaryKey = databaseProcedures.getPrimaryKeys(con, database1, table);
                Set<String> columns = databaseProcedures.getColumns(con, database1, table);
                columns.removeAll(primaryKey);
                System.out.println("Searching for keys in table :" + table);
                System.out.println("Columns found ");
                System.out.println(String.join("\n\t", columns));
                for (String column: columns){
                    Worker worker = new Worker(outputDir,table, column, primaryKey.toArray(new String[]{}));
                    runnables.add(worker);
                    count.incrementAndGet();
                }
            }

            System.out.println("Spawning workers..");

            for (Supplier<String> worker: runnables){
                CompletableFuture<String> future = CompletableFuture.supplyAsync(worker, exectutor);
                finished = future.thenCombine(future, (a, b) -> {
                    long num = completed.incrementAndGet();
                    System.out.println("Completed: "+ completed.get() + " of " + count.get()+".");
                    return "test: "+ num +" "+ a +"\n"+b;
                });
            }
            //spawn workers for each column
            finished.join();
        }
        finally {
            exectutor.shutdownNow();
        }
        System.exit(1);
    }
}
