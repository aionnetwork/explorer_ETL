package aion.dashboard.stats;

import aion.dashboard.domainobject.Block;
import aion.dashboard.domainobject.Graphing;
import aion.dashboard.exception.GraphingException;
import aion.dashboard.service.BlockService;
import aion.dashboard.service.BlockServiceImpl;
import aion.dashboard.service.ParserStateService;
import aion.dashboard.service.ParserStateServiceImpl;
import aion.dashboard.util.Utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class DBGraphingTask extends AbstractGraphingTask<Block> {


    static final DBGraphingTask DB_GRAPHING_TASK = new DBGraphingTask();

    @Override
    public void run() {
        Thread.currentThread().setName("graphing-task");
        GENERAL.info("Starting graphing task...");
        long start = parserStateService.readGraphingState().getBlockNumber().longValue() +1;
        long dbHeight = parserStateService.readDBState().getBlockNumber().longValue();
        try {

            while (start < dbHeight && !Thread.currentThread().isInterrupted()) {
                var optionalBlocks = extractBlocksInRange(start);
                if (optionalBlocks.isPresent() ) {
                    var res = compute(optionalBlocks.get());
                    GENERAL.debug("Computed blocks in range: ({},{})",start, res.get(0).getBlockNumber() );
                    graphingService.save(res);
                }
                else {
                    break;
                }
                start = parserStateService.readGraphingState().getBlockNumber().longValue() +1;
                dbHeight = parserStateService.readDBState().getBlockNumber().longValue();
            }


        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        } catch (Exception e) {
            GENERAL.debug("Caught an exception: ", e);
        }

        scheduleNext();
    }

    private final ParserStateService parserStateService = ParserStateServiceImpl.getInstance();
    private final BlockService service= BlockServiceImpl.getInstance();

    Optional<List<Block>> extractBlocksInRange(long start) throws SQLException {

        Block startBlock = service.getByBlockNumber(start);

        long startTimeStamp = startBlock.getBlockTimestamp();
        long endTimestamp = Instant.ofEpochSecond(startTimeStamp).atZone(UTCZoneID).withMinute(0).withSecond(0).plusHours(1).toEpochSecond();


        final Optional<List<Block>> blocksInTimeRange = service.getBlocksInTimeRange(startTimeStamp, endTimestamp);
        if (blocksInTimeRange.isPresent() &&
                !blocksInTimeRange.get().isEmpty() &&
                Utils.getLastRecord(blocksInTimeRange.get()).getBlockNumber() < parserStateService.readDBState().getBlockNumber().longValue() - 5) {
            return blocksInTimeRange;
        } else {
            return Optional.empty();
        }
    }


    static boolean isValidRange(List<Block> blocks){

        long minTimestamp = blocks.stream().mapToLong(Block::getBlockTimestamp).min().orElseThrow();
        long maxTimestamp = blocks.stream().mapToLong(Block::getBlockTimestamp).max().orElseThrow();

        long range = Math.abs(maxTimestamp - minTimestamp);

        return checkAccuracy(range, 3600, 7500);



    }


    @SuppressWarnings("Duplicates")
    @Override
    List<Graphing> compute(List<Block> blocks) throws GraphingException, InterruptedException {


        final long end  = blocks.parallelStream().mapToLong(Block::getBlockNumber).max().orElseThrow();
        final long maxTimestamp = blocks.parallelStream().mapToLong(Block::getBlockTimestamp).max().orElseThrow();
        List<Graphing> res = Collections.synchronizedList(new ArrayList<>());
        CompletableFuture<Void> activeAddressFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return BigDecimal.valueOf(graphingService.countActiveAddresses(end));
            } catch (SQLException e) {
                throw new RuntimeException(e);// this is eventually replaced with a dedicate exception
                //ignore the linting rule here
            }
        }).thenAccept(bd -> res.add(new Graphing.GraphingBuilder()
                .setBlockNumber(end)
                .setDetail("")
                .setGraphType(Graphing.GraphType.ACTIVE_ADDRESS_GROWTH)
                .setValue(bd)
                .setTimestamp(maxTimestamp)
                .build()));
        List<Block> posBlocks = blocks.stream()
                .filter(b->b.getSealType().equalsIgnoreCase("Pos"))
                .collect(Collectors.toUnmodifiableList());
        List<Block> powBlocks = blocks.stream()
                .filter(b->b.getSealType().equalsIgnoreCase("Pow"))
                .collect(Collectors.toUnmodifiableList());


        final BigDecimal blocksMined = BigDecimal.valueOf(blocks.size());
        final BigDecimal averageDifficulty = blocks.parallelStream()
                .map(Block::getDifficulty)//Get the difficulty of each block
                .reduce(BigDecimal.ZERO, BigDecimal::add)//Accumulate the difficulties
                .divide(blocksMined, MathContext.DECIMAL64);//Find the average


        final BigDecimal averageBlockTime = blocks.parallelStream()
                .map(b -> BigDecimal.valueOf(b.getBlockTime()))//get the block time of each block
                .reduce(BigDecimal.ZERO, BigDecimal::add)//Accumulate the time
                .divide(blocksMined, MathContext.DECIMAL64);// find the average over the period


        final long transactionOverTime = blocks.parallelStream()
                .mapToLong(Block::getNumTransactions)// get the size of each transaction list
                .sum();// sum up the size of the lists

        final BigDecimal posAverageDifficulty = posBlocks.parallelStream()
                .map(Block::getDifficulty)//Get the difficulty of each block
                .reduce(BigDecimal.ZERO, BigDecimal::add)//Accumulate the difficulties
                .divide(blocksMined, MathContext.DECIMAL64);//Find the average


        final BigDecimal posAverageBlockTime = MetricsCalc.avgBlockTimeDO(posBlocks);// find the average over the period

        final BigDecimal powAverageDifficulty = powBlocks.parallelStream()
                .map(Block::getDifficulty)//Get the difficulty of each block
                .reduce(BigDecimal.ZERO, BigDecimal::add)//Accumulate the difficulties
                .divide(blocksMined, MathContext.DECIMAL64);//Find the average

        final BigDecimal powAverageBlockTime = MetricsCalc.avgBlockTimeDO(powBlocks);// find the average over the period

        final BigDecimal averageHashPower = (powBlocks.get(powBlocks.size() -1).getDifficulty())
                .divide(powAverageBlockTime, MathContext.DECIMAL64);// find the hash power by dividing the difficulty and the time for each block

        Map<String, Integer> minerCount = new HashMap<>();


        for (var blockDetail : blocks) {


            if (minerCount.containsKey(blockDetail.getMinerAddress())) {//find all the Miners for this hour

                int count = minerCount.get(blockDetail.getMinerAddress());
                minerCount.replace(blockDetail.getMinerAddress(), count + 1);


            } else {
                minerCount.put(blockDetail.getMinerAddress(), 1);
            }


        }
        List<Object[]> topMinerPair = minerCount.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .map(e -> new Object[]{e.getKey(), e.getValue()})
                .collect(Collectors.toList());




        Graphing.GraphingBuilder builder = new Graphing.GraphingBuilder();
        //Build the graphing object with the defaults
        builder.setBlockNumber(end)
                .setTimestamp(maxTimestamp);

        for (int i = topMinerPair.size()-1; i>=0 && i>=topMinerPair.size() -25; i --){// Get the top 25 Since this is sorted in ascending order start from the tail of the list
            res.add(
                    builder.setValue(BigDecimal.valueOf((int)topMinerPair.get(i)[1])).setDetail((String) topMinerPair.get(i)[0])
                            .setGraphType(Graphing.GraphType.TOP_MINER).build()
            );


        }


        res.add(builder.setValue(BigDecimal.valueOf(transactionOverTime)).setGraphType(Graphing.GraphType.TRANSACTION_OVER_TIME).setDetail("").build());
        res.add(builder.setValue(averageBlockTime).setGraphType(Graphing.GraphType.BLOCK_TIME).setDetail("").build());
        res.add(builder.setValue(averageDifficulty).setGraphType(Graphing.GraphType.DIFFICULTY).setDetail("").build());
        res.add(builder.setValue(averageHashPower).setGraphType(Graphing.GraphType.HASH_POWER).setDetail("").build());
        res.add(builder.setValue(blocksMined).setGraphType(Graphing.GraphType.BLOCKS_MINED).setDetail("").build());
        res.add(builder.setValue(posAverageBlockTime).setGraphType(Graphing.GraphType.POS_BLOCK_TIME).setDetail("").build());
        res.add(builder.setValue(powAverageBlockTime).setGraphType(Graphing.GraphType.POW_BLOCK_TIME).setDetail("").build());
        res.add(builder.setValue(posAverageDifficulty).setGraphType(Graphing.GraphType.POS_DIFFICULTY).setDetail("").build());
        res.add(builder.setValue(powAverageDifficulty).setGraphType(Graphing.GraphType.POW_DIFFICULTY).setDetail("").build());
        Utils.awaitResult(activeAddressFuture::isDone, result -> result);

        if (activeAddressFuture.isCompletedExceptionally()){
            throw new GraphingException();
        }else return res;
    }



    private static boolean checkAccuracy(long num, long val, long ppm){

        final int base = 1_000_000;
        BigInteger valBigInt = BigInteger.valueOf(val);
        BigInteger numBigInt = BigInteger.valueOf(num * base);
        BigInteger lowerLimit = BigInteger.valueOf(base - ppm).multiply(valBigInt);
        BigInteger upperLimit = BigInteger.valueOf(base + ppm).multiply(valBigInt);


        return numBigInt.compareTo(lowerLimit) >=0 && numBigInt.compareTo(upperLimit) <= 0;
    }
}
