package aion.dashboard.domainobject;

import java.util.*;

/**
 * the block details from the chain are parsed and stored in this class
 */
public class BatchObject {


    private List<Block> blocks;
    private List<Transaction> transactions;
    private ParserState parser_state;
    private List<Event> events;
    private List<Transfer> transfers;
    //---------------------------------------We only care about the first token found in a transaction list
    private Set<Token> tokens;
    //---------------------------------------These values are unique and therefore should only be added once
    private Map<String, TokenBalance> tokenBalances;
    private Map<String, Contract> contracts;
    private Map<String, Balance> balanceMap;

    public BatchObject (){
        blocks = new ArrayList<>();
        transactions = new ArrayList<>();
        tokens = new HashSet<>();
        balanceMap = new HashMap<>();
        tokenBalances = new HashMap<>();
        events = new ArrayList<>();
        contracts = new HashMap<>();
        transfers = new ArrayList<>();

    }


    public void addTransaction(Transaction tx){
        transactions.add(tx);
    }
    public void addAllTransaction(List<Transaction> txList){
        transactions.addAll(txList);
    }
    public void addBlock(Block blk){
        blocks.add(blk);
    }
    public void addToken(Token tkn){
        tokens.add(tkn);
    }

    public void addTokenBalance(TokenBalance tokenBalance) {

        if (tokenBalances.containsKey(tokenBalance.getHolderAddress().concat(tokenBalance.getContractAddress())))
            tokenBalances.replace(tokenBalance.getHolderAddress().concat(tokenBalance.getContractAddress()), tokenBalance);
        else
            tokenBalances.put(tokenBalance.getHolderAddress().concat(tokenBalance.getContractAddress()), tokenBalance);

    }

    public void addContract(Contract contract) {
        if (contracts.containsKey(contract.getContractAddr()))
            return;
        contracts.put(contract.getContractAddr(), contract);
    }

    public void addEvent(Event event) {
        events.add(event);
    }


    public void addTransfer(Transfer transfer) {
        transfers.add(transfer);
    }

    public void putBalance(Balance balance) {
        if (balanceMap.containsKey(balance.getAddress())) {
            var prev = balanceMap.get(balance.getAddress());
            balance.setTransactionId(prev.getTransactionId());
            balance.setContract(prev.getContract());
            balanceMap.replace(balance.getAddress(), balance);
        }
        else
            balanceMap.put(balance.getAddress(), balance);
    }

    public void putAllBalance(Map<String, Balance> balances) {

        balanceMap.putAll(balances);

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
        this.balanceMap.putAll(that.balanceMap);
        this.transfers.addAll(that.transfers);
        this.events.addAll(that.events);
        this.contracts.putAll(that.contracts);
        this.tokenBalances.putAll(that.tokenBalances);


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

    public List<Balance> getBalances(){
        return new ArrayList<>(balanceMap.values());
    }


    public ParserState getParser_state() {
        return parser_state;
    }

    public BatchObject setParser_state(ParserState parser_state) {
        this.parser_state = parser_state;
        return this;
    }

    public List<TokenBalance> getTokenBalances() {
        return new ArrayList<>(tokenBalances.values());
    }

    public List<Contract> getContracts() {
        return new ArrayList<>(contracts.values());
    }

    public List<Event> getEvents() {
        return events;
    }

    public List<Transfer> getTransfers() {
        return transfers;
    }
}
