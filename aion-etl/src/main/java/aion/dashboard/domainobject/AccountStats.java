package aion.dashboard.domainobject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccountStats {

    private final List<BigDecimal> aionIn;
    private final List<BigDecimal>  aionOut;
    private final String address;
    private final long timestamp;
    private final long blockNumber;

    public AccountStats(String address, long timestamp, long blockNumber) {
        this.timestamp = timestamp;
        this.blockNumber = blockNumber;
        this.aionIn = Collections.synchronizedList(new ArrayList<>());
        this.aionOut = Collections.synchronizedList(new ArrayList<>());
        this.address = address;
    }

    public void in(BigDecimal transferIn){
        aionIn.add(transferIn);
    }
    public void out(BigDecimal transferOut){
        aionOut.add(transferOut);
    }

    public BigDecimal getAionOut(){
        return aionOut.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getAionIn(){
        return aionIn.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String getAddress() {
        return address;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getBlockNumber() {
        return blockNumber;
    }
}
