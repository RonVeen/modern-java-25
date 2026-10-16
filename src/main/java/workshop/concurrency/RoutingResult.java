package workshop.concurrency;

import java.time.Duration;

public record RoutingResult(int distanceKm, String zone, Duration estimatedDeliveryTime) {
}

