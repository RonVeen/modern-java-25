package workshop.collections;

import java.time.Instant;
import java.util.Objects;

public record ShipmentUpdate(String shipmentId, String message, Instant updatedAt) {
    public ShipmentUpdate {
        Objects.requireNonNull(shipmentId, "shipmentId");
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(updatedAt, "updatedAt");
    }
}

