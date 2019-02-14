package aion.dashboard.service;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.db.DbQuery;
import aion.dashboard.domainobject.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BlockServiceImpl implements BlockService {

    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");
    private static final BlockServiceImpl INSTANCE = new BlockServiceImpl();


    public static BlockServiceImpl getInstance() {
        return INSTANCE;
    }

    private BlockServiceImpl(){

    }


    @Override
    public boolean save(Block block) {


        try (var con = DbConnectionPool.getConnection ()){
            Block comp=getByBlockNumber(block.getBlockNumber());


            if(comp==null ||!comp.equals(block)) {
                try (var ps = con.prepareStatement(DbQuery.InsertBlock)) {


                    ps.setLong(1, block.getBlockNumber());
                    ps.setString(2, block.getBlockHash());
                    ps.setString(3, block.getMinerAddress());
                    ps.setString(4, block.getParentHash());
                    ps.setString(5, block.getReceiptTxRoot());
                    ps.setString(6, block.getStateRoot());
                    ps.setString(7, block.getTxTrieRoot());
                    ps.setString(8, block.getExtraData());
                    ps.setString(9, block.getNonce());
                    ps.setString(10, block.getBloom());
                    ps.setString(11, block.getSolution());
                    ps.setLong(12, block.getDifficulty());
                    ps.setLong(13, block.getTotalDifficulty());
                    ps.setLong(14, block.getNrgConsumed());
                    ps.setLong(15, block.getNrgLimit());
                    ps.setLong(16, block.getBlockSize());
                    ps.setTimestamp(17, new Timestamp(block.getBlockTimestamp()));
                    ps.setLong(18, block.getNumTransactions());
                    ps.setLong(19, block.getBlockTime());
                    ps.setBigDecimal(20, block.getNrgReward());
                    ps.setDouble(21, block.getApproxNrgReward());
                    ps.setString(22, block.getLastTransactionHash());
                    ps.setString(23, block.getTransactionHashes());
                    ps.setInt(24, block.getBlockYear());
                    ps.setInt(25, block.getBlockMonth());
                    ps.setInt(26, block.getBlockDay());
                    ps.execute();

                    con.commit();

                }
                catch (SQLException e){
                    con.rollback();
                    throw e;
                }

            }

        } catch (SQLException e) {
            return false;

        }
        return true;
    }

    @Override
    public boolean save(List<Block> blocks) {

        try(Connection con= DbConnectionPool.getConnection();
            PreparedStatement ps = con.prepareStatement(DbQuery.InsertBlock)) {
            try {
                for (Block block : blocks) {
                    Block comp = getByBlockNumber(block.getBlockNumber());
                    if (comp == null || !comp.equals(block)) {


                        ps.setLong(1, block.getBlockNumber());
                        ps.setString(2, block.getBlockHash());
                        ps.setString(3, block.getMinerAddress());
                        ps.setString(4, block.getParentHash());
                        ps.setString(5, block.getReceiptTxRoot());
                        ps.setString(6, block.getStateRoot());
                        ps.setString(7, block.getTxTrieRoot());
                        ps.setString(8, block.getExtraData());
                        ps.setString(9, block.getNonce());
                        ps.setString(10, block.getBloom());
                        ps.setString(11, block.getSolution());
                        ps.setLong(12, block.getDifficulty());
                        ps.setLong(13, block.getTotalDifficulty());
                        ps.setLong(14, block.getNrgConsumed());
                        ps.setLong(15, block.getNrgLimit());
                        ps.setLong(16, block.getBlockSize());
                        ps.setLong(17, block.getBlockTimestamp());
                        ps.setLong(18, block.getNumTransactions());
                        ps.setLong(19, block.getBlockTime());
                        ps.setBigDecimal(20, (block.getNrgReward()));
                        ps.setDouble(21, block.getApproxNrgReward());
                        ps.setString(22, block.getLastTransactionHash());
                        ps.setString(23, block.getTransactionHashes());
                        ps.setInt(24, block.getBlockYear());
                        ps.setInt(25, block.getBlockMonth());
                        ps.setInt(26, block.getBlockDay());
                        ps.execute();
                    }

                }
                con.commit();
            }
            catch (SQLException e){
                con.rollback();
                throw e;
            }

        } catch (SQLException e) {
            return false;

        }
        return true;

    }

    @Override
    public Block getByBlockNumber(long blockNumber) throws SQLException {
        Block block;
        try (Connection con = DbConnectionPool.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(DbQuery.BlockGetByBlockNumber)) {
                ps.setLong(1, blockNumber);
                try (ResultSet rs = ps.executeQuery()) {
                    block = null;
                    while (rs.next()) {
                        block = new Block.BlockBuilder()
                                .blockNumber(rs.getLong("block_number"))
                                .blockHash(rs.getString("block_hash"))
                                .minerAddress(rs.getString("miner_address"))
                                .parentHash(rs.getString("parent_hash"))
                                .receiptTxRoot(rs.getString("receipt_tx_root"))
                                .stateRoot(rs.getString("state_root"))
                                .txTrieRoot(rs.getString("tx_trie_root"))
                                .extraData(rs.getString("extra_data"))
                                .nonce(rs.getString("nonce"))
                                .bloom(rs.getString("bloom"))
                                .solution(rs.getString("solution"))
                                .difficulty(rs.getLong("difficulty"))
                                .totalDifficulty(rs.getLong("total_difficulty"))
                                .nrgConsumed(rs.getLong("nrg_consumed"))
                                .nrgLimit(rs.getLong("nrg_limit"))
                                .blockSize(rs.getLong("block_size"))
                                .blockTimestamp(rs.getLong("block_timestamp"))
                                .numTransactions(rs.getLong("num_transactions"))
                                .blockTime(rs.getLong("block_time"))
                                .nrgReward(rs.getBigDecimal("nrg_reward"))
                                .transactionHash(rs.getString("transaction_hash"))
                                .transactionList(rs.getString("transaction_hashes"))
                                .approxNrgReward(rs.getDouble("approx_nrg_reward"))
                                .build();
                    }
                }


            }


        }

        return block;
    }

    @Override
    public boolean deleteFromAndUpdate(long blockNumber, List<TokenHolders> tokenHolders, List<Account> accounts) throws SQLException {

        //Use the try with resources to avoid memory leakage
        try (Connection con = DbConnectionPool.getConnection();
             PreparedStatement psDeleteToken = con.prepareStatement(DbQuery.TokenDeleteByBlock);
             PreparedStatement psDeleteBalance = con.prepareStatement(DbQuery.AccountDeleteFromBlock);
             PreparedStatement psDeleteTransaction = con.prepareStatement(DbQuery.TransactionDeleteByBlock);
             PreparedStatement psDeleteBlock = con.prepareStatement(DbQuery.BlocksDeleteFrom);
             PreparedStatement psDeleteContracts = con.prepareStatement(DbQuery.ContractDelete);
             PreparedStatement psDeleteEvents = con.prepareStatement(DbQuery.EventDelete);
             PreparedStatement psDeleteTransfers = con.prepareStatement(DbQuery.TokenTransfersDelete);
             PreparedStatement psDeleteTokenBalances = con.prepareStatement(DbQuery.TokenHoldersDeleteByBlockNumber);
             PreparedStatement psDeleteGraphing = con.prepareStatement(DbQuery.GraphingDelete);
             PreparedStatement psDeleteMetric = MetricsServiceImpl.getInstance().prepareDelete(con);
             PreparedStatement psInternalTransferService = InternalTransferServiceImpl.getInstance().prepareDelete(con, blockNumber)
        ) {


            PreparedStatement psUpdateParserState = null;
            List<PreparedStatement> statements = new ArrayList<>();
            try {

                psDeleteMetric.execute();

                psInternalTransferService.execute();
                psDeleteToken.setLong(1, blockNumber);
                psDeleteToken.execute();

                psDeleteBalance.setLong(1, blockNumber);
                psDeleteBalance.execute();

                psDeleteTransaction.setLong(1, blockNumber);
                psDeleteTransaction.execute();

                psDeleteBlock.setLong(1, blockNumber);
                psDeleteBlock.execute();

                //Delete Contracts

                psDeleteContracts.setLong(1, blockNumber);
                psDeleteContracts.execute();

                //Delete Events
                psDeleteEvents.setLong(1, blockNumber);
                psDeleteEvents.execute();

                //Token Balances
                psDeleteTokenBalances.setLong(1, blockNumber);
                psDeleteTokenBalances.execute();

                //----Transfers----

                psDeleteTransfers.setLong(1, blockNumber);
                psDeleteTransfers.execute();


                //---Parser State----
                ParserState.parserStateBuilder builder = new ParserState.parserStateBuilder();
                List<ParserState> statesToUpdate = new ArrayList<>();


                statesToUpdate.add(builder
                        .blockNumber(BigInteger.valueOf(blockNumber - 1))
                        .id(ParserStateServiceImpl.DB_ID).build());


                /*
                To perform a reorg in the graphing table firstly the record before the block to be deleted needs to be removed
                Secondly the parser state needs to be set to the new last record
                 */

                Graphing lastRecord = GraphingServiceImpl.getInstance().getLastRecord(blockNumber);//get prev graphing point


                //noinspection ConstantConditions
                if (lastRecord != null) {// if the last record is null the graphing table must be empty
                    Graphing newLastRecord = GraphingServiceImpl.getInstance().getLastRecord(lastRecord.getBlockNumber());// get the new head of the graphing table

                    //---Graphing----

                    psDeleteGraphing.setLong(1, lastRecord.getBlockNumber());
                    psDeleteGraphing.execute();

                    statesToUpdate.add(builder
                            .id(ParserStateServiceImpl.GRAPHING_ID)
                            .blockNumber(BigInteger.valueOf(newLastRecord == null ? 1 : newLastRecord.getBlockNumber()))
                            .build());// build the new head of the graphing table
                    //the default value of the parser state is 1

                }

                psUpdateParserState = ParserStateServiceImpl.getInstance().prepare(con, statesToUpdate);
                psUpdateParserState.executeBatch();


                //These are the accounts that must remain in the db after the reorg
                statements.add(AccountServiceImpl.getInstance().prepare(con, accounts));
                statements.add(TokenHoldersServiceImpl.getInstance().prepare(con, tokenHolders));

                for (var statement : statements) statement.executeBatch();

                con.commit();


            }
            catch (SQLException e) {


                try {
                    Objects.requireNonNull(con).rollback();
                } catch (SQLException | NullPointerException e1) {
                    GENERAL.debug("Through an exception while rolling back. ", e);
                }

                GENERAL.debug("Threw exception in deleteFrom: ", e);
                throw e;
            }
            finally {
                try {

                    if (psUpdateParserState != null) {
                        psUpdateParserState.close();
                    }


                    for (var statement : statements) statement.close();
                } catch (Exception e1) {
                    GENERAL.debug("Threw exception in deleteFrom: ", e1);
                }
            }
        }
        return true;
    }

    @Override
    public List<Long> blockHashIntegrity(long startNum) throws SQLException {
        List<Long> out=new ArrayList<Long>();
        Block block0=null;
        Block block1=null;
        long blockCount= ParserStateServiceImpl.getInstance().readDBState().getBlockNumber().longValue();
        try {
            while(blockCount>startNum) {
                block0 = getByBlockNumber(blockCount-1l);
                block1 = getByBlockNumber(blockCount);
                --blockCount;
                if(!block0.getBlockHash().equals(block1.getParentHash()))
                    out.add(blockCount);
            }
        } catch (NullPointerException|SQLException e) {
            GENERAL.debug("Threw Exception in block hash integrity check. " + "\n Blockcount = "+ blockCount+"\nException: ", e);

            throw e;
        }
        return out;
    }


    public Long getLastBlockNumber()  {
        Long out=0L;


        try (Connection con = DbConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(DbQuery.BlockGetMaxBlockNumber);
             ResultSet resultSet = ps.executeQuery()){

            while (resultSet.next()) {
                out =resultSet.getLong(1);
            }

        } catch (SQLException|NullPointerException e) {
            GENERAL.debug("Threw exception in getLastBlockNumber:", e);
        }
        return out;
    }

    @Override
    public PreparedStatement prepare(Connection con, List<Block> blocks) throws SQLException {
        PreparedStatement ps = con.prepareStatement(DbQuery.InsertBlock);

        for(Block block:blocks) {

            ps.setLong(1, block.getBlockNumber());
            ps.setString(2, block.getBlockHash());
            ps.setString(3, block.getMinerAddress());
            ps.setString(4, block.getParentHash());
            ps.setString(5, block.getReceiptTxRoot());
            ps.setString(6, block.getStateRoot());
            ps.setString(7, block.getTxTrieRoot());
            ps.setString(8, block.getExtraData());
            ps.setString(9, block.getNonce());
            ps.setString(10, block.getBloom());
            ps.setString(11, block.getSolution());
            ps.setLong(12, block.getDifficulty());
            ps.setLong(13, block.getTotalDifficulty());
            ps.setLong(14, block.getNrgConsumed());
            ps.setLong(15, block.getNrgLimit());
            ps.setLong(16, block.getBlockSize());
            ps.setLong(17, block.getBlockTimestamp());
            ps.setLong(18, block.getNumTransactions());
            ps.setLong(19, block.getBlockTime());
            ps.setBigDecimal(20, (block.getNrgReward()));
            ps.setDouble(21, block.getApproxNrgReward());
            ps.setString(22, block.getLastTransactionHash());
            ps.setString(23, block.getTransactionHashes());
            ps.setInt(24, block.getBlockYear());
            ps.setInt(25, block.getBlockMonth());
            ps.setInt(26, block.getBlockDay());
            ps.addBatch();
        }

        return ps;
    }

}