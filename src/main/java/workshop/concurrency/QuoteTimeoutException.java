package workshop.concurrency;

public final class QuoteTimeoutException extends RuntimeException {
    public QuoteTimeoutException(String requestId, Throwable cause) {
        super("Quote timed out for " + requestId, cause);
    }
}

