package workshop.concurrency;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VirtualThreadBatchTest {
    @Test
    void createsOneVirtualThreadForEveryQuoteTask() throws InterruptedException {
        var probe = new ServiceProbe();
        var timings = new ServiceTimings(
                Duration.ZERO, Duration.ZERO, Duration.ZERO, Duration.ZERO);
        var runner = new BatchRunner(TestFixtures.sequential(timings, probe));

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var result = runner.run(DemoData.requests(50), DemoData::contextFor, executor);
            assertEquals(50, result.quotes().size());
        }

        assertFalse(probe.events().isEmpty());
        assertTrue(probe.events().stream().allMatch(ServiceProbe.Event::virtualThread));
    }
}

