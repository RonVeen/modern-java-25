final ScopedValue<RequestContext> CURRENT_REQUEST = ScopedValue.newInstance();

void main(String[] args) throws Exception {
    if (args.length < 3) {
        System.err.println("Usage: java --enable-preview WorkshopFinal.java <parcel|freight> <id> <weightKg> [zone]");
        return;
    }

    Shipment shipment = parseShipment(args);
    RequestContext context = new RequestContext(UUID.randomUUID().toString(), true);

    Quote quote = ScopedValue.where(CURRENT_REQUEST, context)
            .call(() -> quote(shipment));

    System.out.println(describe(shipment));
    System.out.println("Quote: " + quote);
    System.out.println("Scan batches: " + batch(scanEvents(shipment), 2));
}

Shipment parseShipment(String[] args) {
    String kind = args[0].toLowerCase(Locale.ROOT);
    String id = args[1];
    int weightKg = Integer.parseInt(args[2]);

    return switch (kind) {
        case "parcel" -> new Parcel(id, weightKg);
        case "freight" -> new Freight(id, weightKg, args.length > 3 ? args[3] : "EU");
        default -> throw new IllegalArgumentException("Unknown shipment kind: " + kind);
    };
}

String describe(Shipment shipment) {
    return switch (shipment) {
        case Parcel(String id, int weightKg) ->
                "Parcel %s (%d kg)".formatted(id, weightKg);
        case Freight(String id, int weightKg, String zone) ->
                "Freight %s (%d kg, zone %s)".formatted(id, weightKg, zone);
    };
}

Quote quote(Shipment shipment) throws InterruptedException {
    try (var scope = StructuredTaskScope.open(
            StructuredTaskScope.Joiner.<Object>awaitAllSuccessfulOrThrow())) {
        var route = scope.fork(() -> route(shipment));
        var price = scope.fork(() -> price(shipment));
        var customs = scope.fork(() -> customs(shipment));

        scope.join();
        return new Quote(route.get(), price.get(), customs.get());
    } catch (StructuredTaskScope.FailedException exception) {
        throw new IllegalStateException("Could not create quote", exception.getCause());
    }
}

RouteQuote route(Shipment shipment) throws InterruptedException {
    trace("routing");
    Thread.sleep(90);
    return new RouteQuote("AMS-UTR-RTM", 142);
}

PriceQuote price(Shipment shipment) throws InterruptedException {
    trace("pricing");
    Thread.sleep(70);
    double handlingFee = switch (shipment) {
        case Parcel(_, _) -> 2.00;
        case Freight(_, _, String zone) -> zone.equalsIgnoreCase("EU") ? 25.00 : 60.00;
    };
    return new PriceQuote(12.50 + shipment.weightKg() * 0.85 + handlingFee);
}

CustomsQuote customs(Shipment shipment) throws InterruptedException {
    trace("customs");
    Thread.sleep(110);
    return new CustomsQuote(true);
}

void trace(String service) {
    RequestContext context = CURRENT_REQUEST.orElseThrow(
            () -> new IllegalStateException("No request context is bound"));
    if (context.trace()) {
        System.out.printf("[%s] %s on %s (virtual=%s)%n",
                context.requestId(), service, Thread.currentThread().getName(),
                Thread.currentThread().isVirtual());
    }
}

List<List<ScanEvent>> batch(List<ScanEvent> events, int size) {
    return events.stream()
            .gather(Gatherers.windowFixed(size))
            .toList();
}

List<ScanEvent> scanEvents(Shipment shipment) {
    return List.of(
            new ScanEvent(shipment.id(), "ARRIVED"),
            new ScanEvent(shipment.id(), "SORTED"),
            new ScanEvent(shipment.id(), "LOADED"),
            new ScanEvent(shipment.id(), "DEPARTED"),
            new ScanEvent(shipment.id(), "DELIVERED"));
}

sealed interface Shipment permits Parcel, Freight {
    String id();
    int weightKg();
}

record Parcel(String id, int weightKg) implements Shipment {
}

record Freight(String id, int weightKg, String zone) implements Shipment {
}

record RequestContext(String requestId, boolean trace) {
}

record RouteQuote(String route, int kilometres) {
}

record PriceQuote(double amount) {
}

record CustomsQuote(boolean cleared) {
}

record Quote(RouteQuote route, PriceQuote price, CustomsQuote customs) {
}

record ScanEvent(String trackingId, String status) {
}
