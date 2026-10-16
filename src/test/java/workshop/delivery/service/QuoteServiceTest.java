package workshop.delivery.service;

import org.junit.jupiter.api.Test;
import workshop.delivery.domain.Address;
import workshop.delivery.domain.ColdChainParcel;
import workshop.delivery.domain.Dimensions;
import workshop.delivery.domain.FragileParcel;
import workshop.delivery.domain.Letter;
import workshop.delivery.domain.Parcel;
import workshop.delivery.domain.Weight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QuoteServiceTest {
    private final QuoteService service = new QuoteService();
    private final Address destination = new Address("NL", "Utrecht", "3511 AA");

    @Test
    void calculatesServiceSurcharges() {
        assertEquals(0, service.serviceSurcharge("STANDARD"));
        assertEquals(500, service.serviceSurcharge("EXPRESS"));
        assertEquals(1_000, service.serviceSurcharge("SAME_DAY"));
        assertThrows(IllegalArgumentException.class,
                () -> service.serviceSurcharge("WARP_SPEED"));
    }

    @Test
    void quotesLettersAndNormalParcels() {
        var letter = new Letter("letter-1", destination, new Weight(80));
        var parcel = new Parcel(
                "parcel-1", destination, new Dimensions(10, 10, 10), new Weight(2_000));

        assertEquals(350, service.quote(letter, "STANDARD").priceInCents());
        assertEquals(600, service.quote(parcel, "STANDARD").priceInCents());
    }

    @Test
    void usesGuardedBusinessRules() {
        var oversized = new Parcel(
                "parcel-big", destination, new Dimensions(100, 100, 20), new Weight(2_000));
        var cold = new ColdChainParcel(
                "cold-1", destination, new Dimensions(10, 10, 10), new Weight(2_000), 2, 8);

        assertEquals("Oversized parcel", service.quote(oversized, "STANDARD").description());
        assertEquals(1_600, service.quote(oversized, "STANDARD").priceInCents());
        assertEquals(2_100, service.quote(cold, "STANDARD").priceInCents());
    }

    @Test
    void extendingTheDomainForcesTheSwitchToGrow() {
        var fragile = new FragileParcel(
                "fragile-1",
                destination,
                new Dimensions(10, 10, 10),
                new Weight(1_000),
                "Keep upright");

        var quote = service.quote(fragile, "STANDARD");

        assertEquals(1_250, quote.priceInCents());
        assertEquals("Fragile parcel: Keep upright", quote.description());
    }
}

