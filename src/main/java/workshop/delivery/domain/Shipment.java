package workshop.delivery.domain;

// TODO LAB 2: Close this domain with sealed and permits.
public interface Shipment {
    String trackingId();

    Address destination();

    Weight weight();
}

