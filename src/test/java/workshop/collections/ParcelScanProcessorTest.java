package workshop.collections;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParcelScanProcessorTest {
    private final ParcelScanProcessor processor = new ParcelScanProcessor();
    private final List<ParcelScanEvent> scans = DemoData.scans();

    @Test
    void createsFixedDispatchBatchesIncludingAPartialLastBatch() {
        var batches = processor.dispatchBatches(scans, 3);

        assertEquals(List.of(3, 3, 1),
                batches.stream().map(batch -> batch.events().size()).toList());
        assertThrows(UnsupportedOperationException.class,
                () -> batches.getFirst().events().clear());
    }

    @Test
    void detectsEverySlidingWindowOfThreeFailures() {
        var alerts = processor.repeatedFailureWindows(scans);

        assertEquals(2, alerts.size());
        assertEquals("parcel-3", alerts.getFirst().trackingId());
    }

    @Test
    void emitsARunningWeightAfterEveryEvent() {
        assertEquals(
                List.of(5, 13, 16, 19, 22, 25, 28),
                processor.runningWeightTotals(scans));
    }

    @Test
    void customAlertFiresOnceWhenTheStreakReachesThree() {
        var alerts = processor.consecutiveFailureAlerts(scans);

        assertEquals(1, alerts.size());
        assertEquals(scans.get(4).scannedAt(), alerts.getFirst().detectedAt());
    }
}

