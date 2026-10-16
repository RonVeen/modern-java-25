package workshop.concurrency;

public record PricingResult(long basePriceInCents, long discountInCents) {
    public long transportPriceInCents() {
        return basePriceInCents - discountInCents;
    }
}

