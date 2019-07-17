package aion.dashboard.blockchain;

import aion.dashboard.blockchain.interfaces.ContractHandler;
import org.aion.base.type.AionAddress;
import org.aion.util.bytes.ByteUtil;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.aion.api.ITx.NRG_LIMIT_CONTRACT_CREATE_MAX;
import static org.aion.api.ITx.NRG_PRICE_MIN;
import static org.junit.jupiter.api.Assertions.*;

class ContractHandlerTestFVM {

    ContractHandler handler = ContractHandler.getInstance();
    @Test
    void callBalanceOf(){

        Optional<BigInteger> res = handler.prepareCallForType(ContractType.DEFAULT)
                .withSignature("balanceOf(address)")
                .withNrgPrice(NRG_PRICE_MIN)
                .withNrigLimit(NRG_LIMIT_CONTRACT_CREATE_MAX)
                .withSender(AionAddress.ZERO_ADDRESS().toString())
                .withRecipient("a08e51a82d19d06fa1968ed85e9be1fa96d0db1b84b027dac3d3b6a67b4d9f66")
                .withParams(AionAddress.wrap("0xa01ebaffe0b0b0eee3928e5e27877d7465c756828369d12f046ea6716a048811"))
                .withReturnType("uint128")
                .executeFunction();
        res.ifPresent(System.out::println);
        assertNotNull(res.orElse(null));
        assertEquals(BigInteger.TEN.pow(22),res.get());
    }
    @Test
    void callTotalSupply(){

        Optional<BigInteger> res = handler.prepareCallForType(ContractType.DEFAULT)
                .withSignature("totalSupply()")
                .withNrgPrice(NRG_PRICE_MIN)
                .withNrigLimit(NRG_LIMIT_CONTRACT_CREATE_MAX)
                .withSender(AionAddress.ZERO_ADDRESS().toString())
                .withRecipient("0xa08e51a82d19d06fa1968ed85e9be1fa96d0db1b84b027dac3d3b6a67b4d9f66")
                .withParams()
                .withReturnType("uint128")
                .executeFunction();
        res.ifPresent(System.out::println);
        assertNotNull(res.orElse(null));
        assertEquals((BigInteger.TEN.pow(26)), res.get());
    }

    @Test
    void callSymbol(){

        Optional<String> res = handler.prepareCallForType(ContractType.DEFAULT)
                .withSignature("symbol()")
                .withNrgPrice(NRG_PRICE_MIN)
                .withNrigLimit(NRG_LIMIT_CONTRACT_CREATE_MAX)
                .withSender(AionAddress.ZERO_ADDRESS().toString())
                .withRecipient("0xa08e51a82d19d06fa1968ed85e9be1fa96d0db1b84b027dac3d3b6a67b4d9f66")
                .withParams()
                .withReturnType("string")
                .executeFunction();
        res.ifPresent(System.out::println);
        assertNotNull(res.orElse(null));
        assertEquals( "PLAY".toUpperCase() ,res.get());
    }
    @Test
    void callName(){

        Optional<String> res = handler.prepareCallForType(ContractType.DEFAULT)
                .withSignature("name()")
                .withNrgPrice(NRG_PRICE_MIN)
                .withNrigLimit(NRG_LIMIT_CONTRACT_CREATE_MAX)
                .withSender(AionAddress.ZERO_ADDRESS().toString())
                .withRecipient("0xa08e51a82d19d06fa1968ed85e9be1fa96d0db1b84b027dac3d3b6a67b4d9f66")
                .withParams()
                .withReturnType("string")
                .executeFunction();
        res.ifPresent(System.out::println);
        assertNotNull(res.orElse(null));
        assertEquals( "PLAY".toUpperCase() ,res.get());
    }
    @Test
    void parseName(){
        var bytes = ByteUtil.hexStringToBytes("504c4159000000000000000000000000");
        System.out.println(new String(bytes, StandardCharsets.UTF_8));

        System.out.println("00000000000000000000000000000004".length());
        System.out.println("00000000000000000000000000000010".length());
    }
}