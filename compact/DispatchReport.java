void main(String[] args) throws IOException {
    if (args.length != 1) {
        System.err.println("Usage: java DispatchReport.java <scans.csv>");
        return;
    }

    Path source = Path.of(args[0]);
    List<Scan> scans = readScans(source);
    printSummary(source, scans);
}

List<Scan> readScans(Path path) throws IOException {
    try (var lines = Files.lines(path)) {
        return lines.skip(1)
                .filter(line -> !line.isBlank())
                .map(this::parse)
                .toList();
    }
}

Scan parse(String line) {
    String[] columns = line.split(",", -1);
    if (columns.length != 4) {
        throw new IllegalArgumentException("Expected four columns: " + line);
    }
    return new Scan(columns[0], columns[1], columns[2], Integer.parseInt(columns[3]));
}

void printSummary(Path source, List<Scan> scans) {
    Map<String, Long> byStatus = scans.stream()
            .collect(Collectors.groupingBy(Scan::status, Collectors.counting()));
    int observedWeight = scans.stream().mapToInt(Scan::weightKg).sum();
    List<String> delayed = scans.stream()
            .filter(scan -> scan.status().equals("DELAYED"))
            .map(Scan::trackingId)
            .distinct()
            .toList();

    System.out.println("Dispatch summary: " + source.getFileName());
    System.out.println("Scans: " + scans.size());
    System.out.println("Observed weight: " + observedWeight + " kg");
    System.out.println("By status: " + byStatus);
    System.out.println("Delayed shipments: " + delayed);
}

record Scan(String trackingId, String hub, String status, int weightKg) {
}
