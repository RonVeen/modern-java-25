# Student handout

## More capable collection processing

### The application

This module contains two connected parts of a parcel-delivery operation:

1. `DeliveryRoutePlanner` keeps pending stops, completed stops, and shipment updates in encounter order.
2. `ParcelScanProcessor` turns a stream of scan events into dispatch batches, failure alerts, and running weight totals.

The starter is already correct. Your task is to preserve its behavior while replacing incidental indexing, copying, and manual state management with APIs introduced after Java 17.

### Learning goals

By the end of the lab, you should be able to:

- select `SequencedCollection`, `SequencedSet`, or `SequencedMap` when encounter order is part of a contract;
- access and modify both ends without index arithmetic;
- use a reverse-ordered view and explain how it differs from a copy;
- use `Gatherers.windowFixed`, `windowSliding`, and `scan`;
- explain the difference between a gatherer and a collector;
- build a sequential custom gatherer with private per-stream state.

### Feature versions

- Sequenced collections were finalized in Java 21.
- Stream gatherers were finalized in Java 24.
- This project targets Java 25 and requires no preview flags.

## Setup (5 minutes)

Open the `starter` project and run:

```shell
java -version
mvn test
```

All six tests should pass. Then run the example:

```shell
mvn -q -DskipTests package
java -cp target/classes workshop.collections.App
```

Search for `TODO LAB`. The tests let you refactor one method at a time without changing its behavior.

## Exercise 1 — Make encounter order part of the API (35 minutes)

Open `DeliveryRoutePlanner`.

### 1. Choose ordered abstractions

The starter declares pending and completed stops as `List`, even though it needs uniqueness and operations at both ends. Change them to:

```java
private final SequencedSet<DeliveryStop> pendingStops = new LinkedHashSet<>();
private final SequencedSet<DeliveryStop> completedStops = new LinkedHashSet<>();
```

Change the update history to:

```java
private final SequencedMap<String, ShipmentUpdate> updates =
        new LinkedHashMap<>();
```

Add the required imports and remove unused ones.

Why use the interface types?

- `SequencedSet` documents both uniqueness and encounter order.
- `SequencedMap` documents that entry order is meaningful.
- `LinkedHashSet` and `LinkedHashMap` supply insertion-ordered implementations.

### 2. Add and retrieve at the front or back

Refactor these methods:

- `addStop`: use `pendingStops.addLast(stop)`;
- `nextStop`: use `pendingStops.getFirst()` after the existing empty check;
- `completeNextStop`: use `pendingStops.removeFirst()` and `completedStops.addLast(completed)`;
- `previousStop`: use `completedStops.getLast()` after the empty check.

Run:

```shell
mvn -Dtest=DeliveryRoutePlannerTest test
```

`getFirst`, `getLast`, `removeFirst`, and `removeLast` throw `NoSuchElementException` on an empty collection. This application deliberately retains its `Optional` API, so it checks emptiness first.

### 3. Promote an urgent stop

Replace the manual remove-and-indexed-add sequence with:

```java
pendingStops.addFirst(urgentStop);
```

For a `LinkedHashSet`, adding an existing element at the front repositions it rather than duplicating it.

### 4. Reverse the route without copying it

Replace the copied list and `Collections.reverse` with:

```java
return Collections.unmodifiableSequencedCollection(
        pendingStops.reversed());
```

`reversed()` returns a view. It does not copy or sort the elements. If the underlying route changes, the view reflects that change. The unmodifiable wrapper prevents callers from changing the planner through the returned view.

Experiment:

1. Store the returned reversed view in a variable.
2. Add another stop to the planner.
3. Print the same view again.

### 5. Track recency with a sequenced map

Refactor `recordUpdate`:

```java
updates.putLast(update.shipmentId(), update);
```

If the shipment key already exists, `putLast` replaces its value and moves the mapping to the end.

Refactor `mostRecentlyUpdatedShipment`:

```java
var entry = updates.lastEntry();
return entry == null
        ? Optional.empty()
        : Optional.of(entry.getValue());
```

Run the route-planner tests again.

### Questions

- How is encounter order different from sorted order?
- Why is `HashSet` not a `SequencedSet`?
- Is every operation on `SequencedCollection` guaranteed to be supported?
- When should an API return a reversed view rather than a snapshot?
- Does `SequencedSet.equals` take encounter order into account?

## Exercise 2 — Add missing stream operations with gatherers (40 minutes)

Open `ParcelScanProcessor`.

A collector is a terminal operation: it consumes the stream and produces a final result. A gatherer is an intermediate operation: it transforms one stream into another, so more mapping, filtering, gathering, or terminal processing can follow.

### 1. Create fixed dispatch batches

Replace the loop in `dispatchBatches` with:

```java
return events.stream()
        .gather(Gatherers.windowFixed(batchSize))
        .map(DispatchBatch::new)
        .toList();
```

Import `java.util.stream.Gatherers`.

Run:

```shell
mvn -Dtest=ParcelScanProcessorTest#createsFixedDispatchBatchesIncludingAPartialLastBatch test
```

Important behavior:

- the final window is emitted even when it is smaller than the requested size;
- each emitted window is an unmodifiable list;
- a window size smaller than one causes `IllegalArgumentException`.

The explicit validation in the starter is therefore no longer necessary; the gatherer enforces the same precondition.

### 2. Detect overlapping failure windows

Replace the index loop in `repeatedFailureWindows`:

```java
return events.stream()
        .gather(Gatherers.windowSliding(3))
        .filter(this::isThreeFailuresForOneParcel)
        .map(window -> {
            var last = window.getLast();
            return new FailureAlert(
                    last.trackingId(), last.scannedAt(), 3);
        })
        .toList();
```

Run the matching test. The sample has four consecutive failed scans for the same parcel. Sliding windows therefore produce two alerts:

```text
failures 1–3
failures 2–4
```

If a stream contains fewer than three elements, `windowSliding(3)` can emit one short window. The existing predicate checks `window.size() == 3`, so it cannot produce a false alert.

### 3. Emit running totals with a prefix scan

Replace the mutable accumulator in `runningWeightTotals`:

```java
return events.stream()
        .gather(Gatherers.scan(
                () -> 0,
                (total, event) -> total + event.weightKg()))
        .toList();
```

Run all scan-processor tests.

`scan` emits the updated accumulated value after every input element. This differs from `reduce` or `Gatherers.fold`, which normally yields only the final accumulated result.

### Questions

- Why could `windowFixed` not be expressed as an ordinary one-to-one `map`?
- How many windows does a seven-element stream produce with a fixed size of three?
- How many full sliding windows of size three does a seven-element stream produce?
- When would `scan` be more useful than a final reduction?
- Can another stream operation follow `gather`?

## Extension — Write a custom gatherer (10 minutes)

The built-in sliding-window operation emits overlapping alerts. Suppose operations wants only one alert at the moment a failure streak first reaches three.

Create a small mutable state class named `FailureStreak`:

- store the current tracking ID and failure count;
- reset both values for a successful event;
- increment the count when the same parcel fails again;
- start at one when a different parcel fails.

Then add this method to `ParcelScanProcessor`:

```java
public Gatherer<ParcelScanEvent, ?, FailureAlert>
        threeConsecutiveFailures() {
    return Gatherer.ofSequential(
            FailureStreak::new,
            (streak, event, downstream) -> {
                streak.accept(event);
                if (streak.count() == 3) {
                    return downstream.push(new FailureAlert(
                            event.trackingId(),
                            event.scannedAt(),
                            streak.count()));
                }
                return true;
            });
}
```

Refactor `consecutiveFailureAlerts`:

```java
return events.stream()
        .gather(threeConsecutiveFailures())
        .toList();
```

The components are:

- initializer: creates private state for a stream evaluation;
- integrator: consumes one event and may emit zero or one alerts;
- downstream: receives emitted values;
- boolean result: tells the upstream pipeline whether the downstream wants more input.

`ofSequential` is appropriate because the state machine depends on encounter order and this exercise does not define how to combine partial streaks from parallel partitions.

## Wrap-up

Complete this comparison:

| Requirement | API |
|---|---|
| First or last ordered element | `getFirst` / `getLast` |
| Add or move an item at either end | `addFirst` / `addLast` |
| Most recently positioned map entry | `lastEntry` |
| Traverse the same data in reverse | `reversed()` view |
| Non-overlapping batches | `windowFixed` |
| Overlapping neighborhoods | `windowSliding` |
| Running intermediate totals | `scan` |
| Domain-specific intermediate operation | Custom `Gatherer` |

The main design lesson is to state ordering requirements directly. Once the collection contract exposes encounter order, code at the ends and in reverse becomes simpler. Once streams have an extensible intermediate operation, stateful transformations no longer have to become manual outer loops.

