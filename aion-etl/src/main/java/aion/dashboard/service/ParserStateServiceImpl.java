package aion.dashboard.service;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.db.DbQuery;
import aion.dashboard.domainobject.ParserState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

public class ParserStateServiceImpl implements ParserStateService {


    private static final ParserStateServiceImpl PARSER_STATE_SERVICE = new ParserStateServiceImpl();

    public static final int DB_ID = 1;
    public static final int BLKCHAIN_ID =2;
    public static final int INTEGRITY_ID =3;
    public static final int GRAPHING_ID = 4;
    public static ParserStateServiceImpl getInstance() {
        return PARSER_STATE_SERVICE;
    }

    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general") ;
    @Override
    public boolean update(ParserState parserState) {
        return updateAll(Collections.singletonList(parserState));
    }


    @Override
    public boolean updateHeadIntegrity(BigInteger blkNum) {
        return update(new ParserState.ParserStateBuilder().id(INTEGRITY_ID).blockNumber(blkNum).transactionID(BigInteger.valueOf(-1)).build());
    }

    @Override
    public boolean updateGraphingState(BigInteger blkNum) {
        return update(new ParserState.ParserStateBuilder().id(GRAPHING_ID).blockNumber(blkNum).transactionID(BigInteger.valueOf(-1)).build());
    }

    @Override
    public boolean updateAll(List<ParserState> parserStates) {

        try(Connection con = DbConnectionPool.getConnection();
            PreparedStatement ps=prepare(con, parserStates)
        ){
            try {
                ps.executeBatch();
                con.commit();

            }catch (SQLException e){
                con.rollback();
            }

            return true;
        }
        catch (SQLException e) {
            GENERAL.debug("Caught exception in ParserStateService save ", e);
            return false;
        }
    }

    @Override
    public List<ParserState> readState() {
        List<ParserState> parserStates = new ArrayList<>();
        try( Connection con = DbConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(DbQuery.GET_PARSER_STATE);
             ResultSet resultSet = ps.executeQuery()) {
            ParserState.ParserStateBuilder builder = new ParserState.ParserStateBuilder();
            while (resultSet.next()){
                builder.id(resultSet.getLong(1))
                        .blockNumber(BigInteger.valueOf(resultSet.getLong(2)))
                        .transactionID(BigInteger.valueOf(resultSet.getLong(3)));
                parserStates.add(builder.build());
            }

            return parserStates;

        } catch (SQLException e) {
            GENERAL.debug("Caught exception in ParserStateService update ", e);

            return List.of();
        }



    }

    @Override
    public ParserState readDBState() {

        return readState().stream()
                .filter(parserState -> parserState.getId() == DB_ID)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Could not find DB State in parser state"));
    }

    @Override
    public ParserState readChainState() {
        return readState().stream()
                .filter(parserState -> parserState.getId() == BLKCHAIN_ID)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Could not find BlockChain State in parser state"));
    }

    @Override
    public ParserState readIntegrityState() {
        return readState().stream()
                .filter(parserState -> parserState.getId() == INTEGRITY_ID)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Could not find Integrity State in parser state"));
    }

    @Override
    public ParserState readGraphingState() {
        return readState().stream()
                .filter(parserState -> parserState.getId() == GRAPHING_ID)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Could not find Graphing State in parser state"));
    }

    @Override
    public PreparedStatement prepare(Connection con, List<ParserState> parserStates) throws SQLException {
        PreparedStatement ps=con.prepareStatement(DbQuery.UPDATE_PARSER_STATE);
        for (var parser_state: parserStates) {

            ps.setLong(1, parser_state.getBlockNumber().longValue());
            ps.setLong(2, parser_state.getTransactionID().longValue());
            ps.setLong(3, parser_state.getId());


            ps.addBatch();
        }

        return ps;
    }
}
