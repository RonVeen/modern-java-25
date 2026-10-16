package workshop.concurrency;

public final class CustomsRejectedException extends RuntimeException {
    public CustomsRejectedException(String requestId) {
        super("Customs rejected request " + requestId);
    }
}

