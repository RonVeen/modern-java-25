package workshop.concurrency;

public final class CustomsService {
    private final ServiceTimings timings;
    private final ServiceProbe probe;

    public CustomsService(ServiceTimings timings, ServiceProbe probe) {
        this.timings = timings;
        this.probe = probe;
    }

    public CustomsResult check(QuoteRequest request) throws InterruptedException {
        probe.record("customs", request, ServiceProbe.Outcome.STARTED);
        try {
            var delay = request.scenario() == Scenario.SLOW_CUSTOMS
                    ? timings.slowCustoms()
                    : timings.customs();
            Thread.sleep(delay);

            if (request.scenario() == Scenario.CUSTOMS_FAILURE) {
                probe.record("customs", request, ServiceProbe.Outcome.FAILED);
                throw new CustomsRejectedException(request.requestId());
            }

            var required = !request.originCountry().equals(request.destinationCountry());
            var result = required
                    ? new CustomsResult(true, 1_500, "CLEARED")
                    : new CustomsResult(false, 0, "NOT_REQUIRED");
            probe.record("customs", request, ServiceProbe.Outcome.COMPLETED);
            return result;
        } catch (InterruptedException exception) {
            probe.record("customs", request, ServiceProbe.Outcome.INTERRUPTED);
            throw exception;
        }
    }
}

