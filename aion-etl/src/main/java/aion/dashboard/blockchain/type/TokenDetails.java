package aion.dashboard.blockchain.type;

import java.math.BigInteger;

public class TokenDetails {


    private final BigInteger totalSupply;
    private final BigInteger liquidSupply;
    private final String name;
    private final String symbol;
    private final int granularity;

    public TokenDetails(BigInteger totalSupply, BigInteger liquidSupply, String name, String symbol, int granularity) {
        this.totalSupply = totalSupply;
        this.liquidSupply = liquidSupply;
        this.name = name;
        this.symbol = symbol;
        this.granularity = granularity;
    }
}
