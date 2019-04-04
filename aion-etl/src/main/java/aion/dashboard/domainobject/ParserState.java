package aion.dashboard.domainobject;

import java.math.BigInteger;
import java.util.Objects;

public class                                                                                                                                                                                                        ParserState {

    private long id;
    private BigInteger blockNumber;

    public ParserState(long id, BigInteger blockNumber) {
        this.id = id;
        this.blockNumber = blockNumber;
    }

    public long getId() {
        return id;
    }

    public BigInteger getBlockNumber() {
        return blockNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParserState)) return false;
        ParserState that = (ParserState) o;
        return getId() == that.getId() &&
                Objects.equals(getBlockNumber(), that.getBlockNumber());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getBlockNumber());
    }

    public static class ParserStateBuilder {

        private long id;
        private BigInteger blockNumber;



        public ParserStateBuilder id(long id) {
            this.id = id;
            return this;
        }

        public ParserStateBuilder blockNumber(BigInteger blockNumber) {
            this.blockNumber = blockNumber;
            return this;
        }


        public ParserState build(){
            return new ParserState(id, blockNumber);
        }
    }
}
