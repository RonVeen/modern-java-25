package workshop.delivery.domain;

abstract class DeliveryItem {
    private final String trackingId;

    protected DeliveryItem(String trackingId) {
        this.trackingId = trackingId;
    }

    public final String trackingId() {
        return trackingId;
    }
}

