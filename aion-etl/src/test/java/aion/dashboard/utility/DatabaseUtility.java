package aion.dashboard.utility;

import aion.dashboard.db.DbConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseUtility {

    private static DatabaseUtility INSTANCE;
    private Connection borrowed;
    static {
        try {
            INSTANCE = new DatabaseUtility();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static DatabaseUtility getInstance() {
        return INSTANCE;
    }


    private DatabaseUtility() throws SQLException {
        borrowed = DbConnectionPool.getConnection();


    }


    /**
     * TODO convert to void only true is ever returned
     * Truncates all tables and resets the parser state.
     * @return A boolean indicating the db was successfully cleared
     * @throws SQLException
     */

    public boolean clearDB() throws SQLException {
        String[] tables = new String[]{"block", "block_map", "parser_state", "transaction", "transaction_map"};

        System.out.println("Clearing out database");
        while (true) {
            try {
                int i = 0;
                while (i < tables.length) {
                    borrowed.createStatement().execute("TRUNCATE Table "+ tables[i]);
                    i++;
                }

                /**
                 *
                 */
                borrowed.createStatement().execute("insert into parser_state values(1,-1,-1);");
                borrowed.createStatement().execute("insert into parser_state values(2,-1,-1);");
                borrowed.createStatement().execute("insert into parser_state values(3,-1,-1);");

                borrowed.commit();

                break;
            } catch (SQLException e) {
                borrowed.rollback();
                e.printStackTrace();
            }
        }

        return true;

    }
}
