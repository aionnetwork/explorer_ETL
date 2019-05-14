package aion.dashboard.parser;

import aion.dashboard.blockchain.APIService;
import aion.dashboard.domainobject.Account;
import aion.dashboard.domainobject.ParserState;
import aion.dashboard.parser.type.AccountBatch;
import aion.dashboard.parser.type.Message;
import aion.dashboard.service.ParserStateServiceImpl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class AccountParser extends IdleProducer<AccountBatch, String> {
    private final APIService service;
    private final ExecutorService apiExecutor;

    public AccountParser(
            BlockingQueue<List<AccountBatch>> queue,
            APIService service,
            BlockingQueue<List<Message<String>>> workQueue
    ) {
        super(queue, workQueue);
        this.service = service;
        apiExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }




    @Override
    protected List<AccountBatch> task() throws Exception {
        super.task();
        Thread.currentThread().setName("account-parser");
        List<Message<String>> messages = getMessage();
        GENERAL.debug("Starting account parser.");
        long maxBlockNumber=-1;
        Set<String> addresses= new HashSet<>();
        AccountBatch batch = new AccountBatch();
        List<CompletableFuture<Void>> futures= new ArrayList<>();
        for (var msg: messages){

            for( String address : msg.getItem()){
                if (addresses.add(address)) {// add the messages to the set so that we avoid making duplicate api calls
                    if (GENERAL.isTraceEnabled()) {
                        GENERAL.trace("Starting request for account: {}", address);
                    }

                    CompletableFuture<Void> future = CompletableFuture
                            .supplyAsync(() -> getAccount(msg, address),apiExecutor)
                            .thenAccept(batch::addAccount);
                    futures.add(future);
                }
            }
            if (msg.getBlockDetails().getNumber()>maxBlockNumber){
                maxBlockNumber=msg.getBlockDetails().getNumber();
            }

        }

        for (var future:futures){
            future.get();
        }

        batch.setState(new ParserState(ParserStateServiceImpl.DB_ID, BigInteger.valueOf(maxBlockNumber)));
        consumeMessage();
        return List.of(batch);
    }

    private Account getAccount(Message<String> msg, String address) {
        try {
            return Account.from(address,
                    msg.getBlockDetails(),
                    msg.getTxDetails(),
                    new BigDecimal(service.getBalance(address)),
                    service.getNonce(address));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
