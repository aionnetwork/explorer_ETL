package aion.dashboard.service;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.db.DbQuery;
import org.spongycastle.asn1.cmp.PollRepContent;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

public class DBService {
    private static final DBService INSTANCE = new DBService();
    private static TreeMap<Instant, BigDecimal> circulatingSupply = new TreeMap<>(Instant::compareTo);

    public static DBService getInstance() {
        return INSTANCE;
    }

    @Nullable
    public BigDecimal getCirculatingSupply(Instant timestamp) throws SQLException {
        if (circulatingSupply.isEmpty()) {
            try (Connection con = DbConnectionPool.getConnection();
                 PreparedStatement ps = con.prepareStatement(DbQuery.SelectCirculatingSupply);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    circulatingSupply.put(rs.getTimestamp("start_date").toInstant(), rs.getBigDecimal("supply"));
                }
            }
        }
        return circulatingSupply.floorEntry(timestamp).getValue();
    }
}
