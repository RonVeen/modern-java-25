package workshop.concurrency;

import java.util.List;
import java.util.stream.IntStream;

public final class DemoData {
    private DemoData() {
    }

    public static List<QuoteRequest> requests(int count) {
        return IntStream.range(0, count)
                .mapToObj(index -> new QuoteRequest(
                        "request-" + index,
                        "NL",
                        index % 2 == 0 ? "NL" : "DE",
                        1 + index % 20,
                        Scenario.NORMAL))
                .toList();
    }

    public static RequestContext contextFor(QuoteRequest request) {
        var number = Integer.parseInt(request.requestId().substring("request-".length()));
        var tier = number % 3 == 0 ? CustomerTier.GOLD : CustomerTier.REGULAR;
        return new RequestContext(request.requestId(), tier, false);
    }
}

