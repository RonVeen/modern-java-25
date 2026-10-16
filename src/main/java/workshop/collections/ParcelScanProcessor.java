package workshop.collections;

import java.util.List;
import java.util.stream.Gatherer;
import java.util.stream.Gatherers;

public final class ParcelScanProcessor {
    public List<DispatchBatch> dispatchBatches(
            List<ParcelScanEvent> events,
            int batchSize) {
        return events.stream()
                .gather(Gatherers.windowFixed(batchSize))
                .map(DispatchBatch::new)
                .toList();
    }

    public List<FailureAlert> repeatedFailureWindows(List<ParcelScanEvent> events) {
        return events.stream()
                .gather(Gatherers.windowSliding(3))
                .filter(this::isThreeFailuresForOneParcel)
                .map(window -> {
                    var last = window.getLast();
                    return new FailureAlert(last.trackingId(), last.scannedAt(), 3);
                })
                .toList();
    }

    public List<Integer> runningWeightTotals(List<ParcelScanEvent> events) {
        return events.stream()
                .gather(Gatherers.scan(
                        () -> 0,
                        (total, event) -> total + event.weightKg()))
                .toList();
    }

    public List<FailureAlert> consecutiveFailureAlerts(List<ParcelScanEvent> events) {
        return events.stream()
                .gather(threeConsecutiveFailures())
                .toList();
    }

    public Gatherer<ParcelScanEvent, ?, FailureAlert> threeConsecutiveFailures() {
        return Gatherer.ofSequential(
                FailureStreak::new,
                (streak, event, downstream) -> {
                    streak.accept(event);
                    if (streak.count() == 3) {
                        return downstream.push(new FailureAlert(
                                event.trackingId(),
                                event.scannedAt(),
                                streak.count()));
                    }
                    return true;
                });
    }

    private boolean isThreeFailuresForOneParcel(List<ParcelScanEvent> window) {
        return window.size() == 3
                && window.stream().allMatch(event -> event.status() == ScanStatus.FAILED)
                && window.stream()
                        .map(ParcelScanEvent::trackingId)
                        .distinct()
                        .count() == 1;
    }
}

