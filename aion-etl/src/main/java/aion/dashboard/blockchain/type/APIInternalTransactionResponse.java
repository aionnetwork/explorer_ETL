package aion.dashboard.blockchain.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class APIInternalTransactionResponse {

    private List<APIInternalTransaction> internalTransactions;
    private String hash;


    @JsonCreator
    public APIInternalTransactionResponse(@JsonProperty("internal_transactions") APIInternalTransaction[] internalTransactions,
                                          @JsonProperty("hash") String hash) {
        this.internalTransactions = internalTransactions == null ? Collections.emptyList(): Arrays.asList(internalTransactions);
        this.hash = hash;
    }

    public List<APIInternalTransaction> getInternalTransactions() {
        return internalTransactions;
    }

    public String getHash() {
        return hash;
    }
}
