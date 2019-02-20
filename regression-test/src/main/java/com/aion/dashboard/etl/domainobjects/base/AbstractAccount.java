package com.aion.dashboard.etl.domainobjects.base;

import java.math.BigDecimal;
import java.util.Objects;

public abstract class AbstractAccount {
    public Long lastBlockNumber;
    public BigDecimal balance;
    public String address;
    public int contract=0;

    public boolean compare(AbstractAccount account) {
        return Objects.equals(address, account.address);
    }
}
