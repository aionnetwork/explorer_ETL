package aion.dashboard.parser.type;

import aion.dashboard.domainobject.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParserBatch extends AbstractBatch{
    private List<Block> blocks;
    private List<Transaction> transactions;
    private List<InternalTransfer> internalTransfers;
    private List<Contract> contracts;
    private ParserState state;
    private List<Event> events;
    private List<Metrics> metrics;
    private ParserState blockChainState;
    private List<ParserState> meanStates;
    private List<TxLog> logs;



    public ParserBatch() {
        this.blocks = new ArrayList<>();
        this.transactions = new ArrayList<>();
        this.internalTransfers = new ArrayList<>();
        this.contracts = new ArrayList<>();
        this.state = null;
        this.metrics = Collections.synchronizedList(new ArrayList<>());
        this.events = new ArrayList<>();
        this.logs = new ArrayList<>();
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public List<InternalTransfer> getInternalTransfers() {
        return internalTransfers;
    }

    public List<Contract> getContracts() {
        return contracts;
    }

    @Override
    public ParserState getState() {
        return state;
    }

    public ParserBatch setState(ParserState state) {
        this.state = state;
        return this;
    }

    public List<TxLog> getLogs() {
        return logs;
    }

    public List<Event> getEvents() {
        return events;
    }

    public List<Metrics> getMetrics() {
        return metrics;
    }

    public boolean addBlock(Block block) {
        return blocks.add(block);
    }

    public boolean addTx(Transaction transaction) {
        return transactions.add(transaction);
    }

    public boolean addTransfers(List<InternalTransfer> transfers) {
        return internalTransfers.addAll(transfers);
    }

    public boolean addContract(Contract contract) {
        return contracts.add(contract);
    }

    public boolean addEvents(List<Event> eventsFrom) {
        return events.addAll(eventsFrom);
    }

    public boolean addMetric(Metrics metric) {
        return metrics.add(metric);
    }

    public ParserBatch setBlockChainState(ParserState blockChainState) {
        this.blockChainState = blockChainState;
        return this;
    }

    public ParserState getBlockChainState() {
        return blockChainState;
    }

    public ParserBatch setMeanStates(List<ParserState> meanStates) {
        this.meanStates = meanStates;
        return this;
    }

    public List<ParserState> getMeanStates() {
        return meanStates;
    }

    public boolean addTxLogs(List<TxLog> logs){
        return this.logs.addAll(logs);
    }
}
