package workshop.collections;

import java.time.Instant;

public final class App {
    public static void main(String[] args) {
        var planner = new DeliveryRoutePlanner();
        DemoData.stops().forEach(planner::addStop);

        var urgent = DemoData.stops().getLast();
        planner.promoteUrgentDelivery(urgent);
        planner.completeNextStop();
        planner.recordUpdate(new ShipmentUpdate(
                "parcel-1", "Loaded", Instant.parse("2026-09-11T09:00:00Z")));
        planner.recordUpdate(new ShipmentUpdate(
                "parcel-2", "Delayed", Instant.parse("2026-09-11T09:05:00Z")));

        System.out.println("Next stop: " + planner.nextStop().orElseThrow());
        System.out.println("Previous stop: " + planner.previousStop().orElseThrow());
        System.out.println("Reverse route: " + planner.reversedRemainingRoute());
        System.out.println("Latest update: " + planner.mostRecentlyUpdatedShipment().orElseThrow());

        var processor = new ParcelScanProcessor();
        var scans = DemoData.scans();
        System.out.println("Dispatch batches: " + processor.dispatchBatches(scans, 3));
        System.out.println("Failure windows: " + processor.repeatedFailureWindows(scans));
        System.out.println("Running weights: " + processor.runningWeightTotals(scans));
        System.out.println("Custom alerts: " + processor.consecutiveFailureAlerts(scans));
    }
}

