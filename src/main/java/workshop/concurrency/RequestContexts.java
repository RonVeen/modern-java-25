package workshop.concurrency;

import java.lang.ScopedValue.CallableOp;

public final class RequestContexts {
    private RequestContexts() {
    }

    // TODO LAB 3: Introduce a private ScopedValue<RequestContext> key.
    public static RequestContext current() {
        throw new IllegalStateException("No RequestContext is bound");
    }

    // TODO LAB 3: Bind the context only for the dynamic extent of operation.
    public static <T, X extends Throwable> T callWith(
            RequestContext context,
            CallableOp<? extends T, X> operation) throws X {
        return operation.call();
    }
}

