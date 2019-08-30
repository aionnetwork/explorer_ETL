package aion.dashboard.update;

import aion.dashboard.blockchain.interfaces.Web3Service;
import aion.dashboard.domainobject.TxLog;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;

import static org.junit.jupiter.api.Assertions.*;

class UpdateTest {

    @Test
    void readInternalForBlock() {
        InternalTransactionUpdate updateTask = new InternalTransactionUpdate(Web3Service.getInstance());
        var res = updateTask.readForBlock(1187374,1191335);
        assertFalse(res.isEmpty());
        assertEquals(24,res.size());

        res = updateTask.readForBlock(1000000,1004000);
        assertTrue(res.isEmpty());
    }

    @Test
    void readTxLogForBlock() throws Exception {
        TxLogUpdate updateTask = new TxLogUpdate(Web3Service.getInstance());
        var res = updateTask.readForBlock(1187374,1191335);
        assertFalse(res.isEmpty());
        assertEquals(26,res.size());

        res = updateTask.readForBlock(1000000,1010000);
        assertTrue(res.isEmpty());
    }
}