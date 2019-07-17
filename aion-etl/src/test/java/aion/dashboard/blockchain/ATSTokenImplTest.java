package aion.dashboard.blockchain;

import aion.dashboard.domainobject.Contract;
import aion.dashboard.domainobject.Token;
import aion.dashboard.exception.AionApiException;
import aion.dashboard.util.Utils;
import org.aion.api.type.BlockDetails;
import org.aion.api.type.TxDetails;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ATSTokenImplTest {

    private String avmContract2="0xa0F0d2ACBbC64c13471a1FF89D04c05F2dc15A6684ab4C272D0AdFA2d59d55dC";
    private String fvmContract = "0xa08e51a82d19d06fa1968ed85e9be1fa96d0db1b84b027dac3d3b6a67b4d9f66";

    private ATSTokenImpl avmToken = new ATSTokenImpl(avmContract2, ContractType.AVM);
    private ATSTokenImpl fvmToken = new ATSTokenImpl(fvmContract, ContractType.DEFAULT);

    @Test
    void getBalance() {
        System.out.println(fvmToken.getBalance("0xa01ebaffe0b0b0eee3928e5e27877d7465c756828369d12f046ea6716a048811"));
        System.out.println(avmToken.getBalance("0xa048630fff033d214b36879e62231cc77d81f45d348f6590d268b9b8cabb88a9"));
    }

    @Test
    void getGranularity() {
        System.out.println(fvmToken.getGranularity());
        System.out.println(avmToken.getGranularity());

    }

    @Test
    void getName() {
        System.out.println(fvmToken.getName());
        System.out.println(avmToken.getName());

    }

    @Test
    void getSymbol() {
        System.out.println(fvmToken.getSymbol());
        System.out.println(avmToken.getSymbol());
    }

    @Test
    void getTotalSupply() {
        System.out.println(fvmToken.getTotalSupply());
        System.out.println(avmToken.getTotalSupply());
    }

    @Test
    void getLiquidSupply() {
        System.out.println(fvmToken.getLiquidSupply());
        System.out.println(avmToken.getLiquidSupply());
    }


    @Test
    void getTokenDetailsFVM() throws AionApiException {
        AionService service = AionService.getInstance();

        BlockDetails blockDetails = service.getBlockDetailsByRange(1129737L,1129737L).stream().findFirst().orElseThrow();
        TxDetails deployedTx = blockDetails.getTxDetails().stream().filter(tx-> Utils.sanitizeHex(tx.getTxHash().toString()).equalsIgnoreCase("f42947d0c5f1533bd3f0c99d5b616f2d5283b1f61bdbc6ff4ecd42d642e757df")).findFirst().orElseThrow();

        ATSTokenImpl token = new ATSTokenImpl(deployedTx.getContract().toString(), ContractType.fromByte(deployedTx.getType()));
        Contract contract = Contract.from(blockDetails, deployedTx);
        Optional<Token> dbToken = assertDoesNotThrow(()-> token.getDetails(contract));
        assertNotNull(dbToken.orElse(null));
        System.out.println(dbToken.toString());
    }

    @Test
    void getTokenDetailsAVM() throws AionApiException {
        AionService service = AionService.getInstance();

        BlockDetails blockDetails = service.getBlockDetailsByRange(2586933,2586933).stream().findFirst().orElseThrow();
        TxDetails deployedTx = blockDetails.getTxDetails().stream().filter(tx-> Utils.sanitizeHex(tx.getTxHash().toString()).equalsIgnoreCase("bd439e1a6b333b93ae38fdaf9dde70618caf7d79c28902c63b32658e34ff6e4e")).findFirst().orElseThrow();

        ATSTokenImpl token = new ATSTokenImpl(deployedTx.getContract().toString(), ContractType.fromByte(deployedTx.getType()));
        Contract contract = Contract.from(blockDetails, deployedTx);
        Optional<Token> dbToken = assertDoesNotThrow(()-> token.getDetails(contract));
        System.out.println(dbToken.toString());
        assertNotNull(dbToken.orElse(null));


    }
}