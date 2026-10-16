package workshop.concurrency;

public final class SequentialQuoteAggregator implements QuoteAggregator {
    private final RoutingService routingService;
    private final PricingService pricingService;
    private final CustomsService customsService;

    public SequentialQuoteAggregator(
            RoutingService routingService,
            PricingService pricingService,
            CustomsService customsService) {
        this.routingService = routingService;
        this.pricingService = pricingService;
        this.customsService = customsService;
    }

    @Override
    public DeliveryQuote quote(QuoteRequest request, RequestContext context)
            throws InterruptedException {
        return RequestContexts.callWith(context, () -> {
            var routing = routingService.route(request);
            var pricing = pricingService.price(request);
            var customs = customsService.check(request);
            return DeliveryQuote.combine(request, routing, pricing, customs);
        });
    }
}

