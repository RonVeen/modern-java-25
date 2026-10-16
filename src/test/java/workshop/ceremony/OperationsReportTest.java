package workshop.ceremony;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OperationsReportTest {
    private final List<ScanEvent> events = List.of(
            event("P-101", ScanStatus.ARRIVED, 5, "2026-09-12T08:00:00Z"),
            event("P-102", ScanStatus.DELAYED, 8, "2026-09-12T08:01:00Z"),
            event("P-102", ScanStatus.LOADED, 8, "2026-09-12T08:03:00Z"),
            event("P-103", ScanStatus.DELAYED, 3, "2026-09-12T08:02:00Z"));

    @Test
    void buildsAnOperationalSummary() {
        var summary = new OperationsReport().summarize(events);

        assertEquals(4, summary.totalScans());
        assertEquals(24, summary.totalWeightKg());
        assertEquals(2, summary.scansByStatus().get(ScanStatus.DELAYED));
        assertEquals(List.of("P-102", "P-103"), summary.delayedShipments());
    }

    @Test
    void keepsTheLatestScanForEachShipment() {
        var latest = new OperationsReport().latestScanByShipment(events);

        assertEquals(3, latest.size());
        assertEquals(ScanStatus.LOADED, latest.get("P-102").status());
    }

    private ScanEvent event(String id, ScanStatus status, int weight, String time) {
        return new ScanEvent(id, "AMS", status, weight, Instant.parse(time));
    }
}
