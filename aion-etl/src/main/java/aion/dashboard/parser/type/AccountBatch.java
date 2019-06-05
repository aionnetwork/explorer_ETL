package aion.dashboard.parser.type;

import aion.dashboard.domainobject.Account;
import aion.dashboard.domainobject.ParserState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccountBatch extends AbstractBatch {
    private List<Account> accounts;
    private ParserState parserState;

    public AccountBatch(){
        accounts= Collections.synchronizedList(new ArrayList<>());
        parserState=null;
    }

    public AccountBatch setState(ParserState parserState) {
        this.parserState = parserState;
        return this;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    @Override
    public ParserState getState() {
        return parserState;
    }

    public boolean addAccount(Account account){
        return accounts.add(account);
    }
}
