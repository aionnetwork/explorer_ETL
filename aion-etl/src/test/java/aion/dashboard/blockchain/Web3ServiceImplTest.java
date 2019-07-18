package aion.dashboard.blockchain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Web3ServiceImplTest {
    Web3ServiceImpl web3Service = Web3ServiceImpl.getInstance();
    @Test
    void getTransactionReceipt() throws Exception {
        System.out.println(web3Service.getTransactionReceipt("0x12a86129c29febf28c41f918db5ee6acf5a37d64c2f4ea0d6cdd5b6ece0907ae"));
    }
}