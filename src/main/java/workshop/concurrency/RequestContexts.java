package workshop.concurrency;

import java.lang.ScopedValue.CallableOp;

public final class RequestContexts {
    private static final ScopedValue<RequestContext> CURRENT = ScopedValue.newInstance();

    private RequestContexts() {
    }

    public static RequestContext current() {
        return CURRENT.orElseThrow(() ->
                new IllegalStateException("No RequestContext is bound"));
    }

    public static <T, X extends Throwable> T callWith(
            RequestContext context,
            CallableOp<? extends T, X> operation) throws X {
        return ScopedValue.where(CURRENT, context).call(operation);
    }
}

