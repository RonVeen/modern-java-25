package workshop.collections;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.SequencedSet;

public final class DeliveryRoutePlanner {
    private final SequencedSet<DeliveryStop> pendingStops = new LinkedHashSet<>();
    private final SequencedSet<DeliveryStop> completedStops = new LinkedHashSet<>();
    private final SequencedMap<String, ShipmentUpdate> updates = new LinkedHashMap<>();

    public void addStop(DeliveryStop stop) {
        pendingStops.addLast(stop);
    }

    public Optional<DeliveryStop> nextStop() {
        return pendingStops.isEmpty()
                ? Optional.empty()
                : Optional.of(pendingStops.getFirst());
    }

    public Optional<DeliveryStop> completeNextStop() {
        if (pendingStops.isEmpty()) {
            return Optional.empty();
        }
        var completed = pendingStops.removeFirst();
        completedStops.addLast(completed);
        return Optional.of(completed);
    }

    public Optional<DeliveryStop> previousStop() {
        return completedStops.isEmpty()
                ? Optional.empty()
                : Optional.of(completedStops.getLast());
    }

    public SequencedCollection<DeliveryStop> reversedRemainingRoute() {
        return Collections.unmodifiableSequencedCollection(pendingStops.reversed());
    }

    public void promoteUrgentDelivery(DeliveryStop urgentStop) {
        pendingStops.addFirst(urgentStop);
    }

    public void recordUpdate(ShipmentUpdate update) {
        updates.putLast(update.shipmentId(), update);
    }

    public Optional<ShipmentUpdate> mostRecentlyUpdatedShipment() {
        var entry = updates.lastEntry();
        return entry == null ? Optional.empty() : Optional.of(entry.getValue());
    }

    public List<DeliveryStop> remainingStops() {
        return List.copyOf(pendingStops);
    }
}

