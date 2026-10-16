package workshop.collections;

final class FailureStreak {
    private String trackingId;
    private int count;

    void accept(ParcelScanEvent event) {
        if (event.status() != ScanStatus.FAILED) {
            trackingId = null;
            count = 0;
        } else if (event.trackingId().equals(trackingId)) {
            count++;
        } else {
            trackingId = event.trackingId();
            count = 1;
        }
    }

    int count() {
        return count;
    }
}

