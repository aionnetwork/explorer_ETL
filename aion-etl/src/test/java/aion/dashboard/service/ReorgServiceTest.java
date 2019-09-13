package aion.dashboard.service;

import aion.dashboard.db.DbConnectionPool;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReorgServiceTest {
    @Test
    void storeDetailsTest() throws SQLException {
        long blockNumber = 10;
        int depth = 50;
        long transactionNum = 100;
        String addressString = "[address1,address2]";
        ReorgServiceImpl.doStore(blockNumber, depth, addressString, transactionNum);
        assertTrue(exists(blockNumber, depth, addressString, transactionNum));

        assertThrows(Exception.class, ()-> ReorgServiceImpl.doStore(0, 0, null, 0));
    }

    boolean exists(long blockNumber, int depth, String addressString, long transactionNum) throws SQLException {
        try (Connection connection = DbConnectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM aion.reorg_details WHERE block_number=? AND block_depth=? AND affected_addresses=? AND number_of_affected_transactions=?")) {
            ps.setLong(1, blockNumber);
            ps.setInt(2, depth);
            ps.setString(3, addressString);
            ps.setLong(4, transactionNum);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
