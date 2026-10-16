package workshop.concurrency;

@FunctionalInterface
public interface QuoteAggregator {
    DeliveryQuote quote(QuoteRequest request, RequestContext context)
            throws InterruptedException;
}

