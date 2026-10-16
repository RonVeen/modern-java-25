package workshop.concurrency;

import java.time.Duration;

public final class StructuredQuoteAggregator implements QuoteAggregator {
    private final RoutingService routingService;
    private final PricingService pricingService;
    private final CustomsService customsService;
    private final Duration timeout;

    public StructuredQuoteAggregator(
            RoutingService routingService,
            PricingService pricingService,
            CustomsService customsService,
            Duration timeout) {
        this.routingService = routingService;
        this.pricingService = pricingService;
        this.customsService = customsService;
        this.timeout = timeout;
    }

    // TODO LAB 2: Replace these sequential calls with three child tasks in a
    // StructuredTaskScope. Apply timeout to the scope and translate failure.
    @Override
    public DeliveryQuote quote(QuoteRequest request, RequestContext context)
            throws InterruptedException {
        var routing = routingService.route(request, context);
        var pricing = pricingService.price(request, context);
        var customs = customsService.check(request, context);
        return DeliveryQuote.combine(request, routing, pricing, customs);
    }
}

