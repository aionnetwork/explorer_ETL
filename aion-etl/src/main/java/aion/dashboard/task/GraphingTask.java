package aion.dashboard.task;

import aion.dashboard.blockchain.AionService;
import aion.dashboard.domainobject.Graphing;
import aion.dashboard.email.EmailService;
import aion.dashboard.exception.DbServiceException;
import aion.dashboard.service.*;
import aion.dashboard.util.TimeLogger;
import org.aion.api.type.BlockDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class GraphingTask extends AbstractGraphingTask<BlockDetails> {
    private static final TimeLogger TIME_LOGGER = new TimeLogger("");
    private AionService service;
    private ParserStateService parser_stateService;





    static final GraphingTask API_GRAPHING_TASK = new GraphingTask(AionService.getInstance());


    private GraphingTask(AionService service){
        this.service = service;
        this.parser_stateService = ParserStateServiceImpl.getInstance();

    }



    @Override
    public void run() {
        Thread.currentThread().setName("GraphingTask");

        GENERAL.info("Starting Graphing Task");
        TIME_LOGGER.start();

        try {
            service.reconnect();

            long endNumberDB = parser_stateService.readDBState().getBlockNumber().longValue();// get the current height of the DB to know how many records can be computed



            long startNumber = parser_stateService.readGraphingState().getBlockNumber().longValue() + 1;
            long range = endNumberDB - startNumber > 1000 ? 1000 : endNumberDB - startNumber;// get the range of values to be extracted from the kernel



            while (range>0 && !Thread.currentThread().isInterrupted()) {

                List<BlockDetails> detailsList = service.getBlockDetailsByRange(startNumber, startNumber + range - 1);
                ZonedDateTime blockTIme = Instant.ofEpochMilli(detailsList.get(0).getTimestamp() * 1000).atZone(UTCZoneID);
                int startHour = blockTIme.getHour();

                if (generatedThisHour(blockTIme)) break;

                List<BlockDetails> listToCompute = detailsList.parallelStream().filter(e -> {

                    ZonedDateTime dateTime = Instant.ofEpochMilli(e.getTimestamp() *1000).atZone(UTCZoneID);

                    return startHour == dateTime.getHour();//Get all the blocks that occurred within this hour
                }).collect(Collectors.toList());


                int initialSize = detailsList.size();
                int addedValues = 0;


                long lastBlock = -1;
                while (initialSize+addedValues == listToCompute.size() && lastBlock !=service.getBlockNumber()){// to handle hours with more than a thousand blocks
                    List<BlockDetails> additionalElements = service.
                            getBlockDetailsByRange(addedValues+startNumber + range,
                                    addedValues+startNumber + range + 199);

                    addedValues += additionalElements.size();
                    listToCompute.addAll(
                            additionalElements.parallelStream().filter(e -> {

                                ZonedDateTime dateTime = Instant.ofEpochMilli(e.getTimestamp() *1000).atZone(UTCZoneID);

                                return startHour == dateTime.getHour();//Get all the blocks that occurred within this hour
                            }).collect(Collectors.toList())
                    );
                    lastBlock = additionalElements.get(additionalElements.size() -1).getNumber();
                }
                if(listToCompute.size() == initialSize + addedValues) {
                    GENERAL.debug("Tried to extract list larger than the kernel's Database");

                    break;// This check makes sure that we never update the graphing information if the Kernel is not up to date
                    //because of all the care done in the scheduling this is probably an indication that the kernel is unable to update
                }


                lastBlock=listToCompute.get(listToCompute.size() -1).getNumber();


                if (lastBlock > parser_stateService.readDBState().getBlockNumber().longValue()){
                    GENERAL.debug("Kernel's last block is greater that the DB.");

                    break;
                    //throw new GraphingException("Failed Sanity check. Last block is greater than the DB parser state.");//sanity check this should never happen
                }



                List<Graphing> computedGraphPoints = compute(listToCompute);


                if (!graphingService.save(computedGraphPoints)) throw new DbServiceException("Failed to save graphing");




                endNumberDB = parser_stateService.readDBState().getBlockNumber().longValue();

                startNumber = listToCompute.get(listToCompute.size() - 1).getNumber() + 1;
                range = endNumberDB - startNumber > 1000 ? 1000 : endNumberDB - startNumber;
            }

        }
        catch (DbServiceException | SQLException e){
            GENERAL.debug("Caught exception in graphing task: ", e);
            EmailService
                    .getInstance()
                    .send("Graphing service", String.format("Failed to update statistics at: %s %n Threw exception: %s \n Shutting down the graph task.", ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME), e.toString()));
            GENERAL.error("Shutting down graphing task");
            return;
        }
        catch (Exception e) {
            GENERAL.debug("Caught exception in graphing task: ", e);
            EmailService
                    .getInstance()
                    .send("Graphing service", String.format("Failed to update statistics at: %s %n Threw exception: %s", ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME), e.toString()));

        }

        GENERAL.info("Statistics up to date.");
        TIME_LOGGER.logTime("Computed statistics in {}");


        scheduleNext();
    }


    @SuppressWarnings("Duplicates")
    List<Graphing> compute(List<BlockDetails> blockDetails) throws SQLException {
        GENERAL.debug("Computing Statistics for blocks in range({}, {})", blockDetails.get(0).getNumber(),
                blockDetails.get(blockDetails.size() - 1).getNumber());
        final BigDecimal BlocksMined = BigDecimal.valueOf(blockDetails.size());
        final BigDecimal averageDifficulty = blockDetails.parallelStream()
                .map(b -> new BigDecimal(b.getDifficulty()))//Get the difficulty of each block
                .reduce(BigDecimal.ZERO, BigDecimal::add)//Accumulate the difficulties
                .divide(BigDecimal.valueOf(blockDetails.size()), MathContext.DECIMAL64);//Find the average


        final BigDecimal averageBlockTime = blockDetails.parallelStream()
                .map(b -> BigDecimal.valueOf(b.getBlockTime()))//get the block time of each block
                .reduce(BigDecimal.ZERO, BigDecimal::add)//Accumulate the time
                .divide(BigDecimal.valueOf(blockDetails.size()), MathContext.DECIMAL64);// find the average over the period


        final long transactionOverTime = blockDetails.parallelStream()
                .mapToLong(b -> b.getTxDetails().size())// get the size of each transaction list
                .sum();// sum up the size of the lists


        final BigDecimal averageHashPower = new BigDecimal(blockDetails.get(blockDetails.size() -1).getDifficulty())
                .divide(averageBlockTime, MathContext.DECIMAL64);// find the hash power by dividing the difficulty and the time for each block
        final BigDecimal activeAddressesCount = BigDecimal.valueOf(graphingService.countActiveAddresses(
                blockDetails.get(blockDetails.size() - 1).getNumber()));


        Map<String, Integer> minerCount = new HashMap<>();


        for (var blockDetail : blockDetails) {


            if (minerCount.containsKey(blockDetail.getMinerAddress().toString())) {//find all the Miners for this hour

                int count = minerCount.get(blockDetail.getMinerAddress().toString());
                minerCount.replace(blockDetail.getMinerAddress().toString(), count + 1);


            } else {
                minerCount.put(blockDetail.getMinerAddress().toString(), 1);
            }


        }
        //TODO convert this from an Object[] to a Type
        List<Object[]> topMinerPair = minerCount.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .map(e -> new Object[]{e.getKey(), e.getValue()})
                .collect(Collectors.toList());

        List<Graphing> ret = new ArrayList<>();






        long timeStamp = blockDetails.get(blockDetails.size() - 1).getTimestamp();



        ZonedDateTime dateTime = Instant.ofEpochMilli(timeStamp)
                .atZone(UTCZoneID);


        Graphing.GraphingBuilder builder = new Graphing.GraphingBuilder();
        //Build the graphing object with the defaults
        builder.setBlockNumber(blockDetails.get(blockDetails.size() - 1).getNumber())
                .setTimestamp(timeStamp);

        for (int i = topMinerPair.size()-1; i>=0 && i>=topMinerPair.size() -25; i --){// Get the top 25 Since this is sorted in ascending order start from the tail of the list
            ret.add(
                builder.setValue(BigDecimal.valueOf((int)topMinerPair.get(i)[1])).setDetail((String) topMinerPair.get(i)[0])
                    .setGraphType(Graphing.GraphType.TOP_MINER).build()
            );


        }


        ret.add(builder.setValue(BigDecimal.valueOf(transactionOverTime)).setGraphType(Graphing.GraphType.TRANSACTION_OVER_TIME).setDetail("").build());
        ret.add(builder.setValue(averageBlockTime).setGraphType(Graphing.GraphType.BLOCK_TIME).setDetail("").build());
        ret.add(builder.setValue(averageDifficulty).setGraphType(Graphing.GraphType.DIFFICULTY).setDetail("").build());
        ret.add(builder.setValue(averageHashPower).setGraphType(Graphing.GraphType.HASH_POWER).setDetail("").build());
        ret.add(builder.setValue(activeAddressesCount).setGraphType(Graphing.GraphType.ACTIVE_ADDRESS_GROWTH).setDetail("").build());
        ret.add(builder.setValue(BlocksMined).setGraphType(Graphing.GraphType.BLOCKS_MINED).setDetail("").build());

        return ret;
    }





    static boolean generatedThisHour(ZonedDateTime time){
        ZonedDateTime timeNow = Instant.now().atZone(UTCZoneID).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime blockTime = time.withMinute(0).withSecond(0).withNano(0);

        return timeNow.compareTo(blockTime) == 0;
    }
}
