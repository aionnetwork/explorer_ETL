package aion.dashboard.service;

import aion.dashboard.domainobject.ParserState;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface ParserStateService {


    boolean update(ParserState parser_state);
    boolean updateHeadBlockChain(BigInteger blkNum);
    boolean updateHeadDataBase(BigInteger blkNum, BigInteger txNum);
    boolean updateHeadIntegrity(BigInteger blkNum);
    boolean updateBlockMeanState(BigInteger blkNum);
    boolean updateGraphingState(BigInteger blkNum);
    boolean updateAll(List<ParserState> parser_stateList);
    List<ParserState> readState();
    ParserState readDBState();
    ParserState readChainState();
    ParserState readIntegrityState();

    ParserState readGraphingState();
    ParserState readBlockMeanState();
    ParserState readTransactionMeanState();
    ParserState readMinerInfoState();

    PreparedStatement prepare(Connection con, List<ParserState> parser_stateList) throws SQLException;


}
