package workshop.delivery.domain;

public sealed interface Shipment
        permits Letter, Parcel, ColdChainParcel, FragileParcel {
    String trackingId();

    Address destination();

    Weight weight();
}

