package workshop.ceremony;

import java.util.List;
import java.util.Map;

public record OperationsSummary(
        int totalScans,
        int totalWeightKg,
        Map<ScanStatus, Long> scansByStatus,
        List<String> delayedShipments) {
}
