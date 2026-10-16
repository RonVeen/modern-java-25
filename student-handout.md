# Student handout

## Simple, scalable concurrent programs

### The application

The Parcel Quote Aggregator creates a delivery quote from three simulated blocking services:

- routing returns distance, zone, and estimated delivery time;
- pricing returns the transport price and customer discount;
- customs returns clearance status and fees.

The calls use `Thread.sleep(Duration)` to simulate waiting for network responses. The delays and failures are deterministic so you can investigate concurrency without random test failures.

### Learning goals

By the end of this lab, you should be able to:

- identify a workload that benefits from virtual threads;
- use one virtual thread per task instead of pooling virtual threads;
- express parent/child task relationships with structured concurrency;
- fail fast and cancel sibling tasks;
- apply one deadline to a family of subtasks;
- propagate immutable request context using `ScopedValue`;
- explain why scoped bindings cannot leak into another request.

### Requirements

- JDK 25
- Maven 3.9 or newer

Structured concurrency is a preview API in JDK 25. The supplied Maven project already enables preview features when compiling and testing.

## Setup (5 minutes)

Open the `starter` project, verify the Java version, and run the tests:

```shell
java -version
mvn test
```

The tests should pass. Two exercise test classes in the **starter project** are
initially disabled and will be enabled later:

- `code/starter/src/test/java/workshop/concurrency/StructuredConcurrencyExerciseTest.java`
- `code/starter/src/test/java/workshop/concurrency/ScopedValueExerciseTest.java`

The solution project contains enabled, expanded versions under the same class
names. Make sure you opened `code/starter`, rather than only `code/solution`,
when following the exercises.

Run the application:

```shell
mvn -q -DskipTests package
java --enable-preview -cp target/classes workshop.concurrency.App
```

The application sends 100 quote requests through a fixed pool of eight platform threads. Each request performs three blocking calls sequentially.

Inspect these classes before editing:

- `App` creates the executor and starts the batch;
- `BatchRunner` submits one task for every quote request;
- `SequentialQuoteAggregator` coordinates the service calls;
- `RoutingService`, `PricingService`, and `CustomsService` simulate downstream systems;
- `ServiceProbe` records the thread and request context observed by each call.

## Exercise 1 — One virtual thread per quote (20 minutes)

### 1. Make the smallest migration

In `App`, replace:

```java
Executors.newFixedThreadPool(8)
```

with:

```java
Executors.newVirtualThreadPerTaskExecutor()
```

Run the application again and compare the elapsed time. The code in `SequentialQuoteAggregator` remains ordinary blocking, sequential Java.

### 2. Verify the thread type

Use the events recorded by `ServiceProbe`:

```java
var allVirtual = probe.events().stream()
        .allMatch(ServiceProbe.Event::virtualThread);
System.out.println("All service calls used virtual threads: " + allVirtual);
```

Each quote task keeps the same virtual thread while it calls the three services sequentially.

### 3. Keep both versions for comparison

Create two runs in `App`:

1. a fixed platform-thread pool of eight;
2. a virtual-thread-per-task executor.

Use a new `ServiceProbe` and aggregator for each run, or call `probe.clear()` between them.

### Questions

- Did an individual service call become faster?
- Why did the batch finish sooner?
- Would virtual threads help a CPU-bound calculation?
- Why is `newFixedThreadPool(10_000, virtualThreadFactory)` the wrong model?

Virtual threads are inexpensive tasks, not scarce resources to reuse. If a database accepts only 20 concurrent operations, use a `Semaphore` or connection pool to protect that resource; do not use a virtual-thread pool as an accidental rate limiter.

## Exercise 2 — Give the three calls a common lifetime (33 minutes)

The three service calls contribute to one quote, but the sequential implementation does not express that relationship. Open `StructuredQuoteAggregator`.

### 1. Open a fail-fast scope with a deadline

Import `java.util.concurrent.StructuredTaskScope` and replace the sequential method body with a structured task scope:

```java
try (var scope = StructuredTaskScope.open(
        StructuredTaskScope.Joiner.<Object>awaitAllSuccessfulOrThrow(),
        configuration -> configuration
                .withName("quote-" + request.requestId())
                .withTimeout(timeout))) {
    // Fork children here
}
```

The explicit joiner says that all children must succeed. If one fails, unfinished siblings are cancelled. The timeout applies to the scope as a whole.

### 2. Fork the children

Inside the scope, start the three calls:

```java
var routing = scope.fork(() -> routingService.route(request, context));
var pricing = scope.fork(() -> pricingService.price(request, context));
var customs = scope.fork(() -> customsService.check(request, context));
```

These return `Subtask` handles. They are not general-purpose futures to pass elsewhere; they belong to this scope.

### 3. Join before reading results

After forking:

```java
scope.join();

return DeliveryQuote.combine(
        request,
        routing.get(),
        pricing.get(),
        customs.get());
```

Do not call `get()` before `join()`. A successful `join()` guarantees that these subtasks succeeded under the selected join policy.

### 4. Translate failures

Add catches after the try-with-resources statement:

```java
catch (StructuredTaskScope.TimeoutException exception) {
    throw new QuoteTimeoutException(request.requestId(), exception);
} catch (StructuredTaskScope.FailedException exception) {
    throw new QuoteFailedException(request.requestId(), exception.getCause());
}
```

### 5. Enable the exercise tests

Open
`src/test/java/workshop/concurrency/StructuredConcurrencyExerciseTest.java`,
remove `@Disabled`, then run:

```shell
mvn -Dtest=StructuredConcurrencyExerciseTest test
```

The first test makes customs fail quickly while routing and pricing are slow. The scope should interrupt the unfinished siblings. The second gives customs a two-second delay but the whole scope only 75 milliseconds.

### 6. Use the structured aggregator

In `App`, create `StructuredQuoteAggregator` with a two-second timeout and run the batch with a virtual-thread-per-task executor. Compare it with the sequential virtual-thread run.

The two improvements solve different problems:

- virtual threads let many independent quote requests wait concurrently;
- structured concurrency lets the three children of one quote run concurrently and gives them one failure/cancellation policy.

### Questions

- Which code owns each child task?
- Can a child outlive the try-with-resources scope?
- Why must blocking operations respond to interruption for cancellation to be useful?
- Which deadline is clearer: three unrelated timeouts or one quote-level deadline?

## Exercise 3 — Propagate request context safely (22 minutes)

The starter passes `RequestContext` through the aggregator and every service method. The data is not part of routing or customs business input; it is ambient request metadata.

### 1. Implement the scoped context holder

Open `RequestContexts` and add a private key:

```java
private static final ScopedValue<RequestContext> CURRENT =
        ScopedValue.newInstance();
```

Implement lookup:

```java
public static RequestContext current() {
    return CURRENT.orElseThrow(() ->
            new IllegalStateException("No RequestContext is bound"));
}
```

Implement the bounded call:

```java
public static <T, X extends Throwable> T callWith(
        RequestContext context,
        ScopedValue.CallableOp<? extends T, X> operation) throws X {
    return ScopedValue.where(CURRENT, context).call(operation);
}
```

Open `src/test/java/workshop/concurrency/ScopedValueExerciseTest.java`, remove
`@Disabled`, and run it. It verifies that the value exists inside the operation
but not before or after it.

```shell
mvn -Dtest=ScopedValueExerciseTest test
```

### 2. Remove parameter plumbing

In each simulated service:

1. remove the `RequestContext` method parameter;
2. obtain it with `var context = RequestContexts.current()`;
3. continue passing that local context to `ServiceProbe.record`.

The resulting service signatures should resemble:

```java
RoutingResult route(QuoteRequest request) throws InterruptedException
```

### 3. Bind before opening the task scope

In `StructuredQuoteAggregator.quote`, bind the context around a private method:

```java
return RequestContexts.callWith(
        context,
        () -> quoteWithinBoundContext(request));
```

Move the structured-scope code into `quoteWithinBoundContext`. Remove explicit context arguments from the three service calls.

Update `SequentialQuoteAggregator` in the same way so that it still compiles and remains available for the platform-versus-virtual-thread comparison: bind the context around its three sequential service calls and remove their explicit context arguments.

The order is important: the scoped value must be bound before the structured scope is opened. The child tasks then inherit the binding.

### 4. Verify isolation

Run at least 100 quote requests on virtual threads. For every `ServiceProbe.Event`, verify:

```java
event.requestId().equals(event.contextRequestId())
```

Also verify that `RequestContexts.current()` throws outside a bound operation.
Compare your work with the enabled `ScopedValueExerciseTest` in the completed
solution.

### Questions

- Why is there no `set` or `remove` pair?
- Can a child replace the binding observed by its parent?
- Why should `RequestContext` be immutable?
- How does lexical lifetime prevent context leakage between reused code paths?

## Wrap-up (10 minutes)

Complete this table with a partner:

| Concern | Mechanism |
|---|---|
| Many blocking request tasks | Virtual thread per task |
| Limit access to a scarce downstream resource | Semaphore or resource-specific pool |
| Give related subtasks one lifetime | Structured task scope |
| Stop siblings after a failure | Fail-fast joiner and interruption |
| Bound the duration of the whole operation | Scope timeout |
| Share immutable request metadata | Scoped value |

### Final reminders

- Virtual threads improve throughput for highly concurrent waiting workloads; they are not faster CPU threads.
- Do not pool virtual threads.
- Cancellation is cooperative. Code must preserve and respond to interruption.
- Structured concurrency is preview in JDK 25 and requires `--enable-preview`.
- Scoped values are final in JDK 25 and are intended for one-way sharing of immutable data.
