package aion.dashboard.service;

import java.util.List;

public interface DBProcedureService {

    List<Long> runStoredTransactionIntegrityProcedure(long startNum);

    List<Long> runStoredBlockIntegrityProcedure(long startNum);
}
