package aion.dashboard.blockchain.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MethodBody<T>{
    private String method;
    private T[] params;
    private int id;

    @JsonCreator
    public MethodBody(@JsonProperty("method") String method,
                      @JsonProperty("params") T[] params,
                      @JsonProperty("id") int id) {
        this.method = method;
        this.params = params;
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public T[] getParams() {
        return params;
    }

    public int getId() {
        return id;
    }
}
