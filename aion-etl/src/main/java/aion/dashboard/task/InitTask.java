package aion.dashboard.task;

import aion.dashboard.blockchain.AionService;
import aion.dashboard.blockchain.Extractor;
import aion.dashboard.blockchain.interfaces.Web3Service;
import aion.dashboard.config.BuildVersion;
import aion.dashboard.config.Config;
import aion.dashboard.consumer.*;
import aion.dashboard.exception.AionApiException;
import aion.dashboard.integritychecks.IntegrityCheckManager;
import aion.dashboard.parser.*;
import aion.dashboard.service.*;
import aion.dashboard.update.UpdateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public final class InitTask {

    private InitTask(){
        throw new UnsupportedOperationException("Cannot create an instance of: "+ InitTask.class.getSimpleName());
    }

    private static final String VERSION_NUMBER = "v2.3";
    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general");

    private static void revert(String blk) {

        ParserStateServiceImpl ps = ParserStateServiceImpl.getInstance();
        ReorgServiceImpl reorgService = new ReorgServiceImpl(AionService.getInstance(), ps, Web3Service.getInstance());
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
        System.out.println("Aion ETL "+ BuildVersion.VERSION.replaceAll("-[0-9]{4}(-[0-9]{1,2}){2}-20[0-9]{2}", ""));
        System.out.println("Built on "+ BuildVersion.BUILD_DATE);
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
        general.info("Starting ETL {}", BuildVersion.VERSION.replaceAll("-[0-9]{4}(-[0-9]{1,2}){2}-20[0-9]{2}", ""));
        general.info("--------------------------------");

        ParserStateService ps = ParserStateServiceImpl.getInstance();
        ContractService contractService= ContractServiceImpl.getInstance();
        TokenService tokenService = TokenServiceImpl.getInstance();

        Config config = Config.getInstance();
        int queueSize = config.getQueueSize()<Integer.MAX_VALUE ? (int)config.getQueueSize(): 100;
        Extractor extractor = new Extractor(AionService.getInstance(),ps, new ArrayBlockingQueue<>(queueSize),config.getBlockQueryRange());

        TokenParser tokenParser= new TokenParser(new ArrayBlockingQueue<>(queueSize), new LinkedBlockingDeque<>(), AionService.getInstance(), contractService,tokenService);

        AccountParser accountParser = new AccountParser(new ArrayBlockingQueue<>(queueSize), Web3Service.getInstance(), new LinkedBlockingQueue<>());

        Parser parser = new ParserBuilder().setAccountProd(accountParser)
                .setTokenProd(tokenParser)
                .setQueue(new ArrayBlockingQueue<>(queueSize))
                .setRollingBlockMean(RollingBlockMean.init(ps, AionService.getInstance()))
                .setExtractor(extractor).setApiService(AionService.getInstance()).createParser();


        Consumer consumer = new ConsumerBuilder().setAccountProducer(accountParser)
                .setBlockProducer(parser).setTokenProducer(tokenParser)
                .setAccountWriter(new AccountWriter())
                .setBlockWriter(new BlockWriter())
                .setTokenWriter(new TokenWriter())
                .setService(new ReorgServiceImpl(AionService.getInstance(),ps, Web3Service.getInstance()))
                .createConsumer();

        UpdateManager.getInstance().start();

        List<Producer> producers = new ArrayList<>();
        producers.add(tokenParser);
        producers.add(accountParser);


        IntegrityCheckManager.getInstance().startAll();
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
            IntegrityCheckManager.getInstance().shutdown();
            Web3Service.getInstance().close();
            AionService.getInstance().close();
            GENERAL.info("Successfully shutdown the ETL");

        });
    }

    private static void shutdownProducer(Producer<?> producer){
        producer.stop();
        producer.awaitTermination(5000L);
    }

}
