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
        var routing = routingService.route(request, context);
        var pricing = pricingService.price(request, context);
        var customs = customsService.check(request, context);
        return DeliveryQuote.combine(request, routing, pricing, customs);
    }
}

