package workshop.delivery.domain;

import java.util.Objects;

public final class ColdChainParcel extends DeliveryItem implements Shipment {
    private final Address destination;
    private final Dimensions dimensions;
    private final Weight weight;
    private final int minTemperatureC;
    private final int maxTemperatureC;

    public ColdChainParcel(
            String trackingId,
            Address destination,
            Dimensions dimensions,
            Weight weight,
            int minTemperatureC,
            int maxTemperatureC) {
        var normalizedTrackingId = TrackingIds.normalize(trackingId);
        Objects.requireNonNull(destination, "destination");
        Objects.requireNonNull(dimensions, "dimensions");
        Objects.requireNonNull(weight, "weight");
        if (minTemperatureC >= maxTemperatureC) {
            throw new IllegalArgumentException("Minimum temperature must be below maximum");
        }

        super(normalizedTrackingId);

        this.destination = destination;
        this.dimensions = dimensions;
        this.weight = weight;
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

