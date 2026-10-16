package workshop.delivery.domain;

import java.util.Objects;

public final class ColdChainParcel extends DeliveryItem implements Shipment {
    private final Address destination;
    private final Dimensions dimensions;
    private final Weight weight;
    private final int minTemperatureC;
    private final int maxTemperatureC;

    // TODO LAB 4: Validate and normalize in a prologue before super(...).
    public ColdChainParcel(
            String trackingId,
            Address destination,
            Dimensions dimensions,
            Weight weight,
            int minTemperatureC,
            int maxTemperatureC) {
        super(TrackingIds.normalize(trackingId));
        if (minTemperatureC >= maxTemperatureC) {
            throw new IllegalArgumentException("Minimum temperature must be below maximum");
        }
        this.destination = Objects.requireNonNull(destination, "destination");
        this.dimensions = Objects.requireNonNull(dimensions, "dimensions");
        this.weight = Objects.requireNonNull(weight, "weight");
        this.minTemperatureC = minTemperatureC;
        this.maxTemperatureC = maxTemperatureC;
    }

    @Override
    public Address destination() {
        return destination;
    }

    public Dimensions dimensions() {
        return dimensions;
    }

    @Override
    public Weight weight() {
        return weight;
    }

    public int minTemperatureC() {
        return minTemperatureC;
    }

    public int maxTemperatureC() {
        return maxTemperatureC;
    }
}

