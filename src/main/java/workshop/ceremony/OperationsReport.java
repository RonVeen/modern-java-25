package workshop.ceremony;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class OperationsReport {

    // LAB: Replace the java.base imports above with one module import.
    public OperationsSummary summarize(List<ScanEvent> events) {
        Objects.requireNonNull(events);

        Map<ScanStatus, Long> counts = events.stream()
                .collect(Collectors.groupingBy(
                        ScanEvent::status,
                        () -> new EnumMap<>(ScanStatus.class),
                        Collectors.counting()));

        var delayed = events.stream()
                .filter(event -> event.status() == ScanStatus.DELAYED)
                .sorted(Comparator.comparing(ScanEvent::scannedAt))
                .map(ScanEvent::trackingId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        int totalWeight = events.stream()
                .map(ScanEvent::weightKg)
                .reduce(0, Integer::sum);

        return new OperationsSummary(
                events.size(),
                totalWeight,
                Map.copyOf(counts),
                new ArrayList<>(delayed));
    }

    public Map<String, ScanEvent> latestScanByShipment(List<ScanEvent> events) {
        return events.stream().collect(Collectors.toMap(
                ScanEvent::trackingId,
                Function.identity(),
                (left, right) -> left.scannedAt().isAfter(right.scannedAt()) ? left : right));
    }
}
