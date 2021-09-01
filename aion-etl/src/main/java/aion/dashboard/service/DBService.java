package aion.dashboard.service;

import aion.dashboard.blockchain.interfaces.Web3Service;
import aion.dashboard.config.Config;
import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.db.DbQuery;
import aion.dashboard.domainobject.ValidatorStats;
import aion.dashboard.exception.Web3ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.*;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

public class DBService {
    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");

    private static final DBService INSTANCE = new DBService();
    private static TreeMap<Instant, BigDecimal> circulatingSupply = new TreeMap<>(Instant::compareTo);
    private BlockService blockService;


    public DBService() {
        blockService = BlockServiceImpl.getInstance();
    }

    public static DBService getInstance() {
        return INSTANCE;
    }



    /*
    * TODO to deprecate
    *  */
    @Nullable
    public BigDecimal getCirculatingSupply() throws SQLException {

        if (circulatingSupply.isEmpty()) {
            try (Connection con = DbConnectionPool.getConnection();
                 PreparedStatement ps = con.prepareStatement(DbQuery.SelectCirculatingSupply);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    circulatingSupply.put(rs.getTimestamp("datetime").toInstant(), rs.getBigDecimal("supply"));
                }
            }
        }
        return circulatingSupply.firstEntry().getValue();
    }
/*
* This method will calculate the Total circulation Supply by the sum of all the blocks reward plus the networkBalanceAlloc.
* Total circulation Supply will be represented by the current height-4320 blocks  (aprox 1/2 day)
* @author Jesus Redon
* */
    public void generateCirculatingSupply() throws SQLException {

        long hight=blockService.getLastBlockNumber()-4320l;

        if(hight<=0)
            return;

        BigDecimal networkBalanceAlloc = Config.getInstance().getNetworkBalanceAlloc();
        BigDecimal totalBlockReward=new BigDecimal(0);
        BigDecimal addTotalBlockReward=new BigDecimal(0);
        BigDecimal blockNumber=new BigDecimal(0);
        boolean isCirculatingSupplyTableEmpty=true;
        try (Connection con = DbConnectionPool.getConnection();
            PreparedStatement psSelectCirculatingSupplyOne = con.prepareStatement(DbQuery.SelectCirculatingSupplyOne);
            PreparedStatement psSelectSumBlockReward = con.prepareStatement(DbQuery.SelectSumBlockReward);
            PreparedStatement psInsertCirculatingSupply = con.prepareStatement(DbQuery.InsertCirculatingSupply);
            PreparedStatement psUpdateCirculatingSupply = con.prepareStatement(DbQuery.UpdateCirculatingSupply);
        ) {

            try {
                ResultSet rsSelectCirculatingSupplyOne = psSelectCirculatingSupplyOne.executeQuery();
                while (rsSelectCirculatingSupplyOne.next()) {
                    totalBlockReward = new BigDecimal(rsSelectCirculatingSupplyOne.getString("total_block_reward"));
                    blockNumber = rsSelectCirculatingSupplyOne.getBigDecimal("block_number");
                    isCirculatingSupplyTableEmpty = false;
                }
                GENERAL.info("Starting calculating circulating supply from block {} to block {}", blockNumber, hight);
                psSelectSumBlockReward.setBigDecimal(1, blockNumber);
                psSelectSumBlockReward.setLong(2, hight);

                ResultSet rsSelectSumBlockReward = psSelectSumBlockReward.executeQuery();
                if (rsSelectSumBlockReward.next()) {
                    addTotalBlockReward = totalBlockReward
                            .add(new BigDecimal(rsSelectSumBlockReward.getString(1)));
                }
                BigDecimal circulationSupply =null;
                if (isCirculatingSupplyTableEmpty) {

                    psInsertCirculatingSupply.setLong(1, hight);
                    psInsertCirculatingSupply.setString(2, addTotalBlockReward.toString());
                    circulationSupply = (addTotalBlockReward
                            .add(networkBalanceAlloc))
                            .divide(new BigDecimal(Math.pow(10, 18))).setScale(18, RoundingMode.HALF_EVEN);
                    psInsertCirculatingSupply.setBigDecimal(3, circulationSupply);
                    psInsertCirculatingSupply.execute();
                }
                else {

                    psUpdateCirculatingSupply.setLong(1, hight);
                    psUpdateCirculatingSupply.setString(2, addTotalBlockReward.toString());
                    circulationSupply = (addTotalBlockReward
                            .add(networkBalanceAlloc))
                            .divide(new BigDecimal(Math.pow(10, 18))).setScale(18, RoundingMode.HALF_EVEN);
                    psUpdateCirculatingSupply.setBigDecimal(3, circulationSupply);
                    psUpdateCirculatingSupply.setBigDecimal(4, blockNumber);
                    psUpdateCirculatingSupply.execute();
                }
                con.commit();
                GENERAL.info("Calculating circulating supply ENDS  from block {} to block {} with a new value for total Supply of  {}", blockNumber, hight, circulationSupply);
            }catch (SQLException e) {

                    try {
                        Objects.requireNonNull(con).rollback();
                    } catch (SQLException | NullPointerException e1) {
                        GENERAL.debug("Through an exception while rolling back. ", e);
                    }
                     throw e;
                }
            finally {
                    try {

                        if (psSelectCirculatingSupplyOne != null) {
                            psSelectCirculatingSupplyOne.close();
                        }
                        if (psSelectSumBlockReward != null) {
                            psSelectSumBlockReward.close();
                        }
                        if (psInsertCirculatingSupply != null) {
                            psInsertCirculatingSupply.close();
                        }
                        if (psUpdateCirculatingSupply != null) {
                            psUpdateCirculatingSupply.close();
                        }

                    } catch (Exception e1) {
                        GENERAL.debug("Threw exception in generateCirculatingSupply: ", e1);
                    }
                }
        }
    }
}
