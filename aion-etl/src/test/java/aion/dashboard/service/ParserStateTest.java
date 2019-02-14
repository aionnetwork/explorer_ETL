package aion.dashboard.service;

import aion.dashboard.domainobject.ParserState;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ParserStateTest {

    public void readState(){
        ParserStateService service = ParserStateServiceImpl.getInstance();
        List<ParserState> res = service.readState();

        assertFalse(res.isEmpty());

        for (var state: res) {
            System.out.println(String.format("ID =[%d], Block_num=[%d], tx_num=[%d]", state.getId(),
                    state.getBlockNumber().longValue(), state.getTransactionID().longValue()));
        }


    }


    public void updateState(){

        ParserStateService service = ParserStateServiceImpl.getInstance();
        ParserState.ParserStateBuilder builder= new ParserState.ParserStateBuilder();
        ParserState test_State =  builder.id(1).blockNumber(BigInteger.valueOf(5)).transactionID(BigInteger.valueOf(5)).build();

        List<ParserState> updates = List.of(
                builder.id(1).blockNumber(BigInteger.valueOf(5)).transactionID(BigInteger.valueOf(5)).build(),
                builder.id(2).blockNumber(BigInteger.valueOf(5)).transactionID(BigInteger.valueOf(5)).build(),
                builder.id(3).blockNumber(BigInteger.valueOf(5)).transactionID(BigInteger.valueOf(5)).build()
        );

        assertTrue(service.updateAll(updates));
        assertEquals(test_State.getBlockNumber(),service.readDBState().getBlockNumber());
        assertEquals(test_State.getId(),service.readDBState().getId());
        assertEquals(test_State.getTransactionID(),service.readDBState().getTransactionID());

    }
}
