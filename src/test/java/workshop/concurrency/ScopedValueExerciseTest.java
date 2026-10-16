package workshop.concurrency;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Disabled("LAB 3: enable after implementing RequestContexts")
class ScopedValueExerciseTest {
    @Test
    void bindingExistsOnlyInsideTheOperation() throws Exception {
        var context = new RequestContext("scope-1", CustomerTier.GOLD, true);

        assertThrows(IllegalStateException.class, RequestContexts::current);
        var requestId = RequestContexts.callWith(
                context,
                () -> RequestContexts.current().requestId());
        assertEquals("scope-1", requestId);
        assertThrows(IllegalStateException.class, RequestContexts::current);
    }
}

