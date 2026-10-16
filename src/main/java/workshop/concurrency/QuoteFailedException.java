package workshop.concurrency;

public final class QuoteFailedException extends RuntimeException {
    public QuoteFailedException(String requestId, Throwable cause) {
        super("Could not create quote for " + requestId, cause);
    }
}

