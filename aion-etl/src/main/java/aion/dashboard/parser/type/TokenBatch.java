package aion.dashboard.parser.type;

import aion.dashboard.domainobject.ParserState;
import aion.dashboard.domainobject.Token;
import aion.dashboard.domainobject.TokenHolders;
import aion.dashboard.domainobject.TokenTransfers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TokenBatch extends AbstractBatch{

    private Set<Token> tokens;
    private Set<TokenTransfers> transfers;
    private List<TokenHolders> tokenHolders;
    private ParserState parserState;

    public TokenBatch(){
        tokens= new HashSet<>();
        transfers = new HashSet<>();
        tokenHolders = new ArrayList<>();
    }

    public List<Token> getTokens() {
        return new ArrayList<>(tokens);
    }

    public List<TokenTransfers> getTransfers() {
        return new ArrayList<>(transfers);
    }

    public List<TokenHolders> getTokenHolders() {
        return tokenHolders;
    }

    @Override
    public ParserState getState() {
        return parserState;
    }

    public TokenBatch setState(ParserState parserState) {
        this.parserState = parserState;
        return this;
    }

    public boolean addToken(Token token){
        return this.tokens.add(token);
    }

    public boolean addHolders(List<TokenHolders> holders){
        return this.tokenHolders.addAll(holders);
    }

    public boolean addTransfers(List<TokenTransfers> transfers){
        return this.transfers.addAll(transfers);
    }

    public TokenBatch merge(TokenBatch that){


        TokenBatch batch = new TokenBatch();

        batch.tokenHolders.addAll(this.tokenHolders);
        batch.tokenHolders.addAll(that.tokenHolders);

        batch.transfers.addAll(this.transfers);
        batch.transfers.addAll(that.transfers);

        batch.tokens.addAll(this.tokens);
        batch.tokens.addAll(that.tokens);

        return batch;
    }
}
