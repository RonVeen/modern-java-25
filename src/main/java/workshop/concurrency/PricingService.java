package workshop.concurrency;

public final class PricingService {
    private final ServiceTimings timings;
    private final ServiceProbe probe;

    public PricingService(ServiceTimings timings, ServiceProbe probe) {
        this.timings = timings;
        this.probe = probe;
    }

    public PricingResult price(QuoteRequest request) throws InterruptedException {
        var context = RequestContexts.current();
        probe.record("pricing", request, context, ServiceProbe.Outcome.STARTED);
        try {
            Thread.sleep(timings.pricing());
            var basePrice = 1_000L + request.weightKg() * 75L;
            var discount = context.customerTier() == CustomerTier.GOLD
                    ? basePrice / 10
                    : 0L;
            var result = new PricingResult(basePrice, discount);
            probe.record("pricing", request, context, ServiceProbe.Outcome.COMPLETED);
            return result;
        } catch (InterruptedException exception) {
            probe.record("pricing", request, context, ServiceProbe.Outcome.INTERRUPTED);
            throw exception;
        }
    }
}
