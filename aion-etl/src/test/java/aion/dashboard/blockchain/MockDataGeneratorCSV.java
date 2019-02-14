package aion.dashboard.blockchain;

import com.opencsv.CSVIterator;
import com.opencsv.CSVReader;
import org.aion.api.type.BlockDetails;
import org.aion.api.type.TxDetails;
import org.aion.base.type.Address;
import org.aion.base.type.Hash256;
import org.aion.base.util.ByteArrayWrapper;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/*
TODO replace parsers within methods to the parseLine method
 */

/**
 * This class requires data from the blockchain stored as a csv file.
 * Export data from the mainnet, test net or staging and store in the test_data/ChainData/ folder
 *
 * TODO add a test config to allow user to specify the location of the test data and file names
 */
public class MockDataGeneratorCSV implements MockDataGenerator{


    /**
     * Used to create some random garbage
     */
    private static Random random;

    /**
     *
     * @return  instance
     */
    public static MockDataGeneratorCSV of(BlockDataConfig blkConfig, TransactionDataConfig txConfig){
        if (blkConfig == null || txConfig == null) {
            throw new IllegalArgumentException("Configs must never be null");
        }
        else {
            return new MockDataGeneratorCSV(blkConfig,txConfig);
        }
    }


    /**
     * Static block used to perform logic before creation of static object
     */
    static {
        random = new Random();

    }

    /**
     * Class used to configure the transaction data creation
     */
    public static class TransactionDataConfig{
        private List<MockDataGenerator.TransactionField> transactionFields;
        public TransactionDataConfig(){transactionFields= new ArrayList<>();}
        public TransactionDataConfig add(MockDataGenerator.TransactionField field){
            transactionFields.add(field);
            return this;
        }
        public TransactionDataConfig addAll(List<MockDataGenerator.TransactionField> fields){
            transactionFields.addAll(fields);
            return this;
        }
        public void remove(MockDataGenerator.TransactionField field){
            transactionFields.remove(field);
        }
        public void clear(){
            transactionFields.clear();
        }


        public MockDataGenerator.TransactionField[] getConfig (){
            MockDataGenerator.TransactionField[] arr = new MockDataGenerator.TransactionField[transactionFields.size()];


            return transactionFields.toArray(arr);
        }
    }

    /**
     * Class used to configure the block data creation
     */
    public static class BlockDataConfig{
        private List<MockDataGenerator.BlockField> blockFields;
        public BlockDataConfig(){blockFields = new ArrayList<>();}

        public BlockDataConfig add(MockDataGenerator.BlockField field){
            blockFields.add(field);
            return this;
        }

        public BlockDataConfig addAll(List<MockDataGenerator.BlockField> fields){
            blockFields.addAll(fields);
            return this;
        }
        public void remove(MockDataGenerator.BlockField field){
            blockFields.remove(field);
        }
        public void clear(){
            blockFields.clear();
        }

        public MockDataGenerator.BlockField[] getConfig (){
            MockDataGenerator.BlockField[] arr = new MockDataGenerator.BlockField[blockFields.size()];


            return blockFields.toArray(arr);
        }
    }

    private BlockDataConfig blockDataConfig;
    private TransactionDataConfig transactionDataConfig;

    private MockDataGeneratorCSV(BlockDataConfig blkConfig, TransactionDataConfig txConfig){
        blockDataConfig = blkConfig;
        transactionDataConfig = txConfig;
    }


    public List<TxDetails> mockTransactionDetails() throws Exception{
        return mockTransactionDetails(transactionDataConfig.getConfig());
    }

    public List<BlockDetails> mockBlockDetails() throws Exception{
        return mockBlockDetails(mockTransactionDetails(),blockDataConfig.getConfig());
    }


    public List<BlockDetails> mockBlockDetailsF() throws Exception{
        return mockBlockDetailsFromMap(mockTransactionDetailsMap(transactionDataConfig.getConfig()),
                blockDataConfig.getConfig());
    }


    /**
     * Implementation details
     * - All value fields are configured to hold negative random values
     * - Address and Hash256 fields are configured to hold null values
     * - Any other objects are configured to hold null values
     * @param fieldsToEdit List of fields that need to be edited
     * @return
     */
    private  static List<TxDetails> mockTransactionDetails(MockDataGenerator.TransactionField[] fieldsToEdit)throws Exception{

        ArrayList<TxDetails> results = new ArrayList<>();
        TxDetails.TxDetailsBuilder txDetailsBuilder = new TxDetails.TxDetailsBuilder();


        System.out.println("RAN");
        FileReader fileReader = new FileReader(
                new File("test_data/ChainData/TransactionData.csv"));


        CSVIterator transactionIterator = new CSVIterator(new CSVReader(fileReader));
        transactionIterator.next();
        String[] transactionLines;

        while (transactionIterator.hasNext()){

            transactionLines = transactionIterator.next();


            parseTransactionLine(transactionLines,fieldsToEdit,txDetailsBuilder);


            Constructor<TxDetails> txDetailsConstructor = TxDetails.class
                    .getDeclaredConstructor(TxDetails.TxDetailsBuilder.class);//getting private constructor


            txDetailsConstructor.setAccessible(true);

            results.add(txDetailsConstructor.newInstance(txDetailsBuilder));//creating a transaction detail in an
            //inconsistent state and add it it to the list

            if (Arrays.stream(fieldsToEdit).parallel().anyMatch( e -> e == MockDataGenerator.TransactionField.DUPLICATE)){
                results.add(txDetailsConstructor.newInstance(txDetailsBuilder));//creating a transaction detail in an

            }
        }


        return results;
    }//MockTransactionDetails

    /**
     * Implementation details
     * - All value fields are configured to hold negative random values
     * - Address and Hash256 fields are configured to hold null values
     * - Any other objects are configured to hold null values
     * @param fieldsToEdit The set of block fields to convert to garbage
     * @return
     */
    private  static List<BlockDetails> mockBlockDetails(List<TxDetails> txDetails ,MockDataGenerator.BlockField[] fieldsToEdit) throws Exception{
        List<BlockDetails> results = new ArrayList<>();
            FileReader fileReader = new FileReader(new File("test_data/ChainData/BlocksData.csv"));
            CSVIterator blockIterator = new CSVIterator(new CSVReader(fileReader));
            blockIterator.next();
            BlockDetails.BlockDetailsBuilder builder = new BlockDetails.BlockDetailsBuilder();


            while (blockIterator.hasNext()){
                final String[] blockLine = blockIterator.next();

                parseBlockLine(blockLine,fieldsToEdit,builder);
                if (Arrays.stream(fieldsToEdit).parallel().anyMatch(e-> e == MockDataGenerator.BlockField.TX_DETAILS)){
                    builder.txDetails(null);
                }
                else {
                    //Get all transactions that are associated with this block.
                    List<TxDetails> subTxDetails = txDetails
                            .stream()
                            .parallel()
                            .filter( tx -> blockLine[MockDataGenerator.BlockField.TX_DETAILS.getIndex()]
                                    .contains(tx.getTxHash().toString()) )
                            .collect(Collectors.toList());
                    builder.txDetails(subTxDetails);
                }

                //Forcing instantiation of blkDetails in a bad state
                Constructor<BlockDetails> blockDetailsConstructor = BlockDetails.class.getDeclaredConstructor(BlockDetails.BlockDetailsBuilder.class);
                blockDetailsConstructor.setAccessible(true);
                results.add(blockDetailsConstructor.newInstance(builder));

                if (Arrays.stream(fieldsToEdit).parallel().anyMatch( e -> e == MockDataGenerator.BlockField.DUPLICATE)){
                    results.add(blockDetailsConstructor.newInstance(builder));
                }
            }



        return results;
    }//MockBlockDetails

    private static String genRandomString(int l){
        return RandomStringUtils.randomAlphanumeric(l);
    }


    /**
     * Stores transactions in a map to make retrieval easier
     * Similiar to mockTransactionDetails
     * @param fieldsToEdit
     * @return
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IOException
     */
    private static Map<Long, List<TxDetails>> mockTransactionDetailsMap(MockDataGenerator.TransactionField[] fieldsToEdit) throws Exception{
        HashMap<Long,List<TxDetails>> results = new HashMap<>();



         TxDetails.TxDetailsBuilder txDetailsBuilder = new TxDetails.TxDetailsBuilder();


        System.out.println("RAN");
        FileReader fileReader = new FileReader(
                new File("test_data/ChainData/TransactionData.csv"));


        CSVIterator transactionIterator = new CSVIterator(new CSVReader(fileReader));
        transactionIterator.next();
        String[] transactionLines;

        while (transactionIterator.hasNext()){

            transactionLines = transactionIterator.next();


            parseTransactionLine(transactionLines,fieldsToEdit,txDetailsBuilder);


            Constructor<TxDetails> txDetailsConstructor = TxDetails.class
                    .getDeclaredConstructor(TxDetails.TxDetailsBuilder.class);//getting private constructor


            txDetailsConstructor.setAccessible(true);


            TxDetails txDetails = txDetailsConstructor.newInstance(txDetailsBuilder);


            if (results.containsKey(Long.parseLong(transactionLines[MockDataGenerator.TransactionField.BLK_NUMBER.getIndex()]))){
                results.get(Long.parseLong(transactionLines[MockDataGenerator.TransactionField.BLK_NUMBER.getIndex()])).add(txDetails);
            }
            else {
                List<TxDetails> temp = new ArrayList<>();

                temp.add(txDetails);
                results.put(Long.parseLong(transactionLines[MockDataGenerator.TransactionField.BLK_NUMBER.getIndex()]), temp);
            }

        }




        return results;

    }//mockTransactionDetailsMap


    private static List<BlockDetails> mockBlockDetailsFromMap(Map<Long, List<TxDetails>> txMap,
                                                              MockDataGenerator.BlockField[] fieldsToEdit)throws Exception{
        List<BlockDetails> results = new ArrayList<>();
        FileReader fileReader = new FileReader(new File("test_data/ChainData/BlocksData.csv"));
        CSVIterator blockIterator = new CSVIterator(new CSVReader(fileReader));
        blockIterator.next();
        BlockDetails.BlockDetailsBuilder builder = new BlockDetails.BlockDetailsBuilder();


        while (blockIterator.hasNext()){
            final String[] blockLine = blockIterator.next();
            parseBlockLine(blockLine,fieldsToEdit,builder);

            if (Arrays.stream(fieldsToEdit).parallel().anyMatch(e-> e == MockDataGenerator.BlockField.TX_DETAILS)){
                builder.txDetails(null);
            }
            else {
                //Get all transactions that are associated with this block.

                List<TxDetails> subTxDetails;
                subTxDetails = txMap.getOrDefault(Long.parseLong(blockLine[MockDataGenerator.BlockField.BLK_NUMBER.getIndex()]), new ArrayList<>());
                builder.txDetails(subTxDetails);
            }

            //Forcing instantiation of blkDetails in a bad state
            Constructor<BlockDetails> blockDetailsConstructor = BlockDetails.class.getDeclaredConstructor(BlockDetails.BlockDetailsBuilder.class);
            blockDetailsConstructor.setAccessible(true);
            results.add(blockDetailsConstructor.newInstance(builder));

            if (Arrays.stream(fieldsToEdit).parallel().anyMatch( e -> e == MockDataGenerator.BlockField.DUPLICATE)){
                results.add(blockDetailsConstructor.newInstance(builder));
            }
        }



        return results;

    }


    /**
     * Forces the instantiation of invalid address, ie. an address that contains null.
     * @return An address object in an inconsistent state
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     */
    private static Address genInvalidAddress() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method addressMethod = Address.class.getDeclaredMethod("setupData", byte[].class);
        addressMethod.setAccessible(true);
        Address address = new Address("");
        addressMethod.invoke(address, (Address)null);

        return address;
    }

    /**
     * Forces the instantiation of an invalid Hash256 object, ie. a has that contains null
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     */
    private static Hash256 genInvalidHash256() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method hash256Method = Hash256.class.getDeclaredMethod("setupData", byte[].class);
        hash256Method.setAccessible(true);
        Hash256 hash256 = new Hash256(new byte[32]);
        hash256Method.invoke(hash256,(Hash256) null);

        return hash256;
    }


    /**
     * Helper method
     *
     * Parses and edits each line in the blocksData csv
     * @param blockLine
     * @param fieldsToEdit
     * @param builder
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    private static void parseBlockLine(String[] blockLine,
                                MockDataGenerator.BlockField[] fieldsToEdit,
                                BlockDetails.BlockDetailsBuilder builder) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        if (Arrays.stream(fieldsToEdit).parallel().anyMatch(e-> e == MockDataGenerator.BlockField.BLK_NUMBER)){
            builder.number(-1*random.nextLong());
        }
        else {
            builder.number(Long.parseLong(blockLine[MockDataGenerator.BlockField.BLK_NUMBER.getIndex()]));
        }

        if (Arrays.stream(fieldsToEdit).parallel().anyMatch(e->e== MockDataGenerator.BlockField.TIMESTAMP)){
            builder.timestamp(-1*random.nextLong());
        }
        else {
            builder.timestamp(Long.parseLong(blockLine[MockDataGenerator.BlockField.TIMESTAMP.getIndex()]));
        }

        if (Arrays.stream(fieldsToEdit).parallel().anyMatch(e->e== MockDataGenerator.BlockField.NRG_CONSUMED)){
            builder.nrgConsumed(-1*random.nextLong());
        }
        else {
            builder.nrgConsumed(Long.parseLong(blockLine[MockDataGenerator.BlockField.NRG_CONSUMED.getIndex()]));
        }

        if(Arrays.stream(fieldsToEdit).parallel().anyMatch(e-> e== MockDataGenerator.BlockField.NRG_LIMIT)){
            builder.nrgLimit(-1*random.nextLong());
        }
        else {
            builder.nrgLimit(Long.parseLong(blockLine[MockDataGenerator.BlockField.NRG_LIMIT.getIndex()]));
        }

        if (Arrays.stream(fieldsToEdit).parallel().anyMatch(e->e== MockDataGenerator.BlockField.BLOOM)){

            builder.bloom(null);
        }
        else {
            builder.bloom(new ByteArrayWrapper(blockLine[MockDataGenerator.BlockField.BLOOM.getIndex()].getBytes()));
        }

        if (Arrays.stream(fieldsToEdit).parallel().anyMatch(e->e== MockDataGenerator.BlockField.EXTRA_DATA)){
            builder.extraData(null);
        }
        else {
            builder.extraData(new ByteArrayWrapper(new byte[]{}));
        }

        if (Arrays.stream(fieldsToEdit).parallel().anyMatch(e->e== MockDataGenerator.BlockField.SOLUTION)){
            builder.solution(null);
        }
        else{
            builder.solution(new ByteArrayWrapper(blockLine[MockDataGenerator.BlockField.SOLUTION.getIndex()].getBytes()));
        }

        if (Arrays.stream(fieldsToEdit).parallel().anyMatch(e->e== MockDataGenerator.BlockField.HASH)){
            builder.hash(genInvalidHash256());
        }
        else{
            builder.hash(new Hash256(blockLine[MockDataGenerator.BlockField.HASH.getIndex()]));
        }

        if (Arrays.stream(fieldsToEdit).parallel().anyMatch(e-> e == MockDataGenerator.BlockField.PARENT_HASH)){
            builder.parentHash(genInvalidHash256());
        }
        else {
            builder.parentHash(new Hash256(blockLine[MockDataGenerator.BlockField.PARENT_HASH.getIndex()]));
        }

        if (Arrays.stream(fieldsToEdit).parallel().anyMatch(e -> e == MockDataGenerator.BlockField.NONCE)){
            builder.nonce(new BigInteger(64, random).negate());
        }
        else {
            builder.nonce(new BigInteger(blockLine[MockDataGenerator.BlockField.NONCE.getIndex()],16));
        }

        if (Arrays.stream(fieldsToEdit).parallel().anyMatch(e-> e== MockDataGenerator.BlockField.DIFFICULTY)){
            builder.difficulty(new BigInteger(64, random).negate());
        }
        else{
            builder.difficulty(new BigInteger(blockLine[MockDataGenerator.BlockField.DIFFICULTY.getIndex()]));
        }

        if (Arrays.stream(fieldsToEdit).parallel().anyMatch(e -> e == MockDataGenerator.BlockField.TOTAL_DIFFICULTY)){
            builder.totalDifficulty(new BigInteger(64, random).negate());
        }
        else {
            builder.totalDifficulty(new BigInteger(blockLine[MockDataGenerator.BlockField.TOTAL_DIFFICULTY.getIndex()],16));
        }

        if (Arrays.stream(fieldsToEdit).parallel().anyMatch(e -> e == MockDataGenerator.BlockField.MINER_ADDRESS)){
            builder.miner(genInvalidAddress());
        }
        else {
            builder.miner(new Address(blockLine[MockDataGenerator.BlockField.MINER_ADDRESS.getIndex()]));
        }

        if (Arrays.stream(fieldsToEdit).parallel().anyMatch(e -> e == MockDataGenerator.BlockField.STATE_ROOT)){
            builder.stateRoot(null);
        }
        else {
            builder.stateRoot(new Hash256(blockLine[MockDataGenerator.BlockField.STATE_ROOT.getIndex()]));
        }

        if(Arrays.stream(fieldsToEdit).parallel().anyMatch(e-> e== MockDataGenerator.BlockField.TX_TRIE_ROOT)){
            builder.txTrieRoot(null);
        }
        else {
            builder.txTrieRoot(new Hash256(blockLine[MockDataGenerator.BlockField.TX_TRIE_ROOT.getIndex()]));
        }

        if(Arrays.stream(fieldsToEdit).parallel().anyMatch(e-> e== MockDataGenerator.BlockField.RECEIPT_TX_ROOT)){
            builder.receiptTxRoot(null);
        }
        else{
            builder.receiptTxRoot(new Hash256(blockLine[MockDataGenerator.BlockField.RECEIPT_TX_ROOT.getIndex()]));
        }

        if(Arrays.stream(fieldsToEdit).parallel().anyMatch(e -> e == MockDataGenerator.BlockField.SIZE)){
            builder.size(-1 * random.nextInt());
        }
        else{
            builder.size(Integer.parseInt(blockLine[MockDataGenerator.BlockField.SIZE.getIndex()]));
        }

        if (Arrays.stream(fieldsToEdit).parallel().anyMatch(e -> e == MockDataGenerator.BlockField.BLOCK_TIME)){
            builder.blockTime(-1 * random.nextLong());
        }
        else {
            builder.blockTime(Integer.parseInt(blockLine[MockDataGenerator.BlockField.BLOCK_TIME.getIndex()]));
        }


        return;
    }//parseBlockLine

    /**
     * Helper method parses and edits each line in the transaction csv
     * @param transactionLines
     * @param fieldsToEdit
     * @param txDetailsBuilder
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    private static void parseTransactionLine(String[] transactionLines,
                                      MockDataGenerator.TransactionField[] fieldsToEdit,
                                      TxDetails.TxDetailsBuilder txDetailsBuilder) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        if (Arrays.stream(fieldsToEdit).parallel().anyMatch(e-> e == MockDataGenerator.TransactionField.FROM)){
            txDetailsBuilder.from(genInvalidAddress());
        }
        else {
            txDetailsBuilder.from(new Address(transactionLines[MockDataGenerator.TransactionField.FROM.getIndex()]));
        }


        if (Arrays.stream(fieldsToEdit).parallel().anyMatch(e->e == MockDataGenerator.TransactionField.TO)){
            txDetailsBuilder.to(genInvalidAddress());
        }
        else {
            txDetailsBuilder.to(new Address(transactionLines[MockDataGenerator.TransactionField.TO.getIndex()]));
        }

        if (Arrays.stream(fieldsToEdit).parallel().anyMatch(e-> e == MockDataGenerator.TransactionField.TX_HASH)){
            txDetailsBuilder.txHash(genInvalidHash256());
        }
        else {
            txDetailsBuilder.txHash(new Hash256(transactionLines[MockDataGenerator.TransactionField.TX_HASH.getIndex()]));
        }

        if (Arrays.stream(fieldsToEdit).parallel().anyMatch(e-> e== MockDataGenerator.TransactionField.CONTRACT)){
            txDetailsBuilder.contract(genInvalidAddress());
        }
        else{
            txDetailsBuilder.contract(new Address(transactionLines[MockDataGenerator.TransactionField.CONTRACT.getIndex()]));
        }

        if (Arrays.stream(fieldsToEdit).parallel().anyMatch(e-> e== MockDataGenerator.TransactionField.VALUE )){
            txDetailsBuilder.value(new BigInteger(64, random).negate());

        }
        else {
            txDetailsBuilder.value(new BigInteger(transactionLines[MockDataGenerator.TransactionField.VALUE.getIndex()],16));
        }

        if (Arrays.stream(fieldsToEdit).parallel().anyMatch(e-> e == MockDataGenerator.TransactionField.NONCE)){
            txDetailsBuilder.nonce(new BigInteger(64,random).negate());
        }
        else{
            txDetailsBuilder.nonce(new BigInteger(transactionLines[MockDataGenerator.TransactionField.NONCE.getIndex()],16));
        }


        if(Arrays.stream(fieldsToEdit).parallel().anyMatch( e -> e == MockDataGenerator.TransactionField.DATA)){
            txDetailsBuilder.data(null);
        }
        else {
            txDetailsBuilder.data(new ByteArrayWrapper(transactionLines[MockDataGenerator.TransactionField.DATA.getIndex()].getBytes()));
        }


        if (Arrays.stream(fieldsToEdit).parallel().anyMatch(e-> e == MockDataGenerator.TransactionField.NRG_CONSUMED)){
            txDetailsBuilder.nrgConsumed(-1*random.nextLong());
        }
        else {
            txDetailsBuilder.nrgConsumed(Long.parseLong(transactionLines[MockDataGenerator.TransactionField.NRG_CONSUMED.getIndex()]));
        }

        if (Arrays.stream(fieldsToEdit).parallel().anyMatch(e-> e== MockDataGenerator.TransactionField.NRG_PRICE)){
            txDetailsBuilder.nrgPrice(-1*random.nextLong());
        }
        else {
            txDetailsBuilder.nrgPrice(Long.parseLong(transactionLines[MockDataGenerator.TransactionField.NRG_PRICE.getIndex()]));
        }

        if (Arrays.stream(fieldsToEdit).parallel().anyMatch(e-> e== MockDataGenerator.TransactionField.TX_INDEX)){
            txDetailsBuilder.txIndex(-1*random.nextInt());
        }
        else {
            txDetailsBuilder.txIndex(Integer.parseInt(transactionLines[MockDataGenerator.TransactionField.TX_INDEX.getIndex()]));
        }

        if (Arrays.stream(fieldsToEdit).parallel().anyMatch(e->e== MockDataGenerator.TransactionField.TIMESTAMP)){
            txDetailsBuilder.timestamp(-1*random.nextLong());
        }
        else {
            txDetailsBuilder.timestamp(Long.parseLong(transactionLines[MockDataGenerator.TransactionField.TIMESTAMP.getIndex()]));
        }

        if (Arrays.stream(fieldsToEdit).parallel().anyMatch(e->e== MockDataGenerator.TransactionField.LOGS)){
            txDetailsBuilder.logs(null);
        }
        else {
            txDetailsBuilder.logs(new ArrayList<>());
        }

        if(Arrays.stream(fieldsToEdit).parallel().anyMatch(e -> e == MockDataGenerator.TransactionField.ERROR)){
            txDetailsBuilder.error(null);
        }
        else {
            txDetailsBuilder.error(transactionLines[MockDataGenerator.TransactionField.ERROR.getIndex()]);
        }



        return;
    } //parseTransactionLine

}
