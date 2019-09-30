package aion.dashboard.parser;

import aion.dashboard.blockchain.Web3Extractor;
import aion.dashboard.blockchain.interfaces.Web3Service;
import aion.dashboard.blockchain.type.APIBlockDetails;
import aion.dashboard.blockchain.type.APITxDetails;
import aion.dashboard.cache.CacheManager;
import aion.dashboard.domainobject.*;
import aion.dashboard.parser.type.Message;
import aion.dashboard.parser.type.ParserBatch;
import aion.dashboard.service.ParserStateServiceImpl;
import aion.dashboard.stats.RollingBlockMean;
import aion.dashboard.parser.events.ContractEvent;
import aion.dashboard.util.Utils;
import org.aion.util.bytes.ByteUtil;
import org.json.JSONArray;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class Parser extends Producer<ParserBatch> {
    private final Web3Extractor extractor;
    private final RollingBlockMean rollingBlockMean;
    private final IdleProducer<?, String> accountProd;
    private final TokenParser tokenProd;
    private final Web3Service apiService;
    private final InternalTransactionParser internalTransactionProducer;
    private final ExecutorService rollingMeanExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    Parser(Web3Extractor extractor, BlockingQueue<List<ParserBatch>> queue, RollingBlockMean rollingBlockMean, IdleProducer<?, String> accountProd, TokenParser tokenProd, Web3Service apiService, InternalTransactionParser internalTransactionProducer) {
        super(queue);
        this.extractor = extractor;
        this.rollingBlockMean = rollingBlockMean;
        this.accountProd = accountProd;
        this.tokenProd = tokenProd;
        this.apiService = apiService;
        this.internalTransactionProducer = internalTransactionProducer;
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

    ParserBatch parseBlk(Iterator<APIBlockDetails> blockDetails) throws Exception {
        ParserBatch batchObject = new ParserBatch();

        APIBlockDetails block = null;
        List<Message<String>> accountsMessages=new ArrayList<>();
        List<Message<ContractEvent>> tokenMessages = new ArrayList<>();
        List<Message<Void>> internalTxMessages = new ArrayList<>();
        List<CompletableFuture> futures = new ArrayList<>();
        GENERAL.debug("Starting parser.");
        while (blockDetails.hasNext()) {
            block = blockDetails.next();

            Set<String> addressesFromBlock=new HashSet<>();
            //loop through blocks

            if (GENERAL.isTraceEnabled()) {
                GENERAL.trace("Parsing block: {}. With hash: {}", block.getNumber(), block.getHash());
            }

            //Add miner
            addressesFromBlock.add(block.getMiner());
            // add block to rolling mean
            futures.addAll(computeMetricsAsync(batchObject, block));

            BigDecimal nrgReward = new BigDecimal(0);
            JSONArray array = new JSONArray();
            final List<APITxDetails> txDetails = block.getTxDetails();
            var canRead = Parsers.containsReadableEvent(ByteUtil.hexStringToBytes(block.getBloom()));
            //loop through the transactions
            for (var tx : txDetails) {
                array.put(Utils.sanitizeHex(tx.getTransactionHash()));

                final Optional<Contract> contract = Parsers.readContract(tx, block);
                contract.ifPresent(contract1 -> {
                    registerContracts(contract1);
                    batchObject.addContract(contract1);
                });//Attempt to read the contract information
                addressesFromBlock.addAll(Parsers.accFromTransaction(tx));


                if (!isMinerTx(block, tx)) {
                    nrgReward = nrgReward.add(new BigDecimal(tx.getNrgPrice()).multiply(new BigDecimal(tx.getNrgUsed())));
                }

                batchObject.addTx(Transaction.from(tx, block));
                batchObject.addTxLogs(TxLog.logsFrom(block, tx));
                Parsers.parseEvents(batchObject, block, tokenMessages, canRead, tx);
            }


            var firstTxHash = Parsers.getFirstTxHash(txDetails);


            batchObject.addBlock(Block.from(block, firstTxHash, array.toString(), nrgReward ));

            accountsMessages.add(new Message<>(new ArrayList<>(addressesFromBlock), block, txDetails.isEmpty()? null: txDetails.get(0)));
            internalTxMessages.add(new Message<>(null, block, null));

        }

        final var lastBlockNumber = block == null ? -1 : block.getNumber();

        if (!accountsMessages.isEmpty()) {
            accountProd.submitAll(accountsMessages);
        }

        if (!tokenMessages.isEmpty()) {
            tokenProd.submitAll(tokenMessages);
        }
        if (!internalTxMessages.isEmpty()){
            internalTransactionProducer.submitAll(internalTxMessages);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
        batchObject.setMeanStates(rollingBlockMean.getStates());

        batchObject.setState(new ParserState(ParserStateServiceImpl.DB_ID, BigInteger.valueOf(lastBlockNumber)));
        batchObject.setBlockChainState(new ParserState(ParserStateServiceImpl.BLKCHAIN_ID,BigInteger.valueOf(apiService.getBlockNumber())));
        return batchObject;
    }//parse blk

    private List<CompletableFuture> computeMetricsAsync(ParserBatch batchObject, APIBlockDetails block) {
        rollingBlockMean.add(block);
        final long blockNumber = block.getNumber();
        var f0 = computeMeanForBlock(rollingBlockMean::computeRTMetricsFrom, blockNumber, batchObject);// calculate the  realtime metrics
        var f1 = computeMeanForBlock(rollingBlockMean::computeStableMetricsFrom, blockNumber, batchObject);// calculate the stable metrics

        return List.of(f1, f0);
    }

    private CompletableFuture<?> computeMeanForBlock(Function<Long, Optional<Metrics>> metricFunction, long blockNUmber, ParserBatch batch){
        return CompletableFuture.supplyAsync(()-> metricFunction.apply(blockNUmber), rollingMeanExecutor)
                .thenAcceptAsync(o-> o.ifPresent(batch::addMetric));
    }

    private boolean isMinerTx(APIBlockDetails block, APITxDetails tx) {
        return Utils.sanitizeHex(tx.getFrom()).equals(Utils.sanitizeHex(block.getMiner()));
    }


    private void registerContracts(Contract contract1){
        CONTRACT_CACHE.putIfAbsent(contract1.getContractAddr(),contract1);
        tokenProd.registerContract(contract1);
    }

    private static final CacheManager<String,Contract> CONTRACT_CACHE = CacheManager.getManager(CacheManager.Cache.CONTRACT);

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
