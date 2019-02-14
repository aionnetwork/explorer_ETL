package aion.dashboard.service;

import aion.dashboard.domainobject.Account;
import aion.dashboard.domainobject.Block;
import aion.dashboard.domainobject.TokenHolders;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface BlockService {

    boolean save(Block block);
    boolean save(List<Block> blocks);

    boolean deleteFromAndUpdate(long blockNumber, List<TokenHolders> tokenHolders, List<Account> accounts) throws SQLException;

    Long getLastBlockNumber() throws SQLException;

    Block getByBlockNumber(long blockNumber) throws SQLException;

    List<Long> blockHashIntegrity(long startNum) throws SQLException;

    PreparedStatement prepare(Connection con, List<Block> blocks) throws SQLException;
}
