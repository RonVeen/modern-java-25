package workshop.collections;

import java.util.ArrayList;
import java.util.List;

public final class ParcelScanProcessor {
    // TODO LAB 2A: Replace this index arithmetic with Gatherers.windowFixed.
    public List<DispatchBatch> dispatchBatches(
            List<ParcelScanEvent> events,
            int batchSize) {
        if (batchSize < 1) {
            throw new IllegalArgumentException("batchSize must be positive");
        }

        var batches = new ArrayList<DispatchBatch>();
        for (var start = 0; start < events.size(); start += batchSize) {
            var end = Math.min(start + batchSize, events.size());
            batches.add(new DispatchBatch(events.subList(start, end)));
        }
        return List.copyOf(batches);
    }

    // TODO LAB 2B: Replace this loop with Gatherers.windowSliding.
    public List<FailureAlert> repeatedFailureWindows(List<ParcelScanEvent> events) {
        var alerts = new ArrayList<FailureAlert>();
        for (var index = 0; index + 2 < events.size(); index++) {
            var window = events.subList(index, index + 3);
            if (isThreeFailuresForOneParcel(window)) {
                var last = window.get(2);
                alerts.add(new FailureAlert(last.trackingId(), last.scannedAt(), 3));
            }
        }
        return List.copyOf(alerts);
    }

    // TODO LAB 2C: Replace the mutable accumulator with Gatherers.scan.
    public List<Integer> runningWeightTotals(List<ParcelScanEvent> events) {
        var totals = new ArrayList<Integer>();
        var runningTotal = 0;
        for (var event : events) {
            runningTotal += event.weightKg();
            totals.add(runningTotal);
        }
        return List.copyOf(totals);
    }

    // TODO EXTENSION: Express this state machine as a custom Gatherer.
    public List<FailureAlert> consecutiveFailureAlerts(List<ParcelScanEvent> events) {
        var alerts = new ArrayList<FailureAlert>();
        String currentTrackingId = null;
        var failureCount = 0;

        for (var event : events) {
            if (event.status() != ScanStatus.FAILED) {
                currentTrackingId = null;
                failureCount = 0;
                continue;
            }

            if (event.trackingId().equals(currentTrackingId)) {
                failureCount++;
            } else {
                currentTrackingId = event.trackingId();
                failureCount = 1;
            }

            if (failureCount == 3) {
                alerts.add(new FailureAlert(event.trackingId(), event.scannedAt(), 3));
            }
        }
        return List.copyOf(alerts);
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

