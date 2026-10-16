package workshop.collections;

import java.util.Objects;

public record DeliveryStop(String stopId, String city, String shipmentId) {
    public DeliveryStop {
        Objects.requireNonNull(stopId, "stopId");
        Objects.requireNonNull(city, "city");
        Objects.requireNonNull(shipmentId, "shipmentId");
    }
}

