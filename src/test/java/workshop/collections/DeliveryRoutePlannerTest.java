package workshop.collections;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeliveryRoutePlannerTest {
    @Test
    void navigatesAndReordersTheRoute() {
        var planner = new DeliveryRoutePlanner();
        var stops = DemoData.stops();
        stops.forEach(planner::addStop);

        assertEquals(stops.getFirst(), planner.nextStop().orElseThrow());

        planner.promoteUrgentDelivery(stops.getLast());
        assertEquals(stops.getLast(), planner.nextStop().orElseThrow());

        planner.completeNextStop();
        assertEquals(stops.getLast(), planner.previousStop().orElseThrow());
        assertEquals(List.of(stops.get(1), stops.get(0)),
                List.copyOf(planner.reversedRemainingRoute()));
    }

    @Test
    void findsTheMostRecentlyRecordedShipmentUpdate() {
        var planner = new DeliveryRoutePlanner();
        var time = Instant.parse("2026-09-11T09:00:00Z");

        planner.recordUpdate(new ShipmentUpdate("parcel-1", "Loaded", time));
        planner.recordUpdate(new ShipmentUpdate("parcel-2", "Delayed", time.plusSeconds(60)));
        planner.recordUpdate(new ShipmentUpdate("parcel-1", "Departed", time.plusSeconds(120)));

        assertEquals(
                new ShipmentUpdate("parcel-1", "Departed", time.plusSeconds(120)),
                planner.mostRecentlyUpdatedShipment().orElseThrow());
    }
}

