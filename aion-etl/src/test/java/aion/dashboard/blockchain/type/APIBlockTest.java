package aion.dashboard.blockchain.type;

import aion.dashboard.blockchain.interfaces.APIService;
import aion.dashboard.blockchain.interfaces.Web3Service;
import aion.dashboard.service.BlockService;
import aion.dashboard.service.BlockServiceImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class APIBlockTest {

    BlockService service = BlockServiceImpl.getInstance();
    APIService web3Service = Web3Service.getInstance();
    @Test
    void compareBlocks() throws Exception {
        assertTrue(web3Service.getBlock(1).compareBlocks(service.getByBlockNumber(1)));
        assertTrue(web3Service.getBlock(30044).compareBlocks(service.getByBlockNumber(30044)));
        assertTrue(web3Service.getBlock(808000).compareBlocks(service.getByBlockNumber(808000)));
        assertTrue(web3Service.getBlock(41934).compareBlocks(service.getByBlockNumber(41934)));
    }
}