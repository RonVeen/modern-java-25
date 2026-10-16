package workshop.delivery.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WeightClassifierTest {
    @Test
    void selectsTheSmallestExactPrimitiveType() {
        assertEquals("byte", WeightClassifier.smallestExactStorageType(127));
        assertEquals("short", WeightClassifier.smallestExactStorageType(128));
        assertEquals("short", WeightClassifier.smallestExactStorageType(32_767));
        assertEquals("int", WeightClassifier.smallestExactStorageType(32_768));
        assertEquals("long", WeightClassifier.smallestExactStorageType(3_000_000_000L));
    }

    @Test
    void rejectsNegativeWeight() {
        assertThrows(IllegalArgumentException.class,
                () -> WeightClassifier.smallestExactStorageType(-1));
    }
}

