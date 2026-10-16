package workshop.delivery.service;

import workshop.delivery.domain.ColdChainParcel;
import workshop.delivery.domain.Letter;
import workshop.delivery.domain.Parcel;
import workshop.delivery.domain.Shipment;
import workshop.delivery.domain.Weight;

public final class QuoteService {
    // TODO LAB 3A: Turn this into a switch expression over a simple type.
    public long serviceSurcharge(String serviceLevel) {
        if ("STANDARD".equals(serviceLevel)) {
            return 0;
        } else if ("EXPRESS".equals(serviceLevel)) {
            return 500;
        } else if ("SAME_DAY".equals(serviceLevel)) {
            return 1_000;
        }
        throw new IllegalArgumentException("Unknown service level: " + serviceLevel);
    }

    // TODO LAB 3B: Replace this type-dispatch chain with an exhaustive pattern switch.
    // TODO LAB 3C: Use nested record patterns for Parcel.
    public Quote quote(Shipment shipment, String serviceLevel) {
        var surcharge = serviceSurcharge(serviceLevel);

        if (shipment instanceof Letter letter) {
            return new Quote(letter.trackingId(), 350 + surcharge, "Letter");
        }

        if (shipment instanceof Parcel parcel) {
            var price = parcel.dimensions().volumeCm3() > 100_000
                    ? 1_500 + weightCharge(parcel.weight())
                    : 500 + weightCharge(parcel.weight());
            var description = parcel.dimensions().volumeCm3() > 100_000
                    ? "Oversized parcel"
                    : "Parcel";
            return new Quote(parcel.trackingId(), price + surcharge, description);
        }

        if (shipment instanceof ColdChainParcel cold) {
            return new Quote(
                    cold.trackingId(),
                    2_000 + weightCharge(cold.weight()) + surcharge,
                    "Cold-chain parcel");
        }

        throw new IllegalArgumentException("Unsupported shipment: " + shipment);
    }

    private long weightCharge(Weight weight) {
        return ((weight.grams() + 99) / 100) * 5;
    }
}

