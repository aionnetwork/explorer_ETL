package aion.dashboard.parser.events;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AVMABIDefinitionsTest {

    @Test
    void fromLine() {
        var res = AVMABIDefinitions.fromLine("TokenSent(Address to,Address indexed from,uint128 amount)");
        var expected = new AVMABIDefinitions.AVMEventSignature(
                List.of(new AVMABIDefinitions.Parameter("Address", "from", true, 1)),
                List.of(new AVMABIDefinitions.Parameter("Address", "to", false, 0), new AVMABIDefinitions.Parameter("uint128", "amount", true, 2)),
                AVMABIDefinitions.hashstr("TokenSent"),
                "TokenSent(Address to,Address indexed from,uint128 amount)",
                "TokenSent");

        assertEquals(expected.toString(), res.toString());

        System.out.println(expected);
    }

    @Test
    void fromFile() {
        var res = AVMABIDefinitions.fromFile("contracts/avm/ATS.avm");

        assertTrue(res.isPresent());
        assertFalse(res.get().isEmpty());
    }

    @Test
    void testHash(){

        assertEquals("546f6b656e53656e740000000000000000000000000000000000000000000000", AVMABIDefinitions.hashstr("TokenSent") );

    }

    @Test
    void lsEvents(){
        var res=AVMABIDefinitions.eventsFromAllFiles();
        assertFalse(res.isEmpty());
        res.entrySet().forEach(System.out::println);
    }

    @Test
    void getFiles(){

        var res = AVMABIDefinitions.fileNamesFor("contracts/avm/");
        assertFalse(res.isEmpty());
        res.forEach(System.out::println);
    }

    @Test
    void getSignatureMap(){
        var map = AVMABIDefinitions.getInstance().getAvmSignatureMap();
        assertNotNull(map);
        assertFalse(map.isEmpty());
    }
}