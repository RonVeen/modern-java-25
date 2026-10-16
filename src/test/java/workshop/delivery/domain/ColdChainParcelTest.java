package workshop.delivery.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ColdChainParcelTest {
    private final Address destination = new Address("NL", "Rotterdam", "3011 AA");
    private final Dimensions dimensions = new Dimensions(30, 20, 20);
    private final Weight weight = new Weight(1_500);

    @Test
    void normalizesTrackingId() {
        var parcel = new ColdChainParcel(
                " cold-25 ", destination, dimensions, weight, 2, 8);

        assertEquals("COLD-25", parcel.trackingId());
    }

    @Test
    void rejectsAnInvalidTemperatureRange() {
        assertThrows(IllegalArgumentException.class,
                () -> new ColdChainParcel(
                        "cold-25", destination, dimensions, weight, 8, 2));
    }
}

