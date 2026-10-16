# Part 4 — Less ceremony for everyday Java

## What you will build

In this 90-minute lab you will improve a parcel-operations toolkit:

1. Replace a long import list with a module import and resolve a name collision.
2. Turn a conventional CSV reporting class into a compact source file with an instance `main`.
3. Complete an integration program combining ideas from all four workshop parts.

The aim is not “shortest code wins.” Decide where less ceremony clarifies intent and where explicit structure is valuable.

## Learning goals

You will learn to:

- use `import module java.base;` and explain its scope;
- distinguish importing types from declaring a module dependency;
- diagnose and resolve an ambiguous imported type;
- create and directly launch a compact source file;
- use command-line arguments with an instance `main`;
- judge where compact source is appropriate;
- combine sealed records, record patterns, Structured Concurrency, Scoped Values, and Stream Gatherers.

## Requirements and layout

You need JDK 25 and Maven:

```shell
java -version
mvn -version
```

The archive contains parallel `starter` and `solution` trees:

```text
starter/
  pom.xml
  src/                         Maven module-import exercise
  compact/DispatchReport.java  CSV report exercise
  integration/WorkshopFinal.java

solution/
  ...                          Completed versions
```

Work in `starter`. Consult `solution` after making an attempt or when the instructor asks.

## Timing

| Time | Activity |
|---|---|
| 0–5 min | Setup and baseline |
| 5–27 min | Exercise 1: module imports |
| 27–52 min | Exercise 2: compact source files |
| 52–57 min | Break and recap |
| 57–85 min | Exercise 3: integration |
| 85–90 min | Review |

---

## Exercise 1 — Import a module

### 1.1 Run the baseline

From `starter`:

```shell
mvn test
```

Both tests should pass. Open `src/main/java/workshop/ceremony/OperationsReport.java`. Its imports come from `java.util`, `java.util.function`, and `java.util.stream`, all exported by `java.base`.

### 1.2 Replace the imports

Delete the individual imports in `OperationsReport.java` and add:

```java
import module java.base;
```

Do not change the class body. Run `mvn test` again.

Question: did this add `java.base` as a dependency, or did it only change which public types are available by simple name in this source file?

> 

### 1.3 Understand the boundary

A module import makes public top-level types in packages exported by the named module available on demand. It is not:

- a static import;
- a replacement for `requires` in `module-info.java`;
- a way to import packages from class-path libraries in the unnamed module;
- a guarantee that every simple type name is unambiguous.

Navigate from `List`, `Collectors`, and `Function` in your IDE. One declaration now covers all three packages.

### 1.4 Create and resolve a conflict

Open `NamingConflictDemo.java`. It imports `java.base`, so `Date` resolves to `java.util.Date`.

Add:

```java
import module java.sql;
```

Run `mvn test`. The compiler reports that `Date` is ambiguous: `java.util.Date` and `java.sql.Date` arrived through on-demand module imports.

Resolve the conflict:

```java
import java.util.Date;
```

Run the tests again. A precise single-type import takes precedence over on-demand imports.

Optional: demonstrate that the other type remains available:

```java
public java.sql.Date serviceDay(LocalDate date) {
    return java.sql.Date.valueOf(date);
}
```

### 1.5 Make a design decision

| Context | Module or precise imports? | Reason |
|---|---|---|
| Teaching scratch file exploring the standard API |  |  |
| Small operational script |  |  |
| Security-sensitive production service |  |  |
| File using modules with common type names |  |  |

There is no universal rule. Consider codebase size, discoverability, stability, and team conventions.

---

## Exercise 2 — Create a compact source file

### 2.1 Run the conventional version

Move into `starter/compact`:

```shell
java DispatchReport.java scans.csv
```

Confirm six scans, 36 kg of observed weight, and two delayed shipments.

The file has six imports, a `public class` wrapper, a static `main`, static helpers, and a nested record.

### 2.2 Transform it

1. Remove `public class DispatchReport {` and its final matching brace.
2. Change the entry point:

   ```java
   void main(String[] args) throws IOException {
   ```

3. Remove `static` from `readScans`, `parse`, and `printSummary`.
4. Change `DispatchReport::parse` to `this::parse`.
5. Remove all imports.

A compact source file automatically imports on demand the public top-level types in packages exported by `java.base`. The result has this shape:

```java
void main(String[] args) throws IOException {
    // ...
}

List<Scan> readScans(Path path) throws IOException {
    // ...
}

Scan parse(String line) {
    // ...
}

void printSummary(Path source, List<Scan> scans) {
    // ...
}

record Scan(String trackingId, String hub, String status, int weightKg) {
}
```

Run it:

```shell
java DispatchReport.java scans.csv
```

The behavior should be unchanged.

### 2.3 What did Java supply?

The class did not disappear. The compiler supplies an implicitly declared final top-level class in the unnamed package. The methods and record are members of it.

- `this` is available in an instance `main`.
- The implicitly declared class cannot be named in Java source.
- You cannot construct another instance of it by name.
- A compact source file must provide a launchable `main`.
- Compact source is ordinary Java, not a separate scripting language.

### 2.4 Exercise error paths

Run without an argument:

```shell
java DispatchReport.java
```

Make a copy of `scans.csv`, add a malformed row, and run it. Should a real operations tool print a row number, skip bad data, or fail immediately?

### 2.5 Choose an appropriate home

| Statement | Agree / disagree |
|---|---|
| A focused command-line utility is a good compact-source candidate. |  |
| Every production service should remove its named entry-point class. |  |
| Compact files help teaching by postponing class-level ceremony. |  |
| A growing tool should adopt packages and public APIs when those boundaries become useful. |  |

Be ready to defend one answer.

---

## Exercise 3 — Final integration challenge

The starter runs before refactoring. From `starter/integration`:

```shell
java WorkshopFinal.java parcel P-901 12
java WorkshopFinal.java freight F-204 420 NON_EU
```

It parses a shipment, obtains three quotes concurrently, and batches scan events. Find the three `LAB` comments.

### 3.1 Close the shipment model

Seal the hierarchy:

```java
sealed interface Shipment permits Parcel, Freight {
    String id();
    int weightKg();
}
```

Replace the cast chain in `describe`:

```java
return switch (shipment) {
    case Parcel(String id, int weightKg) ->
            "Parcel %s (%d kg)".formatted(id, weightKg);
    case Freight(String id, int weightKg, String zone) ->
            "Freight %s (%d kg, zone %s)".formatted(id, weightKg, zone);
};
```

No `default` is needed because the alternatives are known.

### 3.2 Bind request context

Add a field:

```java
final ScopedValue<RequestContext> CURRENT_REQUEST = ScopedValue.newInstance();
```

Bind around the quote:

```java
Quote quote = ScopedValue.where(CURRENT_REQUEST, context)
        .call(() -> quote(shipment));
```

Remove context parameters from `quote` and the service methods. Read it inside `trace`:

```java
RequestContext context = CURRENT_REQUEST.orElseThrow(
        () -> new IllegalStateException("No request context is bound"));
```

### 3.3 Give the children one owner

Replace the executor and futures:

```java
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
```

Structured Concurrency is preview in JDK 25:

```shell
java --enable-preview WorkshopFinal.java parcel P-901 12
```

Trace-line order can change because the child tasks run concurrently.

### 3.4 Batch events with a Gatherer

Replace the indexing loop:

```java
List<List<ScanEvent>> batch(List<ScanEvent> events, int size) {
    return events.stream()
            .gather(Gatherers.windowFixed(size))
            .toList();
}
```

Five events should produce two full batches and one one-item batch.

### 3.5 Definition of done

Your program now demonstrates:

- compact source and instance `main`;
- sealed records and an exhaustive record-pattern switch;
- three quote children in one Structured Task Scope;
- request metadata propagated with a Scoped Value;
- batching with a Stream Gatherer.

Run both forms:

```shell
java --enable-preview WorkshopFinal.java parcel P-901 12
java --enable-preview WorkshopFinal.java freight F-204 420 NON_EU
```

If time is short, compare with `solution/integration/WorkshopFinal.java`.

---

## Wrap-up

1. What does `import module java.base;` change, and what does it not change?
2. Why does a precise import solve the `Date` collision?
3. Give one good and one poor compact-source use case.
4. Which integration feature needs `--enable-preview` in JDK 25?
5. Where would explicitness be preferable to reduced ceremony?

## Further reading

- [JEP 511: Module Import Declarations](https://openjdk.org/jeps/511)
- [Java 25: Module Import Declarations](https://docs.oracle.com/en/java/javase/25/language/module-import-declarations.html)
- [JEP 512: Compact Source Files and Instance Main Methods](https://openjdk.org/jeps/512)
- [Java 25: Compact Source Files and Instance main Methods](https://docs.oracle.com/en/java/javase/25/language/compact-source-files-instance-main-methods.html)
