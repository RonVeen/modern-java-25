package workshop.collections;

import java.time.Instant;
import java.util.List;

public final class DemoData {
    private DemoData() {
    }

    public static List<DeliveryStop> stops() {
        return List.of(
                new DeliveryStop("A", "Amsterdam", "parcel-1"),
                new DeliveryStop("B", "Utrecht", "parcel-2"),
                new DeliveryStop("C", "Rotterdam", "parcel-3"));
    }

    public static List<ParcelScanEvent> scans() {
        var start = Instant.parse("2026-09-11T08:00:00Z");
        return List.of(
                scan("parcel-1", start, 0, ScanStatus.ARRIVED, 5),
                scan("parcel-2", start, 1, ScanStatus.LOADED, 8),
                scan("parcel-3", start, 2, ScanStatus.FAILED, 3),
                scan("parcel-3", start, 3, ScanStatus.FAILED, 3),
                scan("parcel-3", start, 4, ScanStatus.FAILED, 3),
                scan("parcel-3", start, 5, ScanStatus.FAILED, 3),
                scan("parcel-3", start, 6, ScanStatus.LOADED, 3));
    }

    private static ParcelScanEvent scan(
            String trackingId,
            Instant start,
            long minute,
            ScanStatus status,
            int weightKg) {
        return new ParcelScanEvent(
                trackingId,
                start.plusSeconds(minute * 60),
                status,
                weightKg);
    }
}

