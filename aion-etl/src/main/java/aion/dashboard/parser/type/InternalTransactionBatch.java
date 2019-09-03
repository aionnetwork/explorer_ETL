package aion.dashboard.parser.type;

import aion.dashboard.domainobject.Contract;
import aion.dashboard.domainobject.InternalTransaction;
import aion.dashboard.domainobject.ParserState;

import java.util.ArrayList;
import java.util.List;

public class InternalTransactionBatch extends AbstractBatch {
    private List<InternalTransaction> internalTransactions = new ArrayList<>();
    private ParserState parserState;
    private List<Contract> contracts = new ArrayList<>();

    @Override
    public ParserState getState() {
        return parserState;
    }

    public void setParserState(ParserState parserState) {
        this.parserState = parserState;
    }

    public List<InternalTransaction> getInternalTransactions() {
        return internalTransactions;
    }

    public boolean addInternalTransaction(InternalTransaction internalTransaction){
        return this.internalTransactions.add(internalTransaction);
    }

    public boolean addContract(Contract contract){
        return this.contracts.add(contract);
    }

    public List<Contract> getContracts() {
        return contracts;
    }
}
