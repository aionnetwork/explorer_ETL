package aion.dashboard.domainobject;

import java.util.*;

/**
 * the block details from the chain are parsed and stored in this class
 */
public class BatchObject {


    private List<Graphing> graphings;
    private List<Block> blocks;
    private List<Transaction> transactions;
    private List<ParserState> parserState;
    private List<Event> events;
    private List<TokenTransfers> tokenTransfers;
    private List<Metrics> metrics;
    private Set<InternalTransfer> internalTransfers;
    //---------------------------------------We only care about the first token found in a transaction list
    private Set<Token> tokens;
    //---------------------------------------These values are unique and therefore should only be added once
    private Map<String, TokenHolders> tokenBalances;
    private Map<String, Contract> contracts;
    private Map<String, Account> accountMap;


    public BatchObject (){
        blocks = Collections.synchronizedList(new ArrayList<>());
        transactions = Collections.synchronizedList(new ArrayList<>());
        tokens = Collections.synchronizedSet(new HashSet<>());
        accountMap = Collections.synchronizedMap(new HashMap<>());
        tokenBalances = Collections.synchronizedMap(new HashMap<>());
        events = Collections.synchronizedList(new ArrayList<>());
        contracts = Collections.synchronizedMap(new HashMap<>());
        tokenTransfers = Collections.synchronizedList(new ArrayList<>());
        graphings = Collections.synchronizedList(new ArrayList<>());
        parserState = Collections.synchronizedList(new ArrayList<>());
        metrics = Collections.synchronizedList(new ArrayList<>());
        internalTransfers = Collections.synchronizedSet(new HashSet<>());
    }


    public void addMetric(Metrics metric){metrics.add(metric);}
    public void addTransaction(Transaction tx){
        transactions.add(tx);
    }

    public void addInternalTransfer(InternalTransfer transfer){
        internalTransfers.add(transfer);
    }
    public void addBlock(Block blk){
        blocks.add(blk);
    }
    public void addToken(Token tkn){
        tokens.add(tkn);
    }

    public synchronized void addAllGraphing(List<Graphing> graphings){
        this.graphings.addAll(graphings);
    }

    public List<Graphing> getGraphings() {
        return graphings;
    }

    public void addTokenBalance(TokenHolders tokenHolders) {

        if (tokenBalances.containsKey(tokenHolders.getHolderAddress().concat(tokenHolders.getContractAddress())))
            tokenBalances.replace(tokenHolders.getHolderAddress().concat(tokenHolders.getContractAddress()), tokenHolders);
        else
            tokenBalances.put(tokenHolders.getHolderAddress().concat(tokenHolders.getContractAddress()), tokenHolders);

    }

    public void addContract(Contract contract) {
        if (contracts.containsKey(contract.getContractAddr()))
            return;
        contracts.put(contract.getContractAddr(), contract);
    }

    public void addEvent(Event event) {
        events.add(event);
    }


    public void addTransfer(TokenTransfers tokenTransfers) {
        this.tokenTransfers.add(tokenTransfers);
    }

    public void putAccount(Account account) {
        if (accountMap.containsKey(account.getAddress())) {
            var prev = accountMap.get(account.getAddress());
            account.setTransactionHash(prev.getTransactionHash());
            account.setContract(prev.getContract());
            accountMap.replace(account.getAddress(), account);
        }
        else
            accountMap.put(account.getAddress(), account);
    }


    /**
     * Merges this batch object with another
     * @param that the batch object to merge
     *
     */
    public void mergeBatch(BatchObject that){

        this.blocks.addAll(that.blocks);
        this.transactions.addAll(that.transactions);
        this.tokens.addAll(that.tokens);
        this.accountMap.putAll(that.accountMap);
        this.tokenTransfers.addAll(that.tokenTransfers);
        this.events.addAll(that.events);
        this.contracts.putAll(that.contracts);
        this.tokenBalances.putAll(that.tokenBalances);
        this.graphings.addAll(that.graphings);
        this.internalTransfers.addAll(that.internalTransfers);


    }


    public List<InternalTransfer> getInternalTransfers(){
        return new ArrayList<>(internalTransfers);
    }
    public List<Block> getBlocks() {
        return blocks;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public List<Token> getTokens() {
        return new ArrayList<>(tokens);
    }

    public Set<Token> getTokenSet() {
        return tokens;
    }

    public List<Account> getAccounts(){
        return new ArrayList<>(accountMap.values());
    }


    public List<ParserState> getParserState() {
        return parserState;
    }

    public BatchObject addParserState(ParserState parserState) {
        this.parserState.add( parserState);
        return this;
    }

    public List<TokenHolders> getTokenBalances() {
        return new ArrayList<>(tokenBalances.values());
    }

    public List<Contract> getContracts() {
        return new ArrayList<>(contracts.values());
    }

    public List<Event> getEvents() {
        return events;
    }

    public List<TokenTransfers> getTokenTransfers() {
        return tokenTransfers;
    }

    public List<Metrics> getMetrics(){return metrics;}
}
