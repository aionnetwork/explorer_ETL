package aion.dashboard.domainobjects;

public class ParserState {
    Integer id;
    public Long blockNumber;
    public Long transactionId;

    public ParserState(Integer id, Long blockNumber, Long transactionId) {
        this.id = id;
        this.blockNumber = blockNumber;
        this.transactionId = transactionId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getBlockNumber() {
        return blockNumber;
    }

    public Long getTransactionId() {
        return transactionId;
    }
}