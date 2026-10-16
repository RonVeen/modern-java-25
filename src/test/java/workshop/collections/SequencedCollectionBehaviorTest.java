package workshop.collections;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedMap;
import java.util.SequencedSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SequencedCollectionBehaviorTest {
    @Test
    void addingAnExistingSetElementAtTheFrontRepositionsIt() {
        SequencedSet<String> values = new LinkedHashSet<>(List.of("A", "B", "C"));

        values.addFirst("C");

        assertEquals(List.of("C", "A", "B"), List.copyOf(values));
    }

    @Test
    void puttingAnExistingMapEntryLastRepositionsIt() {
        SequencedMap<String, Integer> values = new LinkedHashMap<>();
        values.put("A", 1);
        values.put("B", 2);

        values.putLast("A", 3);

        assertEquals(List.of("B", "A"), List.copyOf(values.sequencedKeySet()));
        assertEquals(3, values.lastEntry().getValue());
    }
}

