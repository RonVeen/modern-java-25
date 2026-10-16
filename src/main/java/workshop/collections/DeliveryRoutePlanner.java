package workshop.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedCollection;

public final class DeliveryRoutePlanner {
    // TODO LAB 1: Express the ordering contract in these field types.
    private final List<DeliveryStop> pendingStops = new ArrayList<>();
    private final List<DeliveryStop> completedStops = new ArrayList<>();
    private final Map<String, ShipmentUpdate> updates = new LinkedHashMap<>();

    public void addStop(DeliveryStop stop) {
        if (!pendingStops.contains(stop)) {
            pendingStops.add(stop);
        }
    }

    // TODO LAB 1: Use a first-element operation instead of index zero.
    public Optional<DeliveryStop> nextStop() {
        return pendingStops.isEmpty()
                ? Optional.empty()
                : Optional.of(pendingStops.get(0));
    }

    public Optional<DeliveryStop> completeNextStop() {
        if (pendingStops.isEmpty()) {
            return Optional.empty();
        }
        var completed = pendingStops.remove(0);
        completedStops.add(completed);
        return Optional.of(completed);
    }

    // TODO LAB 1: Read the last completed stop directly.
    public Optional<DeliveryStop> previousStop() {
        return completedStops.isEmpty()
                ? Optional.empty()
                : Optional.of(completedStops.get(completedStops.size() - 1));
    }

    // TODO LAB 1: Return a reverse-ordered view instead of copying and sorting.
    public SequencedCollection<DeliveryStop> reversedRemainingRoute() {
        var copy = new ArrayList<>(pendingStops);
        Collections.reverse(copy);
        return Collections.unmodifiableSequencedCollection(copy);
    }

    // TODO LAB 1: Move an existing stop to the front with addFirst.
    public void promoteUrgentDelivery(DeliveryStop urgentStop) {
        pendingStops.remove(urgentStop);
        pendingStops.add(0, urgentStop);
    }

    // TODO LAB 1: Move the updated mapping to the end with putLast.
    public void recordUpdate(ShipmentUpdate update) {
        updates.remove(update.shipmentId());
        updates.put(update.shipmentId(), update);
    }

    // TODO LAB 1: Use lastEntry instead of manually walking every entry.
    public Optional<ShipmentUpdate> mostRecentlyUpdatedShipment() {
        ShipmentUpdate mostRecent = null;
        for (var entry : updates.entrySet()) {
            mostRecent = entry.getValue();
        }
        return Optional.ofNullable(mostRecent);
    }

    public List<DeliveryStop> remainingStops() {
        return List.copyOf(pendingStops);
    }
}

