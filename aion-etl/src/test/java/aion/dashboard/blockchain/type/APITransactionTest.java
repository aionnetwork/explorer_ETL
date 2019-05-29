package aion.dashboard.blockchain.type;

import aion.dashboard.blockchain.APIService;
import aion.dashboard.blockchain.Web3Service;
import aion.dashboard.domainobject.Transaction;
import aion.dashboard.service.BlockService;
import aion.dashboard.service.BlockServiceImpl;
import aion.dashboard.service.TransactionService;
import aion.dashboard.service.TransactionServiceImpl;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class APITransactionTest {

    TransactionService service = TransactionServiceImpl.getInstance();
    APIService apiService= Web3Service.getInstance();

    @Test
    void compare() throws Exception {

        List<Transaction> transactions = service.getTransactionByBlockNumber(1851365);
        for (var tx: transactions){
            assertTrue(apiService.getTransaction(tx.getTransactionHash()).compareTransactions(tx));
        }


    }
}