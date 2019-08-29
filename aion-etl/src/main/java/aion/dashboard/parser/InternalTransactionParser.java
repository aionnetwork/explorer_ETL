package aion.dashboard.parser;

import aion.dashboard.blockchain.interfaces.Web3Service;
import aion.dashboard.blockchain.type.APIInternalTransaction;
import aion.dashboard.domainobject.InternalTransaction;
import aion.dashboard.domainobject.ParserState;
import aion.dashboard.exception.Web3ApiException;
import aion.dashboard.parser.type.InternalTransactionBatch;
import aion.dashboard.parser.type.Message;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class InternalTransactionParser extends IdleProducer<InternalTransactionBatch, Void> {
    private final Web3Service web3Service;

    public InternalTransactionParser(BlockingQueue<List<InternalTransactionBatch>> queue,
                                     BlockingQueue<List<Message<Void>>> workQueue,
                                     Web3Service web3Service) {
        super(queue, workQueue);
        this.web3Service = web3Service;
    }
    ParserState.ParserStateBuilder psBuilder=new ParserState.ParserStateBuilder();

    @Override
    protected List<InternalTransactionBatch> doTask(List<Message<Void>> messages) throws Web3ApiException {
        Thread.currentThread().setName("itx-parser");
        GENERAL.info("Starting internal transaction parser");
        InternalTransactionBatch batch = new InternalTransactionBatch();
        for(var message: messages){
            for (var tx: message.getBlockDetails().getTxDetails()){
                List<APIInternalTransaction> internalTransactions = web3Service.getInternalTransaction(tx.getTxHash().toString());
                if (GENERAL.isTraceEnabled()){
                    GENERAL.trace("Found an internal transaction at block number {}", message.getBlockDetails().getNumber());
                }
                //Store the transactions to be written
                for (int i = 0; i < internalTransactions.size(); i++) {
                    APIInternalTransaction itx = internalTransactions.get(i);
                    InternalTransaction from = InternalTransaction.from(itx,
                            tx.getTxHash().toString(),
                            i,
                            message.getBlockDetails().getNumber(),
                            message.getBlockDetails().getTimestamp()
                    );
                    batch.addInternalTransaction(from);
                }
            }
            ParserState ps = psBuilder
                    .blockNumber(BigInteger.valueOf(message.getBlockDetails().getNumber()))
                    .id(8)
                    .build();
            batch.setParserState(ps);
        }
        if (batch.getInternalTransactions().isEmpty()) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(batch);
        }
    }
}
