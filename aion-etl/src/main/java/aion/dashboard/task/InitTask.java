package aion.dashboard.task;

import aion.dashboard.blockchain.AionService;
import aion.dashboard.config.Config;
import aion.dashboard.blockchain.Extractor;
import aion.dashboard.blockchain.Web3Service;
import aion.dashboard.consumer.*;
import aion.dashboard.exception.AionApiException;
import aion.dashboard.parser.*;
import aion.dashboard.service.*;
import aion.dashboard.worker.IntegrityCheckThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public final class InitTask {

    private InitTask(){
        throw new UnsupportedOperationException("Cannot create an instance of: "+ InitTask.class.getSimpleName());
    }

    private static final String VERSION_NUMBER = "v2.2";
    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");

    private static void revert(String blk) {

        ParserStateServiceImpl ps = ParserStateServiceImpl.getInstance();
        ReorgServiceImpl reorgService = new ReorgServiceImpl(AionService.getInstance(), ps);
        long blknum = Long.parseLong(blk) + 1;
        try {
            reorgService.performReorg(blknum);
            GENERAL.info("New database head: {}", ps.readDBState());

        } catch (SQLException e) {
            String message = "unable to delete the blocks in the database.";

            GENERAL.error("Revert failed: {}", message);
        } catch (AionApiException e) {
            String message = "unable to reach the API.";

            GENERAL.error("Revert failed: {}", message);
        }

    }

    private static void printVersion() {
        System.out.println("Aion ETL "+ VERSION_NUMBER);
    }


    public static void checkArgs(String[] arg) {
        switch (arg[0]) {
            case "-v":
                printVersion();
                break;
            case "-r":
                if (arg.length == 2)
                    revert(arg[1]);
                else
                    System.out.println("Wrong number of arguments passed to revert! \nExpected 2 got " + arg.length + ".");
                break;
            default:
                System.out.println("Unrecognized argument");

        }
    }

    public static void start() throws AionApiException {
        Logger general = GENERAL;
        general.info("--------------------------------");
        general.info("Starting ETL {}", VERSION_NUMBER);
        general.info("--------------------------------");

        ParserStateService ps = ParserStateServiceImpl.getInstance();
        ContractService contractService= ContractServiceImpl.getInstance();
        TokenService tokenService = TokenServiceImpl.getInstance();

        Extractor extractor = new Extractor(AionService.getInstance(),ps, new LinkedBlockingDeque<>());

        TokenParser tokenParser= new TokenParser(new LinkedBlockingDeque<>(), new LinkedBlockingDeque<>(), AionService.getInstance(), contractService,tokenService);

        AccountParser accountParser = new AccountParser(new LinkedBlockingQueue<>(), Web3Service.getInstance(), new LinkedBlockingQueue<>());

        Parser parser = new ParserBuilder().setAccountProd(accountParser)
                .setTokenProd(tokenParser)
                .setQueue(new LinkedBlockingQueue<>())
                .setRollingBlockMean(RollingBlockMean.init(ps, AionService.getInstance()))
                .setExtractor(extractor).setApiService(Web3Service.getInstance()).createParser();


        Consumer consumer = new ConsumerBuilder().setAccountProducer(accountParser)
                .setBlockProducer(parser).setTokenProducer(tokenParser)
                .setAccountWriter(new AccountWriter())
                .setBlockWriter(new BlockWriter())
                .setTokenWriter(new TokenWriter())
                .setService(new ReorgServiceImpl(AionService.getInstance(),ps))
                .createConsumer();



        List<Producer> producers = new ArrayList<>();
        producers.add(tokenParser);
        producers.add(accountParser);


        IntegrityCheckThread checkThread = new IntegrityCheckThread();
        checkThread.start();
        extractor.start();
        parser.start();
        tokenParser.start();
        accountParser.start();
        consumer.start();


        AbstractGraphingTask task = AbstractGraphingTask.getInstance(Config.getInstance().getTaskType());
        task.scheduleNow();


        Runtime.getRuntime().addShutdownHook(buildShutdownHook(extractor, parser, consumer, producers, task));
    }

    private static Thread buildShutdownHook(Extractor extractor, Parser parser, Consumer consumer, List<Producer> producers, AbstractGraphingTask task) {
        return new Thread(()->{
            Thread.currentThread().setName("shutdown-hook");
            GENERAL.info("Shutting down the ETL.");
            shutdownProducer(extractor);
            shutdownProducer(parser);


            for (var producer:producers){
                shutdownProducer(producer);
            }

            consumer.stop();
            task.stop();
            AionService.getInstance().close();
            GENERAL.info("Succesfully shutdown the ETL");

        });
    }

    private static void shutdownProducer(Producer<?> producer){
        producer.stop();
        producer.awaitTermination();
    }

}