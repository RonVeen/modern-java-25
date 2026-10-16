import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// LAB: Remove this class declaration and convert main and the helper methods
// into instance members of a compact source file.
public class DispatchReport {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: java DispatchReport.java <scans.csv>");
            return;
        }

        List<Scan> scans = readScans(Path.of(args[0]));
        printSummary(Path.of(args[0]), scans);
    }

    private static List<Scan> readScans(Path path) throws IOException {
        try (var lines = Files.lines(path)) {
            return lines.skip(1)
                    .filter(line -> !line.isBlank())
                    .map(DispatchReport::parse)
                    .toList();
        }
    }

    private static Scan parse(String line) {
        String[] columns = line.split(",", -1);
        if (columns.length != 4) {
            throw new IllegalArgumentException("Expected four columns: " + line);
        }
        return new Scan(columns[0], columns[1], columns[2], Integer.parseInt(columns[3]));
    }

    private static void printSummary(Path source, List<Scan> scans) {
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

    private record Scan(String trackingId, String hub, String status, int weightKg) {
    }
}
