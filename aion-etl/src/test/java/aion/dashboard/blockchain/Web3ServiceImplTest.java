package aion.dashboard.blockchain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Web3ServiceImplTest {
    Web3ServiceImpl web3Service = Web3ServiceImpl.getInstance();
    @Test
    void getTransactionReceipt() throws Exception {
        System.out.println(web3Service.getTransactionReceipt("0x12a86129c29febf28c41f918db5ee6acf5a37d64c2f4ea0d6cdd5b6ece0907ae"));
    }

    @Test
    public void getInternalTransaction() throws Exception {
        web3Service.setWeb3Providers(List.of("127.0.0.1:8545"));
        System.out.println(web3Service.getInternalTransaction("0xe570e0407083da0ade69c9c8f30e44bf2d5ee0d28b7d9873b9009731a9e61317"));
    }
}