package aion.dashboard.blockchain;

import org.aion.api.IAdmin;
import org.aion.api.type.ApiMsg;
import org.aion.api.type.BlockDetails;
import org.aion.base.type.Address;
import org.aion.base.type.Hash256;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class MockAdmin implements IAdmin {

    private AdminMode mode;
    private final Random random;
    List<BlockDetails> blkDetails;
    private final int rateOfDrops;
    private final int rateOfSwaps;
    private final int numberToRemove;//Number of items to remove from head or tail
    private final MockDataGenerator generator;


    /**
     *
     * @param mode
     * @param rateOfDrops
     * @param rateOfSwaps
     * @param numberToRemove
     * @param generator
     * @throws Exception
     */
    MockAdmin(AdminMode mode, int rateOfDrops, int rateOfSwaps, int numberToRemove, MockDataGenerator generator) throws Exception {
        this.mode = mode;
        this.random = new Random();
        this.rateOfDrops = rateOfDrops;
        this.rateOfSwaps = rateOfSwaps;
        this.numberToRemove = numberToRemove;
        setUp(generator);
        this.generator=generator;

        System.out.println(blkDetails.size());
    }

    private void setUp(MockDataGenerator generator) throws Exception {
        if (blkDetails == null){
            blkDetails = generator.mockBlockDetailsF();
        }
        System.out.println(blkDetails.size());

    }
    public void update() throws Exception {
        blkDetails.addAll(generator.mockBlockDetailsF());
    }

    @Override
    public ApiMsg getBlockDetailsByNumber(String s) {
        throw new UnsupportedOperationException("Call to unsupported method in mocking class");
    }

    @Override
    public ApiMsg getBlockDetailsByNumber(long l) {
        throw new UnsupportedOperationException("Call to unsupported method in mocking class");
    }

    @Override
    public ApiMsg getBlockDetailsByHash(Hash256 hash256) {
        throw new UnsupportedOperationException("Call to unsupported method in mocking class");
    }

    @Override
    public ApiMsg getBlockDetailsByNumber(List<Long> list) {
        throw new UnsupportedOperationException("Call to unsupported method in mocking class");
    }

    @Override
    public ApiMsg getBlockSqlByRange(Long aLong, Long aLong1) {
        throw new UnsupportedOperationException("Call to unsupported method in mocking class");
    }

    /**
     *
     * @param start the start of the range to be returned
     * @param end the end of the range to be returned
     * @return An apimsg containing the requested information
     */

    @Override
    public ApiMsg getBlockDetailsByRange(Long start, Long end) {

        switch (mode){
            case UNSORTED:
                return createUnsortedList(start, end) ;
            case INCOMPLETE:
                return createIncompleteList(start, end);
            case DISCONTINUOUS:
                return createDiscontinuousList(start, end);
            case VALID:
                return createValidList(start, end);
            default:
                return null;
        }

    }

    @Override
    public ApiMsg getBlockDetailsByLatest(Long aLong) {
        throw new UnsupportedOperationException("Call to unsupported method in mocking class");
    }

    @Override
    public ApiMsg getBlocksByLatest(Long aLong) {
        throw new UnsupportedOperationException("Call to unsupported method in mocking class");
    }

    @Override
    public ApiMsg getAccountDetailsByAddressList(String s) {
        throw new UnsupportedOperationException("Call to unsupported method in mocking class");
    }

    @Override
    public ApiMsg getAccountDetailsByAddressList(List<Address> list) {
        throw new UnsupportedOperationException("Call to unsupported method in mocking class");
    }

    /**
     * Creates list and randomly switches elements
     * @param start
     * @param end
     * @return
     */

    private ApiMsg createUnsortedList(long start, long end){
        List<BlockDetails> results = blkDetails
                .parallelStream()
                .filter(e-> e.getNumber() >= start && e.getNumber() <= end)
                .collect(Collectors.toList());

        for (int i = 0; i<results.size(); i++){
            if (random.nextInt(rateOfSwaps) % rateOfSwaps == 0){// switches blocks randomly 1/rateOfSwaps of the time
                BlockDetails tempBlk = results.get(i);
                int newIndex = random.nextInt(results.size());
                results.set(i,results.get(newIndex));
                results.set(newIndex, tempBlk);
            }
        }
        return new ApiMsg(results, ApiMsg.cast.OTHERS) ;
    }

    /**
     * Randomly drops elements from the list
     * @param start
     * @param end
     * @return
     */
    private ApiMsg createDiscontinuousList(long start, long end){
        List<BlockDetails> results = blkDetails
                .parallelStream()
                .filter(e -> e.getNumber() >= start
                        && e.getNumber() <= end
                        && random.nextInt(rateOfDrops) % rateOfDrops == 0)// drops 1/rateOfDrops of the items in the list
                .collect(Collectors.toList());

        return new ApiMsg(results, ApiMsg.cast.OTHERS);
    }

    /**
     * Randomly removes an element from the start or the end of the list
     * @param start
     * @param end
     * @return
     */
    private ApiMsg createIncompleteList(long start, long end){

        boolean dropFromFront = random.nextBoolean();
        boolean dropFromTail = !dropFromFront;

        final long actualStart = dropFromFront ? start + numberToRemove : start;
        final long actualEnd = dropFromTail ? end - numberToRemove : end;
        List<BlockDetails> results = blkDetails
                .parallelStream()
                .filter(e-> e.getNumber() >= actualStart && e.getNumber() <= actualEnd)
                .collect(Collectors.toList());


        return new ApiMsg(results, ApiMsg.cast.OTHERS);
    }


    private ApiMsg createValidList(long start, long end){

        List<BlockDetails> results = blkDetails
                .parallelStream()
                .filter(e-> e.getNumber() >= start && e.getNumber() <= end)
                .collect(Collectors.toList());


         return new ApiMsg(results, ApiMsg.cast.OTHERS);

    }


    /**
     * Used to specify how the result should be mutated
     */
    public enum AdminMode{
        DISCONTINUOUS, INCOMPLETE, UNSORTED, VALID
    }

    public static MockAdminBuilder createBuilder(){ return new MockAdminBuilder();}

    /**
     * This class acts as a configuration/builder for the mockadmin
     * The rate of drops indicates the rate at which a block should be randomly removed
     * The number to remove indicates the number of blocks to be removed from either the tail or the front of the list
     * The rate of swaps indicate the rate at which the blocks should be swapped. This indicates an unordered list.
     */

    public static class MockAdminBuilder{

        int rateOfDrops;
        int rateOfSwaps;
        int numberToRemove;
        AdminMode mode;
        MockDataGenerator dataSource;


        private MockAdminBuilder(){
            mode = AdminMode.VALID;
            rateOfDrops =10;
            rateOfSwaps =5;
            numberToRemove=10;
            dataSource = MockDataGeneratorCSV
                    .of(new MockDataGeneratorCSV.BlockDataConfig(), new MockDataGeneratorCSV.TransactionDataConfig());
        }


        public MockAdminBuilder setRateOfDrops(int rateOfDrops) {
            this.rateOfDrops = rateOfDrops;
            return this;
        }

        public MockAdminBuilder setRateOfSwaps(int rateOfSwaps) {
            this.rateOfSwaps = rateOfSwaps;
            return this;
        }

        public MockAdminBuilder setNumberToRemove(int numberToRemove) {
            this.numberToRemove = numberToRemove;
            return this;
        }

        public MockAdminBuilder setMode(AdminMode mode) {
            this.mode = mode;
            return this;
        }

        public MockAdmin build() throws Exception{
            return new MockAdmin(mode, rateOfDrops, rateOfSwaps, numberToRemove, dataSource);
        }

        public MockAdminBuilder setDataSource(MockDataGenerator dataSource) {
            this.dataSource = dataSource;
            return this;
        }
    }
}
