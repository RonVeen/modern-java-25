package workshop.concurrency;

import java.util.concurrent.Executors;

public final class App {
    public static void main(String[] args) throws InterruptedException {
        var probe = new ServiceProbe();
        var timings = ServiceTimings.demo();
        var aggregator = new SequentialQuoteAggregator(
                new RoutingService(timings, probe),
                new PricingService(timings, probe),
                new CustomsService(timings, probe));
        var runner = new BatchRunner(aggregator);
        var requests = DemoData.requests(100);

        // TODO LAB 1: Compare this bounded platform-thread pool with a
        // virtual-thread-per-task executor. Do not create a virtual-thread pool.
        try (var executor = Executors.newFixedThreadPool(8)) {
            var result = runner.run(requests, DemoData::contextFor, executor);
            System.out.printf(
                    "Completed %d quotes in %d ms using platform threads%n",
                    result.quotes().size(),
                    result.elapsed().toMillis());
        }

        // TODO LAB 2: Add StructuredQuoteAggregator with fail-fast children
        // and a per-quote deadline.
        // TODO LAB 3: Replace context parameters with ScopedValue.
    }
}

