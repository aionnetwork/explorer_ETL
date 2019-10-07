package aion.dashboard.consumer;


import aion.dashboard.parser.type.AbstractBatch;

import java.util.Iterator;
import java.util.List;

public abstract class WriteTask<T extends AbstractBatch> {
    private final String name;

    WriteTask(String name){
        this.name = name;
    }
    public abstract void write(T records) throws Exception;

    @Override
    public String toString() {
        return name;
    }
}
