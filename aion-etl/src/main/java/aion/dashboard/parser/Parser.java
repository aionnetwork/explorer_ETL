package aion.dashboard.parser;

import aion.dashboard.blockchain.AionService;
import aion.dashboard.blockchain.interfaces.APIService;
import aion.dashboard.parser.events.EventDecoder;
import aion.dashboard.blockchain.Extractor;
import aion.dashboard.domainobject.*;
import aion.dashboard.parser.type.Message;
import aion.dashboard.parser.type.ParserBatch;
import aion.dashboard.service.ParserStateServiceImpl;
import aion.dashboard.service.RollingBlockMean;
import aion.dashboard.parser.events.ContractEvent;
import org.aion.api.type.BlockDetails;
import org.aion.api.type.TxDetails;
import org.json.JSONArray;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Parser extends Producer<ParserBatch> {
    private final Extractor extractor;
    private final RollingBlockMean rollingBlockMean;
    private final IdleProducer<?, String> accountProd;
    private final TokenParser tokenProd;
    private final AionService apiService;
    private final ExecutorService rollingMeanExecutor = Executors.newFixedThreadPool(2);

    Parser(Extractor extractor, BlockingQueue<List<ParserBatch>> queue, RollingBlockMean rollingBlockMean, IdleProducer<?, String> accountProd, TokenParser tokenProd, AionService apiService) {
        super(queue);
        this.extractor = extractor;
        this.rollingBlockMean = rollingBlockMean;
        this.accountProd = accountProd;
        this.tokenProd = tokenProd;
        this.apiService = apiService;
    }

    @Override
    public void reset() {
        extractor.reset();// trigger the reset
        //Trigger a flag to start the reset and stop the running task
        super.reset();
    }

    @Override
    protected List<ParserBatch> task() throws Exception {
        var blocks = extractor.peek();
        Thread.currentThread().setName("block-parser");

        if (blocks.hasNext()) {
            var batch = parseBlk(blocks);
            extractor.consume();

            return Collections.singletonList(batch);
        } else {
            return Collections.emptyList();
        }
    }

    private ParserBatch parseBlk(Iterator<BlockDetails> blockDetails) throws Exception {
        ParserBatch batchObject = new ParserBatch();

        BlockDetails block = null;
        List<Message<String>> accountsMessages=new ArrayList<>();
        List<Message<ContractEvent>> tokenMessages = new ArrayList<>();

        GENERAL.debug("Starting parser.");
        while (blockDetails.hasNext()) {
            block = blockDetails.next();

            Set<String> addressesFromBlock=new HashSet<>();
            //loop through blocks

            if (GENERAL.isTraceEnabled()) {
                GENERAL.trace("Parsing block: {}. With hash: {}", block.getNumber(), block.getHash());
            }

            //Add miner
            addressesFromBlock.add(block.getMinerAddress().toString());

            // add block to rolling mean
            rollingBlockMean.add(block);


            BigDecimal nrgReward = new BigDecimal(0);
            JSONArray array = new JSONArray();
            final List<TxDetails> txDetails = block.getTxDetails();
            var canRead = Parsers.containsReadableEvent(block.getBloom().toBytes());
            //loop through the transactions
            for (var tx : txDetails) {
                array.put(tx.getTxHash().toString());

                final Optional<Contract> contract = Parsers.readContract(tx, block);
                contract.ifPresent(contract1 -> {
                    registerContracts(contract1);
                    batchObject.addContract(contract1);
                });//Attempt to read the contract information
                addressesFromBlock.addAll(Parsers.accFromTransaction(tx));


                if (!tx.getFrom().equals(block.getMinerAddress())) {
                    nrgReward = nrgReward.add(BigDecimal.valueOf(tx.getNrgPrice())
                            .multiply(BigDecimal.valueOf(tx.getNrgConsumed())));
                }

                batchObject.addTx(Transaction.from(tx, block));

                Parsers.parseEvents(batchObject, block, tokenMessages, canRead, tx);
            }


            var firstTxHash = Parsers.getFirstTxHash(txDetails);


            batchObject.addBlock(Block.from(block, firstTxHash, array.toString(), nrgReward, apiService.getBlockReward(block.getNumber())));

            accountsMessages.add(new Message<>(new ArrayList<>(addressesFromBlock), block, txDetails.isEmpty()? null: txDetails.get(0)));

        }

        final var lastBlockNumber = block == null ? -1 : block.getNumber();

        if (!accountsMessages.isEmpty()) {
            accountProd.submitAll(accountsMessages);
        }

        if (!tokenMessages.isEmpty()) {
            tokenProd.submitAll(tokenMessages);
        }
        if (lastBlockNumber >= 0) {
            //run these operation asynchronously
            final CompletableFuture<Void> future0 = CompletableFuture
                    .supplyAsync(() -> rollingBlockMean.computeRTMetricsFrom(lastBlockNumber), rollingMeanExecutor)
                    .thenAcceptAsync(o -> o.ifPresent(batchObject::addMetric), rollingMeanExecutor);
            final CompletableFuture<Void> future1 = CompletableFuture
                    .supplyAsync(() -> rollingBlockMean.computeStableMetricsFrom(lastBlockNumber), rollingMeanExecutor)
                    .thenAcceptAsync(o -> o.ifPresent(batchObject::addMetric), rollingMeanExecutor);

            future0.join();
            future1.join();

            batchObject.setMeanStates(rollingBlockMean.getStates());
        }


        batchObject.setState(new ParserState(ParserStateServiceImpl.DB_ID, BigInteger.valueOf(lastBlockNumber)));
        batchObject.setBlockChainState(new ParserState(ParserStateServiceImpl.BLKCHAIN_ID,BigInteger.valueOf(apiService.getBlockNumber())));
        return batchObject;
    }//parse blk


    private void registerContracts(Contract contract1){
        EventDecoder.register(contract1);
        tokenProd.registerContract(contract1);
    }


    @Override
    protected void doReset() {
        queue.clear();

        while (extractor.shouldReset()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        shouldReset.compareAndSet(true, false);

    }


}
