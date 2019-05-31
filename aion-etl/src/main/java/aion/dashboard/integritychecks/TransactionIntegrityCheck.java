package aion.dashboard.integritychecks;

import aion.dashboard.blockchain.interfaces.APIService;
import aion.dashboard.blockchain.type.APITransaction;
import aion.dashboard.domainobject.Block;
import aion.dashboard.domainobject.Transaction;
import aion.dashboard.service.BlockService;
import aion.dashboard.service.ParserStateService;
import aion.dashboard.service.TransactionService;
import aion.dashboard.util.Tuple2;
import aion.dashboard.util.Utils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TransactionIntegrityCheck extends IntegrityCheck<Block, Tuple2<Transaction, APITransaction>>{
    private final BlockService blockService;
    private final TransactionService service;
    private final ParserStateService parserStateService;
    private final APIService apiService;


    protected TransactionIntegrityCheck( BlockService blockService, TransactionService service, ParserStateService parserStateService, APIService apiService) {
        super("tx-integrity-check", "tx");
        this.blockService = blockService;
        this.service = service;
        this.parserStateService = parserStateService;
        this.apiService = apiService;
    }

    @Override
    protected List<Tuple2<Transaction, APITransaction>> integrityCheck(List<Block> candidates) throws Exception {
        List<Tuple2<Transaction, APITransaction>> res = new ArrayList<>();

        for (var block: candidates){
            //get the transactions for this block
            var transactions = service.getTransactionByBlockNumber(block.getBlockNumber());
            Set<String> hashes = getTransactionsFromBlock(block);
            // check that no transaction is missing
            res.addAll(findMissingTransactions(transactions, hashes));

            for (var tx: transactions){
                var apiTx = apiService.getTransaction(tx.getTransactionHash());// get the transaction from the api
                if (!apiTx.compareTransactions(tx)){
                    res.add(new Tuple2<>(tx, apiTx));//
                }
            }

        }

        return res;
    }

    private List<Tuple2<Transaction, APITransaction>> findMissingTransactions(List<Transaction> transactions, Set<String> hashes) {
        return transactions.stream()
                .filter(tx->!hashes.contains(tx.getTransactionHash()))
                .map(tx->new Tuple2<Transaction, APITransaction>(tx, null))
                .collect(Collectors.toList());
    }


    private Set<String> getTransactionsFromBlock(Block b){

        return Stream.of(b.getTransactionHashes().replaceAll("(\\[|]|\")", "").split(","))// get the transactions from the area
                .map(Utils::sanitizeHex)// clean up the hex
                .collect(Collectors.toCollection(HashSet::new));// store as a hashset
    }

    /**
     * @return the random list of values to use for the integrity check
     */
    @Override
    protected List<Block> findCandidates() throws Exception {
        return getRandomBlocks(parserStateService,blockService);
    }

    @Override
    protected void printFailure(List<Tuple2<Transaction, APITransaction>> failedCandidates) {
        for(var tuple : failedCandidates){
            INTEGRITY_LOGGER.warn("Transaction check failed for: {}", tuple);
        }
    }

    @Override
    protected void printSuccess(List<Block> succeededCandidates) {

    }
}
