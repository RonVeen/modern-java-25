package workshop.concurrency;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class ServiceProbe {
    public enum Outcome {
        STARTED,
        COMPLETED,
        FAILED,
        INTERRUPTED
    }

    public record Event(
            String service,
            String requestId,
            String contextRequestId,
            Outcome outcome,
            boolean virtualThread,
            String threadName) {
    }

    private final ConcurrentLinkedQueue<Event> events = new ConcurrentLinkedQueue<>();

    public void record(
            String service,
            QuoteRequest request,
            RequestContext context,
            Outcome outcome) {
        var thread = Thread.currentThread();
        events.add(new Event(
                service,
                request.requestId(),
                context.requestId(),
                outcome,
                thread.isVirtual(),
                thread.getName()));
    }

    public List<Event> events() {
        return List.copyOf(events);
    }

    public void clear() {
        events.clear();
    }
}
