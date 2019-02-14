package aion.dashboard.domainobjects;

public class TransactionMap {

    String transactionHash;
    Long id;

    public TransactionMap(String transactionHash, Long id) {
        this.transactionHash = transactionHash;
        this.id = id;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
