package workshop.concurrency;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

public final class BatchRunner {
    private final QuoteAggregator aggregator;

    public BatchRunner(QuoteAggregator aggregator) {
        this.aggregator = aggregator;
    }

    public BatchResult run(
            List<QuoteRequest> requests,
            Function<QuoteRequest, RequestContext> contextFactory,
            ExecutorService executor) throws InterruptedException {
        var startedAt = Instant.now();
        var futures = requests.stream()
                .map(request -> executor.submit(
                        () -> aggregator.quote(request, contextFactory.apply(request))))
                .toList();

        var quotes = new ArrayList<DeliveryQuote>(requests.size());
        for (var future : futures) {
            try {
                quotes.add(future.get());
            } catch (ExecutionException exception) {
                cancelAll(futures);
                var cause = exception.getCause();
                if (cause instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new IllegalStateException("Quote task failed", cause);
            } catch (InterruptedException exception) {
                cancelAll(futures);
                throw exception;
            }
        }
        return new BatchResult(quotes, Duration.between(startedAt, Instant.now()));
    }

    private static void cancelAll(List<? extends java.util.concurrent.Future<?>> futures) {
        futures.forEach(future -> future.cancel(true));
    }
}

