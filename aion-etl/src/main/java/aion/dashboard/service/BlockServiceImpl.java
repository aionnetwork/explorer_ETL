package aion.dashboard.service;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.db.DbQuery;
import aion.dashboard.domainobject.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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
        return save(Collections.singletonList(block));
    }

    @Override
    public boolean save(List<Block> blocks) {



        try(Connection  con = DbConnectionPool.getConnection();
            var ps = con.prepareStatement(DbQuery.INSERT_BLOCK);
            var psMap = con.prepareStatement(DbQuery.INSERT_BLOCK_MAP)) {

            try{

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
                        ps.setString(12, block.getDifficulty());
                        ps.setString(13, block.getTotalDifficulty());
                        ps.setLong(14, block.getNrgConsumed());
                        ps.setLong(15, block.getNrgLimit());
                        ps.setLong(16, block.getSize());
                        ps.setLong(17, block.getBlockTimestamp());
                        ps.setLong(18, block.getNumTransactions());
                        ps.setLong(19, block.getBlockTime());
                        ps.setString(20, block.getNrgReward().toString(16));
                        ps.setLong(21, block.getTransactionId().longValue());
                        ps.setString(22, block.getTransactionList());

                        psMap.setString(1, block.getBlockHash());
                        psMap.setLong(2, block.getBlockNumber());
                        psMap.execute();
                        ps.execute();

                    }

                }
                con.commit();
            }
            catch (SQLException e){
                con.rollback();
                throw e;
            }
            return true;
        } catch (SQLException e) {
            return false;

        }

    }

    @Override
    public Block getByBlockNumber(long blockNumber) throws SQLException {
        Block block;
        try (Connection con = DbConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(DbQuery.BLOCKGET_BY_BLOCK_NUMBER)) {
            ps.setLong(1, blockNumber);
            try (ResultSet rs = ps.executeQuery()) {
                block = null;
                while (rs.next()) {
                    block = new Block.BlockBuilder().blockNumber(rs.getLong("block_number")).
                            blockHash(rs.getString("block_hash")).
                            minerAddress(rs.getString("miner_address")).
                            parentHash(rs.getString("parent_hash")).
                            receiptTxRoot(rs.getString("receipt_tx_root")).
                            stateRoot(rs.getString("state_root")).
                            txTrieRoot(rs.getString("tx_trie_root")).
                            extraData(rs.getString("extra_data")).
                            nonce(rs.getString("nonce")).
                            bloom(rs.getString("bloom")).
                            solution(rs.getString("solution")).
                            difficulty(rs.getString("difficulty")).
                            totalDifficulty(rs.getString("total_difficulty")).
                            nrgConsumed(rs.getLong("nrg_consumed")).
                            nrgLimit(rs.getLong("nrg_limit")).
                            size(rs.getLong("size")).
                            blockTimestamp(rs.getLong("block_timestamp")).
                            numTransactions(rs.getLong("num_transactions")).
                            blockTime(rs.getLong("block_time")).
                            nrgReward(new BigInteger(rs.getString("nrg_reward"), 16)).
                            transactionId(new BigInteger(rs.getString("transaction_id"))).
                            transactionList(rs.getString("transaction_list")).build();
                }
            }


        }

        return block;
    }

    @Override
    public boolean deleteFromAndUpdate(long blockNumber, List<TokenBalance> tokenBalances, List<Balance> balances) throws SQLException {

        //Use the try with resources to avoid memory leakage
        try (Connection con = DbConnectionPool.getConnection();
             PreparedStatement psDeleteToke = con.prepareStatement(DbQuery.TOKEN_DELETE_BY_BLOCK);
             PreparedStatement psDeleteBalance = con.prepareStatement(DbQuery.BALANCE_DELETE_FROM_BLOCK);
             PreparedStatement psDeleteBlockMap = con.prepareStatement(DbQuery.BLOCK_MAP_DELETE_BY_BLOCK);
             PreparedStatement psDeleteTransactionMap = con.prepareStatement(DbQuery.TRANSACTION_MAP_DELETE_BY_ID);
             PreparedStatement psDeleteTransaction = con.prepareStatement(DbQuery.TRANSACTION_DELETE_BY_ID);
             PreparedStatement psDeleteBlock = con.prepareStatement(DbQuery.BLOCKS_DELETE_FROM);
             PreparedStatement psDeleteContracts = con.prepareStatement(DbQuery.CONTRACT_DELETE);
             PreparedStatement psDeleteEvents = con.prepareStatement(DbQuery.EVENT_DELETE);
             PreparedStatement psDeleteTransfers = con.prepareStatement(DbQuery.TRANSFER_DELETE);
             PreparedStatement psDeleteTokenBalances = con.prepareStatement(DbQuery.TOKEN_BALANCE_DELETE_BY_BLOCK_NUMBER);
             PreparedStatement psDeleteGraphing = con.prepareStatement(DbQuery.GRAPHING_DELETE)
        ) {


            PreparedStatement psUpdateParserState = null;
            List<PreparedStatement> statements = new ArrayList<>();
            try {

                Block lastBlock = getByBlockNumber(blockNumber -1);

                psDeleteToke.setLong(1, blockNumber);
                psDeleteToke.execute();

                psDeleteBalance.setLong(1, blockNumber);
                psDeleteBalance.execute();

                psDeleteBlockMap.setLong(1, blockNumber);
                psDeleteBlockMap.execute();


                psDeleteTransactionMap.setLong(1, lastBlock.getTransactionId().longValue());
                psDeleteTransactionMap.execute();


                psDeleteTransaction.setLong(1, lastBlock.getTransactionId().longValue());
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
                ParserState.ParserStateBuilder builder = new ParserState.ParserStateBuilder();
                List<ParserState> statesToUpdate = new ArrayList<>();


                statesToUpdate.add(builder.blockNumber(BigInteger.valueOf(blockNumber - 1)).transactionID(getByBlockNumber(blockNumber - 1).getTransactionId()).id(ParserStateServiceImpl.DB_ID).build());
                statesToUpdate.add(builder.transactionID(BigInteger.valueOf(-1)).id(ParserStateServiceImpl.BLKCHAIN_ID).build());
                statesToUpdate.add(builder.transactionID(BigInteger.valueOf(-1)).id(ParserStateServiceImpl.INTEGRITY_ID).build());


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
                            .transactionID(BigInteger.valueOf(-1)).build());// build the new head of the graphing table
                    //the default value of the parser state is 1

                }

                psUpdateParserState = ParserStateServiceImpl.getInstance().prepare(con, statesToUpdate);
                psUpdateParserState.executeBatch();


                //These are the balances that must remain in the db after the reorg
                statements.add(BalanceServiceImpl.getInstance().prepare(con, balances));
                statements.add(TokenBalanceServiceImpl.getInstance().prepare(con, tokenBalances));

                for (var statement : statements) statement.executeBatch();

                con.commit();


            }
            catch (SQLException e) {

                GENERAL.debug("Threw exception in deleteFrom: ", e);

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


    public Long getMaxTransactionIdForBlock(long blockNumber) throws SQLException {
        long out = -1L;
        try(Connection connection = DbConnectionPool.getConnection();
            PreparedStatement ps = connection.prepareStatement(DbQuery.MAX_TRANSACTION_ID_FOR_BLOCK_NUMBER)){
            ps.setLong(1,blockNumber);
            try(ResultSet rs =ps.executeQuery()){

                while (rs.next()){
                    out = rs.getLong(1);
                }


            }
            return out;
        } catch (SQLException e) {
            GENERAL.debug("Threw Exception while getting max transaction id");
            throw e;
        }

    }

    @Override
    public PreparedStatement[] prepare(Connection con, List<Block> blocks) throws SQLException {
        PreparedStatement ps = con.prepareStatement(DbQuery.INSERT_BLOCK);
        PreparedStatement psMap = con.prepareStatement(DbQuery.INSERT_BLOCK_MAP);

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
            ps.setString(12, block.getDifficulty());
            ps.setString(13, block.getTotalDifficulty());
            ps.setLong(14, block.getNrgConsumed());
            ps.setLong(15, block.getNrgLimit());
            ps.setLong(16, block.getSize());
            ps.setLong(17, block.getBlockTimestamp());
            ps.setLong(18, block.getNumTransactions());
            ps.setLong(19, block.getBlockTime());
            ps.setString(20, block.getNrgReward().toString(16));
            ps.setLong(21, block.getTransactionId().longValue());
            ps.setString(22, block.getTransactionList());

            psMap.setString(1, block.getBlockHash());
            psMap.setLong(2, block.getBlockNumber());
            psMap.addBatch();
            ps.addBatch();

        }

        return new PreparedStatement[]{ps,psMap};
    }

}
