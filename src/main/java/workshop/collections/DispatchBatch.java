package workshop.collections;

import java.util.List;

public record DispatchBatch(List<ParcelScanEvent> events) {
    public DispatchBatch {
        events = List.copyOf(events);
        if (events.isEmpty()) {
            throw new IllegalArgumentException("A dispatch batch must not be empty");
        }
    }
}

