package aion.dashboard.stats;

import aion.dashboard.exception.GraphingException;
import aion.dashboard.service.GraphingService;
import aion.dashboard.service.GraphingServiceImpl;
import aion.dashboard.stats.DBGraphingTask;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class DBGraphingTaskTest {
    private final DBGraphingTask task = DBGraphingTask.DB_GRAPHING_TASK;

    @Test
    void extractBlocksInRange() throws SQLException {


        var res = task.extractBlocksInRange(1);
        assertTrue(res.isPresent());
        assertFalse(res.get().isEmpty());

        res.get().forEach(System.out::println);
    }

    @Test
    void compute() throws SQLException, GraphingException, InterruptedException {
        var res = task.extractBlocksInRange(1);
        assertTrue(res.isPresent());
        var graphingsRes = task.compute(res.get());
        assertFalse(graphingsRes.isEmpty());

        graphingsRes.forEach(System.out::println);
    }

    @Test
    void checkAccuracy() throws SQLException {

        var res = task.extractBlocksInRange(1);
        assertTrue(res.isPresent());

        assertTrue(DBGraphingTask.isValidRange(res.get()));
    }

    @Test
    void checkTimeout(){
        GraphingService service = GraphingServiceImpl.getInstance();
        var res =assertTimeout(Duration.ofSeconds(4), ()-> {
            return service.countActiveAddresses(3000_000L);
        });
        assertNotEquals(res, 0);
    }
}