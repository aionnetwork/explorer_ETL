package aion.dashboard.service;

import aion.dashboard.domainobject.ParserState;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public interface ParserStateService {


    boolean update(ParserState parserState);

    boolean updateHeadIntegrity(BigInteger blkNum);

    boolean updateGraphingState(BigInteger blkNum);
    boolean updateAll(List<ParserState> parserStates);
    List<ParserState> readState();
    ParserState readDBState();
    ParserState readChainState();
    ParserState readIntegrityState();

    ParserState readGraphingState();

    PreparedStatement prepare(Connection con, List<ParserState> parserStates) throws SQLException;


}
