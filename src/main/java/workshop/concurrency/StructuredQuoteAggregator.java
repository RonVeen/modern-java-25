package workshop.concurrency;

import java.time.Duration;
import java.util.concurrent.StructuredTaskScope;

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

    @Override
    public DeliveryQuote quote(QuoteRequest request, RequestContext context)
            throws InterruptedException {
        return RequestContexts.callWith(
                context,
                () -> quoteWithinBoundContext(request));
    }

    private DeliveryQuote quoteWithinBoundContext(QuoteRequest request)
            throws InterruptedException {
        try (var scope = StructuredTaskScope.open(
                StructuredTaskScope.Joiner.<Object>awaitAllSuccessfulOrThrow(),
                configuration -> configuration
                        .withName("quote-" + request.requestId())
                        .withTimeout(timeout))) {

            var routing = scope.fork(() -> routingService.route(request));
            var pricing = scope.fork(() -> pricingService.price(request));
            var customs = scope.fork(() -> customsService.check(request));

            scope.join();

            return DeliveryQuote.combine(
                    request,
                    routing.get(),
                    pricing.get(),
                    customs.get());
        } catch (StructuredTaskScope.TimeoutException exception) {
            throw new QuoteTimeoutException(request.requestId(), exception);
        } catch (StructuredTaskScope.FailedException exception) {
            throw new QuoteFailedException(request.requestId(), exception.getCause());
        }
    }
}

