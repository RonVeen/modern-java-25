package workshop.ceremony;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class NamingConflictDemoTest {
    @Test
    void preciseImportSelectsUtilDate() {
        assertInstanceOf(java.util.Date.class, new NamingConflictDemo().generatedAt());
    }

    @Test
    void qualifiedNameStillSelectsSqlDate() {
        var expected = LocalDate.of(2026, 9, 12);
        assertEquals(expected, new NamingConflictDemo().serviceDay(expected).toLocalDate());
    }
}
