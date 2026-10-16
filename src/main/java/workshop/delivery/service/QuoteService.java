package workshop.delivery.service;

import workshop.delivery.domain.Address;
import workshop.delivery.domain.ColdChainParcel;
import workshop.delivery.domain.Dimensions;
import workshop.delivery.domain.FragileParcel;
import workshop.delivery.domain.Letter;
import workshop.delivery.domain.Parcel;
import workshop.delivery.domain.Shipment;
import workshop.delivery.domain.Weight;

public final class QuoteService {
    public long serviceSurcharge(String serviceLevel) {
        return switch (serviceLevel) {
            case "STANDARD" -> 0;
            case "EXPRESS" -> 500;
            case "SAME_DAY" -> 1_000;
            default -> throw new IllegalArgumentException(
                    "Unknown service level: " + serviceLevel);
        };
    }

    public Quote quote(Shipment shipment, String serviceLevel) {
        var surcharge = serviceSurcharge(serviceLevel);

        return switch (shipment) {
            case Letter(var trackingId, var destination, var weight) ->
                    new Quote(trackingId, 350 + surcharge, "Letter");

            case Parcel(
                    var trackingId,
                    Address(var country, var city, var postalCode),
                    Dimensions(var length, var width, var height),
                    Weight(var grams))
                    when (long) length * width * height > 100_000 ->
                    new Quote(
                            trackingId,
                            1_500 + weightCharge(grams) + surcharge,
                            "Oversized parcel");

            case Parcel(
                    var trackingId,
                    var destination,
                    var dimensions,
                    Weight(var grams)) ->
                    new Quote(
                            trackingId,
                            500 + weightCharge(grams) + surcharge,
                            "Parcel");

            case ColdChainParcel cold ->
                    new Quote(
                            cold.trackingId(),
                            2_000 + weightCharge(cold.weight().grams()) + surcharge,
                            "Cold-chain parcel");

            case FragileParcel(
                    var trackingId,
                    var destination,
                    var dimensions,
                    Weight(var grams),
                    var handlingInstruction) ->
                    new Quote(
                            trackingId,
                            1_200 + weightCharge(grams) + surcharge,
                            "Fragile parcel: " + handlingInstruction);
        };
    }

    private long weightCharge(long grams) {
        return ((grams + 99) / 100) * 5;
    }
}

