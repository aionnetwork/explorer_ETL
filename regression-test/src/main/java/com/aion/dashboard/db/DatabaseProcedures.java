package com.aion.dashboard.db;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class DatabaseProcedures {

    private static final DatabaseProcedures INSTANCE = new DatabaseProcedures();

    public static DatabaseProcedures getInstance() {
        return INSTANCE;
    }

    /**
     *
     * @param connection
     * @param db
     * @param table
     * @return a set containing all the primary keys in a table
     * @throws SQLException
     */
    public Set<String> getPrimaryKeys(Connection connection, String db, String table) throws SQLException {
        try (ResultSet pkColumns = connection.getMetaData().getPrimaryKeys(null, db, table)){
            Set<String> results = new TreeSet<>();
            while (pkColumns.next()){
                results.add(pkColumns.getString("COLUMN_NAME"));
            }
            return results;
        }
    }

    /**
     * @param connection the database connection to be used
     * @param db the database name
     * @param table the table
     * @return a set containing all columns in a table
     * @throws SQLException
     */
    public Set<String> getColumns(Connection connection, String db, String table) throws SQLException {
        try (ResultSet columns = connection.getMetaData().getColumns(null, db, table, null)){
            Set<String> results = new TreeSet<>();
            while (columns.next()){
                results.add(columns.getString("COLUMN_NAME"));
            }
            return results;
        }
    }


    /**
     *
     * @param connection
     * @param db
     * @return A list of all the tables in a schema
     * @throws SQLException
     */
    public Set<String> getTableNames(Connection connection, String db) throws SQLException {
        String[] types= {"TABLE"};
        try (ResultSet rs = connection.getMetaData().getTables(db, null, "%", types)){
            Set<String> results = new TreeSet<>();
            while (rs.next()){
                results.add(rs.getString(3));
            }
            return results;
        }
    }

    public boolean compareTables(Connection connection, PrintStream outputStream,String table,String db1, String db2, String column, String... primaryKeys)
        throws SQLException {
        String query = buildQuery(table, db1, db2, column, primaryKeys);
        boolean failed = false;
        try (PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery()) {
            var metaData = rs.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                outputStream.print(metaData.getColumnName(i));
                if (i != metaData.getColumnCount()) {
                    outputStream.print(",");
                }
            }
            outputStream.println();

            while (rs.next()) {
                failed = true;
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    String contents = Objects.requireNonNullElse(rs.getObject(i),"<null>")
                        .toString().replaceAll(
                            "\"", "");
                    outputStream.print("\""+contents +"\"");
                    if (i != metaData.getColumnCount()) {
                        outputStream.print(",");
                    }
                }
                outputStream.println();
            }
        }
        return failed;
    }


    /**
     * Builds a query that finds the complement of the inner join of the two tables
     * @param table
     * @param db1
     * @param db2
     * @param column
     * @param primaryKeys
     * @return
     */
    public static String buildQuery(String table, String db1, String db2, String column,
        String[] primaryKeys) {
        return "Select "
            + listColumns(table, db1, db2, column, primaryKeys)
            + " from "
            + tableSpecifier(db1, table)
            + " left outer join "
            + tableSpecifier(db2, table)
            + " on "
            + joinConditions(db1, db2, table, column, primaryKeys)
            + " where "
            + buildNullCheck(db1, db2, table, column)
            + " union "
            + "select "
            + listColumns(table, db1, db2, column, primaryKeys)
            + " from "
            + tableSpecifier(db1, table)
            + " right outer join "
            + tableSpecifier(db2, table)
            + " on "
            + joinConditions(db1, db2, table, column, primaryKeys)
            + " where "
            + buildNullCheck(db1, db2, table, column);

    }

    /**
     *
     * @param db
     * @param table
     * @param column
     * @return the fully qualified column name along with a specified alias
     */
    private static String formatWithAlias(String db, String table, String column){
        return String.format("%s as %s", format(db, table, column), alias(db, column));
    }

    /**
     *
     * @param db
     * @param table
     * @param column
     * @return a fully qualified column name
     */
    private static String format(String db, String table, String column){
        return String.format("%s.%s.%s", db, table, column);
    }

    /**
     *
     * @param db
     * @param column
     * @return an alias for easy readability in the csv
     */
    private static String alias(String db, String column){
        return String.format("%s_%s", db,column);
    }

    /**
     *
     * @param db
     * @param table
     * @return the fully qualified table name
     */
    private static String tableSpecifier(String db, String table){
        return String.format("%s.%s", db, table);
    }

    /**
     *
     * @param table
     * @param dbName1
     * @param dbName2
     * @param column1
     * @param columns
     * @return a string containing the list of columns to be displayed
     */
    private static String listColumns (String table,String dbName1, String dbName2,String column1, String... columns){
        List<String> columnNames = new ArrayList<>();
        columnNames.add(column1);
        columnNames.addAll(Arrays.asList(columns));

        List<String> dbNames = List.of(dbName1, dbName2);
        StringBuilder builder = new StringBuilder();
        for (var db: dbNames){
            for (var column: columnNames){
                builder.append(formatWithAlias(db, table, column));
                builder.append(",");
            }
        }
        builder.deleteCharAt(builder.length() -1);
        builder.append(" ");
        return builder.toString();
    }

    /**
     *
     * @param dbName1
     * @param dbName2
     * @param table
     * @param column1
     * @param columns
     * @return a string containing the join conditions
     */
    private static String joinConditions(String dbName1, String dbName2,
        String table, String column1, String... columns){
        List<String> columnList = new ArrayList<>();
        columnList.add(column1);
        columnList.addAll(Arrays.asList(columns));
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < columnList.size(); i++) {
            builder.append(format(dbName1, table, columnList.get(i))).append("=").append(
                format(dbName2, table, columnList.get(i)));
            if (i != columnList.size() - 1){
                builder.append(" and ");
            }
            else {
                builder.append(" ");
            }
        }
        return builder.toString();
    }

    /**
     *
     * @param dbName1
     * @param dbName2
     * @param table
     * @param column
     * @return a null check on the columns
     */
    private static String buildNullCheck(String dbName1, String dbName2, String table, String column){
        return String.format("%s is null or %s is null",
            format(dbName1, table,column), format(dbName2, table, column));
    }

}
