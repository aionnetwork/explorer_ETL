package aion.dashboard.task;

import aion.dashboard.exception.GraphingException;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

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
}