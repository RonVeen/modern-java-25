package workshop.delivery.domain;

import java.util.Objects;

public record Parcel(
        String trackingId,
        Address destination,
        Dimensions dimensions,
        Weight weight) implements Shipment {

    public Parcel {
        trackingId = TrackingIds.normalize(trackingId);
        Objects.requireNonNull(destination, "destination");
        Objects.requireNonNull(dimensions, "dimensions");
        Objects.requireNonNull(weight, "weight");
    }
}

