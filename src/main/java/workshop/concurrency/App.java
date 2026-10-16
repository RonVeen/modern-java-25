package workshop.concurrency;

import java.time.Duration;
import java.util.concurrent.Executors;

public final class App {
    public static void main(String[] args) throws InterruptedException {
        var timings = ServiceTimings.demo();
        var requests = DemoData.requests(100);

        var platformProbe = new ServiceProbe();
        var platformRunner = new BatchRunner(sequentialAggregator(timings, platformProbe));
        try (var executor = Executors.newFixedThreadPool(8)) {
            print("fixed platform pool", platformRunner.run(
                    requests, DemoData::contextFor, executor));
        }

        var virtualProbe = new ServiceProbe();
        var virtualRunner = new BatchRunner(sequentialAggregator(timings, virtualProbe));
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            print("virtual thread per task", virtualRunner.run(
                    requests, DemoData::contextFor, executor));
        }

        var structuredProbe = new ServiceProbe();
        var structuredRunner = new BatchRunner(structuredAggregator(timings, structuredProbe));
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            print("virtual threads + structured children", structuredRunner.run(
                    requests, DemoData::contextFor, executor));
        }
    }

    private static QuoteAggregator sequentialAggregator(
            ServiceTimings timings,
            ServiceProbe probe) {
        return new SequentialQuoteAggregator(
                new RoutingService(timings, probe),
                new PricingService(timings, probe),
                new CustomsService(timings, probe));
    }

    private static QuoteAggregator structuredAggregator(
            ServiceTimings timings,
            ServiceProbe probe) {
        return new StructuredQuoteAggregator(
                new RoutingService(timings, probe),
                new PricingService(timings, probe),
                new CustomsService(timings, probe),
                Duration.ofSeconds(2));
    }

    private static void print(String label, BatchResult result) {
        System.out.printf(
                "%-38s %3d quotes in %4d ms%n",
                label,
                result.quotes().size(),
                result.elapsed().toMillis());
    }
}

