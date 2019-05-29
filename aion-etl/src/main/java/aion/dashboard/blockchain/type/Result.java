package aion.dashboard.blockchain.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Result <T>{
    private final T result;
    private final int id;
    private final String jsonrpc;

    @JsonCreator
    public Result(@JsonProperty("result") T result,
                  @JsonProperty("id") int id,
                  @JsonProperty("jsonrpc") String jsonrpc) {
        this.result = result;
        this.id = id;
        this.jsonrpc = jsonrpc;
    }

    public T getResult() {
        return result;
    }

    public int getId() {
        return id;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }
}
