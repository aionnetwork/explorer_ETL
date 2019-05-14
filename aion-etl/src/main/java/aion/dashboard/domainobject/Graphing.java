package aion.dashboard.domainobject;

import java.math.BigDecimal;
import java.util.Objects;

public class Graphing {


    private GraphType graphType;


    private BigDecimal value;
    private long timestamp;
    private long blockNumber;
    private String detail;

    private Graphing(BigDecimal value, GraphType graphType, Long timestamp, Long blockNumber, String detail) {

        this.value = value;
        this.graphType = graphType;
        this.timestamp = timestamp;
        this.blockNumber = blockNumber;
        this.detail = detail;
    }

    public String getGraphType() {
        return graphType.toString();
    }


    public BigDecimal getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, graphType);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getBlockNumber() {
        return blockNumber;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Graphing graphing1 = (Graphing) o;
        return Objects.equals(value, graphing1.value) &&
                Objects.equals(graphType, graphing1.graphType) &&
                Objects.equals(timestamp, graphing1.timestamp) &&
                Objects.equals(blockNumber, graphing1.blockNumber);
    }

    public String getDetail() {
        return detail;
    }

    public enum GraphType {
        ACTIVE_ADDRESS_GROWTH("Active Address Growth"),
        BLOCK_TIME("Block Time"),
        DIFFICULTY("Difficulty"),
        HASH_POWER("Hashing Power"),
        TOP_MINER("Top Miner"),
        TRANSACTION_OVER_TIME("Transactions per hour"),
        BLOCKS_MINED("Blocks Mined");

        private String type;



        GraphType(String s) {
            type = s;
        }

        @Override
        public String toString() {
            return this.type;
        }

        public static GraphType getByType(String s){
            for (GraphType type:values()){
                if (type.type.equals(s)){
                    return type;
                }
            }
            throw new IllegalArgumentException("Illegal type");
        }
    }

    @Override
    public String toString() {
        return "Graphing{" +
                "graphType=" + graphType +
                ", value=" + value +
                ", timestamp=" + timestamp +
                ", blockNumber=" + blockNumber +
                ", detail='" + detail + '\'' +
                '}';
    }

    public static class GraphingBuilder {
        private BigDecimal value;
        private GraphType graphType;
        private Long timestamp;
        private Long blockNumber;
        private String detail = "";
        

        public GraphingBuilder setValue(BigDecimal value) {
            this.value = value;
            return this;
        }

        public GraphingBuilder setGraphType(GraphType graphType) {
            this.graphType = graphType;
            return this;
        }

        public GraphingBuilder setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public GraphingBuilder setBlockNumber(Long blockNumber) {
            this.blockNumber = blockNumber;
            return this;
        }

        public GraphingBuilder setDetail(String detail) {
            this.detail = detail;
            return this;
        }

        public Graphing build() {
            return new Graphing(value, graphType, timestamp, blockNumber, detail);
        }
    }
}

