package workshop.delivery.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddressTest {
    @Test
    void isAValidatedValueRecord() {
        var first = new Address(" nl ", " Amsterdam ", " 1012 JS ");
        var second = new Address("NL", "Amsterdam", "1012 JS");

        assertTrue(Address.class.isRecord());
        assertEquals(first, second);
        assertEquals("NL", first.country());
        assertEquals("Amsterdam", first.city());
        assertEquals("1012 JS", first.postalCode());
    }

    @Test
    void rejectsBlankComponents() {
        assertThrows(IllegalArgumentException.class,
                () -> new Address("NL", " ", "1012 JS"));
    }
}

