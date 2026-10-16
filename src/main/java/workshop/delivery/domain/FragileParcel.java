package workshop.delivery.domain;

import java.util.Objects;

public record FragileParcel(
        String trackingId,
        Address destination,
        Dimensions dimensions,
        Weight weight,
        String handlingInstruction) implements Shipment {

    public FragileParcel {
        trackingId = TrackingIds.normalize(trackingId);
        Objects.requireNonNull(destination, "destination");
        Objects.requireNonNull(dimensions, "dimensions");
        Objects.requireNonNull(weight, "weight");
        Objects.requireNonNull(handlingInstruction, "handlingInstruction");
    }
}

