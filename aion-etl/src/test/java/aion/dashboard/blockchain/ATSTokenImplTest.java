package aion.dashboard.blockchain;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

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
}