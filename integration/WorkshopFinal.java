void main(String[] args) throws Exception {
    if (args.length < 3) {
        System.err.println("Usage: java WorkshopFinal.java <parcel|freight> <id> <weightKg> [zone]");
        return;
    }

    Shipment shipment = parseShipment(args);
    RequestContext context = new RequestContext(UUID.randomUUID().toString(), true);

    Quote quote = quote(shipment, context);
    System.out.println(describe(shipment));
    System.out.println("Quote: " + quote);
    System.out.println("Scan batches: " + batches(scanEvents(shipment), 2));
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
    // LAB: Make Shipment sealed and replace this chain with a record-pattern switch.
    if (shipment instanceof Parcel parcel) {
        return "Parcel %s (%d kg)".formatted(parcel.id(), parcel.weightKg());
    }
    Freight freight = (Freight) shipment;
    return "Freight %s (%d kg, zone %s)".formatted(
            freight.id(), freight.weightKg(), freight.zone());
}

Quote quote(Shipment shipment, RequestContext context) throws Exception {
    // LAB: Replace the executor and Futures with one StructuredTaskScope.
    // Then bind context with ScopedValue instead of passing it to every service.
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
        Future<RouteQuote> route = executor.submit(() -> route(shipment, context));
        Future<PriceQuote> price = executor.submit(() -> price(shipment, context));
        Future<CustomsQuote> customs = executor.submit(() -> customs(shipment, context));
        return new Quote(route.get(), price.get(), customs.get());
    }
}

RouteQuote route(Shipment shipment, RequestContext context) throws InterruptedException {
    trace(context, "routing");
    Thread.sleep(90);
    return new RouteQuote("AMS-UTR-RTM", 142);
}

PriceQuote price(Shipment shipment, RequestContext context) throws InterruptedException {
    trace(context, "pricing");
    Thread.sleep(70);
    return new PriceQuote(12.50 + shipment.weightKg() * 0.85 + handlingFee(shipment));
}

CustomsQuote customs(Shipment shipment, RequestContext context) throws InterruptedException {
    trace(context, "customs");
    Thread.sleep(110);
    return new CustomsQuote(true);
}

double handlingFee(Shipment shipment) {
    if (shipment instanceof Parcel) {
        return 2.00;
    }
    Freight freight = (Freight) shipment;
    return freight.zone().equalsIgnoreCase("EU") ? 25.00 : 60.00;
}

void trace(RequestContext context, String service) {
    if (context.trace()) {
        System.out.printf("[%s] %s on %s (virtual=%s)%n",
                context.requestId(), service, Thread.currentThread().getName(),
                Thread.currentThread().isVirtual());
    }
}

List<List<ScanEvent>> batches(List<ScanEvent> events, int size) {
    // LAB: Replace this loop with Gatherers.windowFixed(size).
    var result = new ArrayList<List<ScanEvent>>();
    for (int start = 0; start < events.size(); start += size) {
        result.add(List.copyOf(events.subList(start, Math.min(start + size, events.size()))));
    }
    return List.copyOf(result);
}

List<ScanEvent> scanEvents(Shipment shipment) {
    return List.of(
            new ScanEvent(shipment.id(), "ARRIVED"),
            new ScanEvent(shipment.id(), "SORTED"),
            new ScanEvent(shipment.id(), "LOADED"),
            new ScanEvent(shipment.id(), "DEPARTED"),
            new ScanEvent(shipment.id(), "DELIVERED"));
}

interface Shipment {
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
