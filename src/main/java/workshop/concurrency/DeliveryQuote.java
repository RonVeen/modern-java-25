package workshop.concurrency;

public record DeliveryQuote(
        String requestId,
        RoutingResult routing,
        PricingResult pricing,
        CustomsResult customs,
        long totalPriceInCents) {

    public static DeliveryQuote combine(
            QuoteRequest request,
            RoutingResult routing,
            PricingResult pricing,
            CustomsResult customs) {
        return new DeliveryQuote(
                request.requestId(),
                routing,
                pricing,
                customs,
                pricing.transportPriceInCents() + customs.feeInCents());
    }
}

