package aion.dashboard.task;

import aion.dashboard.domainobject.BatchObject;
import aion.dashboard.domainobject.ParserState;

public interface WriteTask {


    boolean executeTask(BatchObject batchObject, ParserState chainState);
}
