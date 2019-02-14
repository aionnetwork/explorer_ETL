package aion.dashboard.domainobject;

import java.math.BigInteger;
import java.util.Objects;

public class ParserState {

    private long id;
    private BigInteger blockNumber;
    private BigInteger transactionID;

    public ParserState(long id, BigInteger blockNumber, BigInteger transactionID) {
        this.id = id;
        this.blockNumber = blockNumber;
        this.transactionID = transactionID;
    }

    public long getId() {
        return id;
    }

    public BigInteger getBlockNumber() {
        return blockNumber;
    }

    public BigInteger getTransactionID() {
        return transactionID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParserState)) return false;
        ParserState that = (ParserState) o;
        return getId() == that.getId() &&
                Objects.equals(getBlockNumber(), that.getBlockNumber()) &&
                Objects.equals(getTransactionID(), that.getTransactionID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getBlockNumber(), getTransactionID());
    }

    public static class ParserStateBuilder {

        private long id;
        private BigInteger blockNumber;
        private BigInteger transactionID;


        public ParserStateBuilder id(long id) {
            this.id = id;
            return this;
        }

        public ParserStateBuilder blockNumber(BigInteger blockNumber) {
            this.blockNumber = blockNumber;
            return this;
        }

        public ParserStateBuilder transactionID(BigInteger transactionID) {
            this.transactionID = transactionID;
            return this;
        }


        public ParserState build(){
            return new ParserState(id, blockNumber, transactionID);
        }
    }
}
