package workshop.concurrency;

import java.util.Objects;

public record QuoteRequest(
        String requestId,
        String originCountry,
        String destinationCountry,
        int weightKg,
        Scenario scenario) {

    public QuoteRequest {
        Objects.requireNonNull(requestId, "requestId");
        Objects.requireNonNull(originCountry, "originCountry");
        Objects.requireNonNull(destinationCountry, "destinationCountry");
        Objects.requireNonNull(scenario, "scenario");
        if (weightKg <= 0) {
            throw new IllegalArgumentException("weightKg must be positive");
        }
    }
}

