package aion.dashboard.service;

import aion.dashboard.domainobject.Balance;
import aion.dashboard.domainobject.Block;
import aion.dashboard.domainobject.TokenBalance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface BlockService {

    boolean save(Block block);
    boolean save(List<Block> blocks);

    boolean deleteFromAndUpdate(long blockNumber, List<TokenBalance>tokenBalances, List<Balance> balances) throws SQLException;

    Block getByBlockNumber(long blockNumber) throws SQLException;
    List<Long> blockHashIntegrity(long startNum) throws SQLException;

    Long getMaxTransactionIdForBlock(long blockNumber) throws SQLException;

    PreparedStatement[] prepare(Connection con, List<Block> blocks) throws SQLException;
}
