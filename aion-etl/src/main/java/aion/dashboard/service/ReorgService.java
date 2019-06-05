package aion.dashboard.service;

import aion.dashboard.exception.AionApiException;
import aion.dashboard.exception.DbServiceException;
import aion.dashboard.exception.ReorganizationLimitExceededException;

import java.sql.SQLException;

public interface ReorgService {

    boolean reorg() throws Exception;

    boolean performReorg(long consistentBlock) throws SQLException, AionApiException;

}
