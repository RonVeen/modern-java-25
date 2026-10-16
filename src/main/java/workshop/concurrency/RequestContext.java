package workshop.concurrency;

import java.util.Objects;

public record RequestContext(String requestId, CustomerTier customerTier, boolean traceEnabled) {
    public RequestContext {
        Objects.requireNonNull(requestId, "requestId");
        Objects.requireNonNull(customerTier, "customerTier");
    }
}

