package aion.dashboard.blockchain;

import aion.dashboard.blockchain.type.APIBlockDetails;
import aion.dashboard.config.Config;
import aion.dashboard.exception.AionApiException;
import aion.dashboard.exception.Web3ApiException;
import aion.dashboard.util.Utils;
import com.google.common.base.Stopwatch;
import org.aion.api.type.BlockDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//TODO add the web3 provider as part of the testing environment
class Web3ServiceImplTest {
    Web3ServiceImpl web3Service = Web3ServiceImpl.getInstance();
    Logger test_logger = LoggerFactory.getLogger("logger_test");
    @BeforeEach
    void setup(){

    }

    @Test
    void getTransactionReceipt() throws Exception {
        System.out.println(web3Service.getTransactionReceipt("32bc0cafd5a05a6e7efa80a61441f5b41d4e8e139a1ad84f5d226e56964299b0"));
    }

    @Test
    public void getInternalTransaction() throws Exception {
        System.out.println(web3Service.getInternalTransaction("0xe570e0407083da0ade69c9c8f30e44bf2d5ee0d28b7d9873b9009731a9e61317"));
    }
    @Test
    public void getAPIBLockDetail() throws Exception {
        assertAll(
                this::getGenesisBlock,
                this::getFirstBlockMined,
                this::getBlockWithOneTx,
                this::getBlockWithTxLogs,
                this::getBlockWithInternalTx
        );
    }

    @Test
    public void getAPIBlock() throws Web3ApiException {
        web3Service.getBlock(1);
    }

    private void getGenesisBlock() throws Web3ApiException {
        long genesis = 0;
        var block = web3Service.getBlockDetails(genesis);
        assertTrue(block.getTxDetails().isEmpty(), "Found transactions in genesisBlock");
    }
    private void getFirstBlockMined() throws Web3ApiException{
        long firstMined = 1;
        var block =web3Service.getBlockDetails(firstMined);
        assertTrue(block.getTxDetails().isEmpty(), "Found transactions in a known empty block");
        assertEquals(0,block.getNumTransactions());
    }
    private void getBlockWithOneTx() throws Web3ApiException {
        long blockWithOneTx = 6753;
        var block =web3Service.getBlockDetails(blockWithOneTx);
        assertEquals(block.getNumTransactions(), block.getTxDetails().size());
        assertEquals(1, block.getNumTransactions());
    }

    private void getBlockWithTxLogs() throws Web3ApiException {
        long blockWithOneTx = 1088454;
        var block = web3Service.getBlockDetails(blockWithOneTx);
        assertEquals(block.getNumTransactions(), block.getTxDetails().size());
        for (var tx: block.getTxDetails()){
            if (!tx.getLogs().isEmpty()){
                return;
            }
        }
        fail("Did not find any logs in blocks");
    }

    private void getBlockWithInternalTx() throws Web3ApiException {
        long blockWithITX = 1174759;
        var block = web3Service.getBlockDetails(blockWithITX);
        assertEquals(block.getNumTransactions(), block.getTxDetails().size());
        for (var tx: block.getTxDetails()){
            if (tx.hasInternalTransactions()){
                return;
            }
        }
        fail("Did not find any internal tx in block");
    }



    @Test
    public void getAPIBlockDetailsRangeBench() throws Web3ApiException, AionApiException {

        for (long start = 0; start <= 4_000_000L; start += 1_000_000){
            for (int i = 0; i < 4; i++) {
                final long run5 = runInRange(i, start, 19);
                final long run6 = runInRange(i, start, 18);
                final long run1 = runInRange(i, start, 499);
                final long run2 = runInRange(i, start, 999);
                final long run3 = runInRange(i, start, 1999);
                final long run4 = runInRange(i, start, 3999);

                System.out.printf("%d,%d,%d,%d,%d,%d%n",  run5, run6, run1,run2,run3,run4);
            }
        }

    }

    @Test
    public void getAPIBlockDetailsBadRequest(){
        assertThrows(Web3ApiException.class, ()-> web3Service.getBlockDetailsInRange(-100,0));
    }

    @Test
    public void getAccountDetails() throws Web3ApiException {
        var accountState = web3Service.getAccountDetails("0000000000000000000000000000000000000000000000000000000000000000");
        var accountBalance = web3Service.getBalanceAt("0000000000000000000000000000000000000000000000000000000000000000", accountState.getBlockNumber());
        var accountNonce = web3Service.getNonceAt("0000000000000000000000000000000000000000000000000000000000000000", accountState.getBlockNumber());

        assertEquals(accountBalance, accountState.getBalance());
        assertEquals(accountNonce, accountState.getNonce());
    }

    private long runInRange(long runNumber, long start, long num) throws Web3ApiException {
        long end = num + start;
        Stopwatch stopwatch = Stopwatch.createStarted();
        var result =web3Service.getBlockDetailsInRange(start, end);
        stopwatch.stop();
        long elapsed=(stopwatch.elapsed().toMillis());
        assertEquals(num+1, result.size());
        test_logger.info("Run number: {} Completed {} requests in range ({},{}) in {}ms", runNumber,end - start + 1,start, end, elapsed);
        return elapsed;
    }

}