package aion.dashboard.consumer;


import aion.dashboard.parser.type.AbstractBatch;

import java.util.Iterator;
import java.util.List;

interface WriteTask<T extends AbstractBatch> {



    void write(T records) throws Exception;
}
