package aion.dashboard.stats;

import aion.dashboard.domainobject.Block;
import aion.dashboard.domainobject.SealInfo;
import aion.dashboard.service.BlockService;
import aion.dashboard.service.BlockServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class MetricsCalcTest {

    private BlockService service;

    @BeforeEach
    void setUp() {
        service = BlockServiceImpl.getInstance();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void calculateStats() throws SQLException {
        var list = service.getMiningInfo(0, 60479);
        var block = service.getByBlockNumber(60479);
        var stats = MetricsCalc.calculateStats(list, block);
        var miners = list.stream().map(m -> m.getMinerAddress() + "-" + m.getSealType()).collect(Collectors.toSet());
        assertEquals(miners.size(), stats.size());
        assertTrue(stats.stream().allMatch(stat -> stat.getBlockTimestamp() == block.getBlockTimestamp()));
        assertTrue(stats.stream().allMatch(stat -> stat.getBlockNumber() == block.getBlockNumber()));
        assertTrue(stats.stream().allMatch(stat -> miners.contains(stat.getMinerAddress() + "-" + stat.getSealType())));

        List<SealInfo> sealInfos = new ArrayList<>();
        int transactionNum = 10;
        List<String> address = List.of("a", "b", "c", "d");
        int count = 0;
        for (int j = 0; j < 10; j++) {

            for (int i = 0; i <= 2; i++) {
                sealInfos.add(new SealInfo(address.get(i), count, "POW", transactionNum));
                count++;
            }

            for (int i = 2; i <= 3; i++) {
                sealInfos.add(new SealInfo(address.get(i), count, "POS", transactionNum));
                count++;
            }
        }

        Block block1 = new Block.BlockBuilder().blockNumber(count).blockTimestamp(System.currentTimeMillis() / 1000).build();
        var stats2 = MetricsCalc.calculateStats(sealInfos, block1);
        assertEquals(5, stats2.size());
        assertTrue(stats2.stream().allMatch(m -> m.getPercentageOfBlocksValidated().compareTo(BigDecimal.valueOf(20)) == 0));
        assertTrue(stats2.stream().allMatch(m -> m.getBlockCount() == 10));
        assertTrue(stats2.stream().allMatch(m-> address.contains(m.getMinerAddress())));
        assertTrue(stats2.stream().allMatch(m-> m.getAverageTransactionsPerBlock().compareTo(BigDecimal.TEN) == 0));
    }

    @Test
    void countValidators() throws SQLException {
        var list = service.getMiningInfo(0, 60479);
        var res = assertDoesNotThrow(() -> MetricsCalc.countValidators(list));
        var miners = list.stream().map(m -> m.getMinerAddress() + "-" + m.getSealType()).collect(Collectors.toSet());
        assertNotNull(res);
        assertTrue(res.values().stream().allMatch(sealInfoList -> sealInfoList.size() >= 1));
        assertEquals(list.size(), res.values().stream().mapToInt(List::size).sum());
        assertTrue(res.keySet().containsAll(miners));
    }
}