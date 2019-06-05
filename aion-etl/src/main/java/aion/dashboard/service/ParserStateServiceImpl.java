package aion.dashboard.service;

import aion.dashboard.db.DbConnectionPool;
import aion.dashboard.db.DbQuery;
import aion.dashboard.domainobject.ParserState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ParserStateServiceImpl implements ParserStateService {


    private static final ParserStateServiceImpl PARSER_STATE_SERVICE = new ParserStateServiceImpl();

    public static final  int DB_ID = 1;
    public static final  int BLKCHAIN_ID =2;
    public static final  int INTEGRITY_ID =3;
    public static final  int GRAPHING_ID = 4;
    public static final int BLOCK_MEAN_ID = 5;
    public static final int TRANSACTION_MEAN_ID =6;
    public static final int ACCOUNT_ID=7;
    public static final int TOKEN_ID=8;
    public static ParserStateServiceImpl getInstance() {
        return PARSER_STATE_SERVICE;
    }

    private static final Logger GENERAL = LoggerFactory.getLogger("logger_general") ;
    @Override
    public boolean update(ParserState parserState) {


        try(var con = DbConnectionPool.getConnection();
            var ps = con.prepareStatement(DbQuery.UpdateParserState)){
            try {
                ps.setLong(1, parserState.getBlockNumber().longValue());
                ps.setLong(2, parserState.getId());


                ps.execute();
                con.commit();
            }
            catch (SQLException e){
                con.rollback();
                throw e;
            }

        }
        catch (SQLException e) {
            GENERAL.debug("Caught exception in ParserStateService save ", e);
            return false;
        }

        return true;
    }

    @Override
    public boolean updateHeadBlockChain(BigInteger blkNum) {
        return update(new ParserState.ParserStateBuilder()
                .id(BLKCHAIN_ID)
                .blockNumber(blkNum)
                .build());
    }

    @Override
    public boolean updateHeadDataBase(BigInteger blkNum, BigInteger txNum) {
        return update(new ParserState.ParserStateBuilder()
                .id(DB_ID)
                .blockNumber(blkNum)
                .build());
    }

    @Override
    public boolean updateHeadIntegrity(BigInteger blkNum) {
        return update(new ParserState.ParserStateBuilder()
                .id(INTEGRITY_ID)
                .blockNumber(blkNum)
                .build());
    }

    @Override
    public boolean updateGraphingState(BigInteger blkNum) {
        return update(new ParserState.ParserStateBuilder()
                .id(GRAPHING_ID)
                .blockNumber(blkNum)
                .build());
    }

    @Override
    public boolean updateBlockMeanState(BigInteger blkNum) {
        return update(new ParserState.ParserStateBuilder()
                .id(BLOCK_MEAN_ID)
                .blockNumber(blkNum)
                .build());
    }




    @Override
    public boolean updateAll(List<ParserState> parserStates) {

        Connection con = null;
        PreparedStatement ps=null;

        try{
            con = DbConnectionPool.getConnection();
            ps=con.prepareStatement(DbQuery.UpdateParserState);
            for (var parser_state: parserStates) {

                ps.setLong(1, parser_state.getBlockNumber().longValue());
                ps.setLong(2, parser_state.getId());


                ps.execute();
            }
            con.commit();


        }
        catch (SQLException e) {
            try {
                Objects.requireNonNull(con).rollback();
                
            } catch (SQLException | NullPointerException ignored ) {

            }
            GENERAL.debug("Caught exception in ParserStateService save ", e);
            return false;
        }
        finally {
            try {

                Objects.requireNonNull(ps).close();
                Objects.requireNonNull(con).close();
            } catch (SQLException |NullPointerException e) {
                GENERAL.debug("Caught exception in ParserStateService save ", e);
            }
        }


        return true;

    }

    @Override
    public List<ParserState> readState() {
        List<ParserState> parserStates = new ArrayList<>();
        try (var con = DbConnectionPool.getConnection();
             var ps = con.prepareStatement(DbQuery.GetParserState);
             var resultSet = ps.executeQuery()){

            ParserState.ParserStateBuilder builder = new ParserState.ParserStateBuilder();
            while (resultSet.next()) {
                builder
                        .id(resultSet.getLong(1))
                        .blockNumber(BigInteger.valueOf(resultSet.getLong(2)));
                parserStates.add(builder.build());
            }


        } catch (SQLException e) {
            GENERAL.debug("Caught exception in ParserStateService update ", e);


            return List.of();
        }




        return parserStates;
    }

    @Override
    public ParserState readDBState() {

        return readState().stream().filter(parserState -> parserState.getId() == DB_ID).findFirst().orElseThrow();
    }

    @Override
    public ParserState readChainState() {
        return readState().stream().filter(parserState -> parserState.getId() == BLKCHAIN_ID).findFirst().orElseThrow();
    }

    @Override
    public ParserState readIntegrityState() {
        return readState().stream().filter(parserState -> parserState.getId() == INTEGRITY_ID).findFirst().orElseThrow();
    }

    @Override
    public ParserState readGraphingState() {
        return readState().stream().filter(parserState -> parserState.getId() == GRAPHING_ID).findFirst().orElseThrow();
    }

    @Override
    public ParserState readBlockMeanState() {
        return readState().stream().filter(parserState -> parserState.getId() == BLOCK_MEAN_ID).findFirst().orElseThrow();

    }

    @Override
    public ParserState readTransactionMeanState() {
        return readState().stream().filter(parserState -> parserState.getId() == TRANSACTION_MEAN_ID).findFirst().orElseThrow();
    }

    @Override
    public PreparedStatement prepare(Connection con, List<ParserState> parserStates) throws SQLException {
        PreparedStatement ps=con.prepareStatement(DbQuery.UpdateParserState);// IGNORE squid:S2095 here the resource is closed by
        for (var parser_state: parserStates) {

            ps.setLong(1, parser_state.getBlockNumber().longValue());
            ps.setLong(2, parser_state.getId());


            ps.addBatch();
        }

        return ps;
    }
}
