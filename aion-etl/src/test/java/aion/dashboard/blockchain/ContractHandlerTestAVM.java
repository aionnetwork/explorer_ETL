package aion.dashboard.blockchain;

import aion.dashboard.blockchain.interfaces.ContractHandler;
import org.aion.base.type.AionAddress;
import org.junit.jupiter.api.Test;

import javax.swing.text.html.Option;
import java.math.BigInteger;
import java.util.Optional;

import static org.aion.api.ITx.*;
import static org.junit.jupiter.api.Assertions.*;

class ContractHandlerTestAVM {

    private ContractHandler handler = ContractHandlerImpl.getInstance();
    @Test
    void callBalanceOf(){

        Optional<BigInteger> res = handler.prepareCallForType(ContractType.AVM)
                .withSignature("balanceOf")
                .withNrgPrice(NRG_PRICE_MIN)
                .withNrigLimit(NRG_LIMIT_CONTRACT_CREATE_MAX)
                .withSender(AionAddress.ZERO_ADDRESS().toString())
                .withRecipient("0xA01d351d3D0971EA31C19301A835866998B8CE1f9E40153bd0BA5421519B8b02")
                .withParams(AionAddress.wrap("0xa048630fff033d214b36879e62231cc77d81f45d348f6590d268b9b8cabb88a9"))
                .withReturnType("BigInteger")
                .executeFunction();
        assertNotNull(res.orElse(null));
        assertEquals(res.get(), new BigInteger("333333333000000000000000000"));
    }
    @Test
    void callTotalSupply(){

        Optional<BigInteger> res = handler.prepareCallForType(ContractType.AVM)
                .withSignature("totalSupply")
                .withNrgPrice(NRG_PRICE_MIN)
                .withNrigLimit(NRG_LIMIT_CONTRACT_CREATE_MAX)
                .withSender(AionAddress.ZERO_ADDRESS().toString())
                .withRecipient("0xA01d351d3D0971EA31C19301A835866998B8CE1f9E40153bd0BA5421519B8b02")
                .withParams()
                .withReturnType("BigInteger")
                .executeFunction();
        assertNotNull(res.orElse(null));
        assertEquals(res.get(), new BigInteger("333333333000000000000000000"));
    }

    @Test
    void callName(){

        Optional<String> res = handler.prepareCallForType(ContractType.AVM)
                .withSignature("symbol")
                .withNrgPrice(NRG_PRICE_MIN)
                .withNrigLimit(NRG_LIMIT_CONTRACT_CREATE_MAX)
                .withSender(AionAddress.ZERO_ADDRESS().toString())
                .withRecipient("0xA01d351d3D0971EA31C19301A835866998B8CE1f9E40153bd0BA5421519B8b02")
                .withParams()
                .withReturnType("String")
                .executeFunction();
        assertNotNull(res.orElse(null));
        assertEquals( "J3N".toUpperCase() ,res.get());
    }

}