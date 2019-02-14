package aion.dashboard.tools;

import aion.dashboard.db.DbQuery;
import aion.dashboard.domainobjects.Block;
import aion.dashboard.domainobjects.ParserState;
import aion.dashboard.domainobjects.Transaction;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates records to be added to the DB
 */
public class WriteThread extends Thread {

    private final Logger logger;
    final long NUMBEROFBLKS;
    final long NUMBEROFTX;
    final long BatchSize;
    long lastBlkNum = 0;
    long lastTxNum = 0;
    long totalDifficulty = 0;
    final long NUMBEROFADDRESS = 100_000L;
    List<String> addresses = new ArrayList<>();
    final int WriteInterval;
    String lastBlockHash = "0x0000000000000000000000000000000000";
    List<String> blockHashes;
    List<String> transactionHashes;
    ParserState state;

    Connection connection;
    private Random random = new Random();


    private PreparedStatement updateBlocks;
    private PreparedStatement updateTransactions;
    private PreparedStatement updateParser;
    private PreparedStatement updateBlockMap;
    private PreparedStatement updateTransactionMap;

    volatile private boolean keepAlive;


    /**
     * Creates a new write thread and clears the database before the test begins
     *
     * @param BatchSize  Number of blocks that need to be written on each commit
     * @param NUMBEROFTX Number of transactions to be included in each block
     * @throws SQLException
     */


    public WriteThread(int WriteInterval, int BatchSize, int NUMBEROFTX, Connection conn) throws SQLException {
        super("Write Thread");
        logger = Logger.getLogger("Write Thread");
        state = new ParserState(0, 0L, 0L);
        blockHashes = new ArrayList<>();

        transactionHashes = new ArrayList<>();

        logger.log(Level.INFO, "Creating Addresses and clearing database");
        this.BatchSize = BatchSize;
        this.NUMBEROFBLKS = BatchSize / NUMBEROFTX;
        this.NUMBEROFTX = NUMBEROFTX;
        this.WriteInterval = WriteInterval;
        keepAlive = true;

        initializeAddress();


        connection = conn;

        updateBlockMap = connection.prepareStatement(DbQuery.INSERT_BLOCK_MAP);
        updateTransactionMap = connection.prepareStatement(DbQuery.INSERT_TRANSACTION_MAP);

        updateBlocks = connection.prepareStatement(DbQuery.INSERT_BLOCK);
        updateTransactions = connection.prepareStatement(DbQuery.TRANSACTION_INSERT);

        updateParser = connection.prepareStatement(DbQuery.UPDATE_PARSER_STATE);

        connection.setAutoCommit(false);


        logger.log(Level.INFO, "Construction complete");

    }


    /**
     *
     */
    @Override
    public void run() {
        super.run();

        logger.log(Level.INFO, "Starting Thread");
        while (keepAlive) {


            logger.log(Level.INFO, "Creating Data");
            double start = System.nanoTime();
            List<Block> blocks = createBlocks();
            List<List<Transaction>> transactions = new ArrayList<>();
            int i = 0;
            while (i < blocks.size()) {
                transactions.add(createTransactions(blocks.get(i)));
                i++;
            }
            double end = System.nanoTime();

            System.out.println(("Time to create data: " + (start - end) / 1_000_000F));

            i = 0;


            start = System.nanoTime();
            logger.log(Level.INFO, "Updating database");
            long lastBlockWritten = 0;
            long lastTrxWritten = 0;

            while (i < blocks.size() && keepAlive) {
                int j = 0;

                try {

                    updateBlockPreparedStatement(blocks.get(i));
                    lastBlockWritten = blocks.get(i).blockNumber;
                    /*
                     *
                     */
                    while (j < transactions.get(i).size()) {
                        lastTrxWritten = transactions.get(i).get(j).transactionIndex;
                        updateTransactionPreparedStatement(transactions.get(i).get(j));
                        j++;
                    }

                    i++;

                } catch (SQLException e) {

                    try {
                        connection.rollback();
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                }


            }

            try {
                updateParser.setLong(3, 1);
                updateParser.setLong(1, lastBlockWritten);
                updateParser.setLong(2, lastTrxWritten);
                updateParser.addBatch();

                updateParser.setLong(3, 2);
                updateParser.setLong(1, lastBlkNum);
                updateParser.setLong(2, -1);

                updateParser.addBatch();

                updateTransactionMap.executeBatch();
                updateBlockMap.executeBatch();
                updateBlocks.executeBatch();
                updateTransactions.executeBatch();
                updateParser.executeBatch();

                state.transactionId = lastTrxWritten;
                state.blockNumber = lastBlockWritten;


                connection.commit();
            } catch (SQLException e) {
                try {
                    connection.rollback();
                } catch (Exception e0) {
                    e0.printStackTrace();
                }
                e.printStackTrace();
            }


            end = System.nanoTime();
            System.out.println(("Time to edit data: " + (start - end) / 1_000_000F));


            try {

                Thread.sleep(1000 * WriteInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }


    private List<Block> createBlocks() {
        List<Block> blocks = new ArrayList<>();

        long i = 0;
        while (i <= NUMBEROFBLKS) {

            Block.BlockBuilder builder = new Block.BlockBuilder();
            String seed = DigestUtils.sha256Hex(RandomStringUtils.random(32));
            blockHashes.add(seed);
            builder.setBlockNumber(lastBlkNum + 1)
                    .setNumTransactions(NUMBEROFTX)
                    .setBloom(RandomStringUtils.randomAscii(256))
                    .setBlockHash(seed)
                    .setBlockTime(10L)
                    .setTotalDifficulty(Long.toString(totalDifficulty + 100, 16))
                    .setDifficulty(Long.toHexString(100))
                    .setBlockTimestamp(System.currentTimeMillis())
                    .setExtraData("")
                    .setMinerAddress(addresses.get(random.nextInt((int) NUMBEROFADDRESS)))
                    .setNonce(Long.toHexString(random.nextLong()))
                    .setNrgConsumed(50000L)
                    .setNrgLimit(50000L)
                    .setParentHash(lastBlockHash)
                    .setSize(random.nextLong())
                    .setStateRoot("")
                    .setTransactionList("[]")
                    .setTxTrieRoot("");


            lastBlkNum++;
            lastBlockHash = seed;
            totalDifficulty += 100;
            blocks.add(builder.build());
            i++;

        }
        return blocks;
    }


    private List<Transaction> createTransactions(Block block) {

        List<Transaction> transactions = new ArrayList<>();
        long i = 0L;
        Transaction.TransactionBuilder transactionBuilder = new Transaction.TransactionBuilder();


        while (i < NUMBEROFTX) {
            String seed = DigestUtils.sha256Hex(RandomStringUtils.random(32));
            transactionHashes.add(seed);
            transactionBuilder.setBlockNumber(block.blockNumber)
                    .setBlockHash(block.blockHash)
                    .setBlockTimestamp(block.blockTimestamp)
                    .setContractAddr(random.nextBoolean() ? "" : addresses.get(random.nextInt((int) NUMBEROFADDRESS)))
                    .setTransactionIndex(lastTxNum + 1)
                    .setFromAddr(addresses.get(random.nextInt((int) NUMBEROFADDRESS)))
                    .setToAddr(addresses.get(random.nextInt((int) NUMBEROFADDRESS)))
                    .setNrgConsumed(10L)
                    .setNrgPrice(10L)
                    .setTransactionTimestamp(System.currentTimeMillis())
                    .setValue("")
                    .setTransactionLog("[]")
                    .setData("")
                    .setNonce(Long.toHexString(random.nextLong()))
                    .setTxError("")
                    .setTransactionHash(seed)
                    .setId(lastTxNum);


            transactions.add(transactionBuilder.build());
            i++;
            lastTxNum++;
        }
        return transactions;
    }


    private void initializeAddress() {

        for (int i = 0; i < NUMBEROFADDRESS; i++) {
            addresses.add(DigestUtils.sha256Hex(RandomStringUtils.random(32)));
        }
    }


    private void updateBlockPreparedStatement(Block block) throws SQLException {

        long blockTime = block.getBlockTime();
        String bloom = block.getBloom();
        String difficulty = block.getDifficulty();
        String extraData = block.getExtraData();
        String blockHash = block.getBlockHash();
        String minerAddress = block.getMinerAddress();
        String blockNonce = block.getNonce();
        long blockNrgConsumed = block.getNrgConsumed();
        long nrgLimit = block.getNrgLimit();
        long blockNumber = block.getBlockNumber();
        String parentHash = block.getParentHash();
        String receiptTxRoot = block.getReceiptTxRoot();
        long blockSize = block.getSize();
        String solution = block.getSolution();
        String stateRoot = block.getStateRoot();
        long blockTimestamp = block.getBlockTimestamp();
        String totalDifficulty = block.getTotalDifficulty();
        String txTrieRoot = block.getTxTrieRoot();
        long numTransactions = block.getNumTransactions();
        String nrgReward = block.getNrgReward();
        Long transactionId = 0L;
        String blockTxList = block.getTransactionList();

        // save to block batch
        updateBlocks.setLong(1, blockNumber);
        updateBlocks.setString(2, blockHash);
        updateBlocks.setString(3, minerAddress);
        updateBlocks.setString(4, parentHash);
        updateBlocks.setString(5, receiptTxRoot);
        updateBlocks.setString(6, stateRoot);
        updateBlocks.setString(7, txTrieRoot);
        updateBlocks.setString(8, extraData);
        updateBlocks.setString(9, blockNonce);
        updateBlocks.setString(10, bloom);
        updateBlocks.setString(11, solution);
        updateBlocks.setString(12, difficulty);
        updateBlocks.setString(13, totalDifficulty);
        updateBlocks.setLong(14, blockNrgConsumed);
        updateBlocks.setLong(15, nrgLimit);
        updateBlocks.setLong(16, blockSize);
        updateBlocks.setLong(17, blockTimestamp);
        updateBlocks.setLong(18, numTransactions);
        updateBlocks.setLong(19, blockTime);
        updateBlocks.setString(20, nrgReward);
        updateBlocks.setLong(21, transactionId);
        updateBlocks.setString(22, blockTxList);
        updateBlocks.addBatch();

        // save to block map batch
        updateBlockMap.setString(1, blockHash);
        updateBlockMap.setLong(2, blockNumber);
        updateBlockMap.addBatch();

    }


    private void updateTransactionPreparedStatement(Transaction txDetails) throws SQLException {

        String contractAddress = txDetails.getContractAddr();
        String data = txDetails.getData();
        String error = txDetails.getTxError();
        String fromAddress = txDetails.getFromAddr();
        String toAddress = txDetails.getToAddr();
        String txNonce = txDetails.getNonce();
        long txNrgConsumed = txDetails.getNrgConsumed();
        long nrgPrice = txDetails.getNrgPrice();
        long txTimestamp = txDetails.getBlockTimestamp();
        String transactionHash = txDetails.getTransactionHash();
        long transactionIndex = txDetails.getTransactionIndex();
        String value = txDetails.getValue();
        Long transactionId = txDetails.getId();


        // save to transaction batch
        updateTransactions.setLong(1, transactionId);
        updateTransactions.setString(2, transactionHash);
        updateTransactions.setString(3, txDetails.blockHash);
        updateTransactions.setLong(4, txDetails.blockNumber);
        updateTransactions.setLong(5, transactionIndex);
        updateTransactions.setString(6, fromAddress);
        updateTransactions.setString(7, toAddress);
        updateTransactions.setLong(8, txNrgConsumed);
        updateTransactions.setLong(9, nrgPrice);
        updateTransactions.setLong(10, txTimestamp);
        updateTransactions.setLong(11, txDetails.getBlockTimestamp());
        updateTransactions.setString(12, value);
        updateTransactions.setString(13, txDetails.getTransactionLog());
        updateTransactions.setString(14, data);
        updateTransactions.setString(15, txNonce);
        updateTransactions.setString(16, error);
        updateTransactions.setString(17, contractAddress);
        updateTransactions.addBatch();

        // save to transaction map batch
        updateTransactionMap.setString(1, transactionHash);
        updateTransactionMap.setLong(2, transactionId);
        updateTransactionMap.addBatch();

    }


    public synchronized void kill() {
        keepAlive = false;
    }

}
