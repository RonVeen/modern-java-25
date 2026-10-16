package workshop.delivery.domain;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShipmentModelTest {
    @Test
    void shipmentIsAClosedDomain() {
        assertTrue(Shipment.class.isSealed());

        var permittedTypes = Arrays.stream(Shipment.class.getPermittedSubclasses())
                .map(Class::getSimpleName)
                .collect(Collectors.toSet());

        assertEquals(
                Set.of("Letter", "Parcel", "ColdChainParcel", "FragileParcel"),
                permittedTypes);
    }
}

