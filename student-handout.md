# Student handout

## Modeling data: Data-oriented programming

### Goal

Modern Java lets us describe a domain as data and then process that data directly. In this lab you will modernize a parcel-pricing application without changing its observable behaviour.

By the end, you should be able to:

- recognize when a class should be a record;
- model a closed set of alternatives with a sealed hierarchy;
- use switch expressions with simple values and type patterns;
- deconstruct nested records with record patterns;
- validate constructor arguments before calling `super(...)`;
- use primitive patterns when an exact conversion is required.

### Setup

You need JDK 25 and Maven. Confirm the Java version and run the baseline tests:

```shell
java -version
cd starter
mvn test
```

All tests should pass before you edit anything. Maven is already configured to enable JDK 25 preview features.

## Exercise 1 — Turn a data carrier into a record (12 minutes)

Open `domain/Address.java`. It is a conventional immutable class whose identity is entirely defined by three values.

1. Replace the class declaration, fields, accessors, `equals`, `hashCode`, and `toString` with this record header:

   ```java
   public record Address(String country, String city, String postalCode)
   ```

2. Preserve validation and normalization with a compact constructor.
3. Remember that assigning to a compact constructor parameter changes the value stored in the generated field.
4. Keep `requireText` as a static helper.
5. Run:

   ```shell
   mvn -Dtest=AddressTest test
   ```

Discuss with a partner:

- What did the compiler generate?
- Is a record deeply immutable?
- Why is `Address` a suitable record while an entity such as a mutable delivery route might not be?

## Exercise 2 — Close the shipment hierarchy (12 minutes)

Open `domain/Shipment.java`.

1. Make `Shipment` a `sealed interface`.
2. Permit `Letter`, `Parcel`, and `ColdChainParcel`.
3. Inspect each permitted implementation. Why are the two records automatically final? Why must `ColdChainParcel` explicitly be `final`, `sealed`, or `non-sealed`?
4. Create a temporary class that implements `Shipment` but is not permitted. Compile and read the compiler error, then delete the temporary class.
5. Run the complete test suite again.

The point is not inheritance control by itself. The compiler now knows every possible kind of shipment.

## Exercise 3 — Process the closed model with patterns (23 minutes)

Open `service/QuoteService.java`.

### 3A: A switch expression over a simple type

Refactor `serviceSurcharge` from an `if/else` chain to a switch expression over `String`:

```java
return switch (serviceLevel) {
    case "STANDARD" -> ...;
    case "EXPRESS" -> ...;
    case "SAME_DAY" -> ...;
    default -> throw ...;
};
```

Notice that this switch produces a value and has no accidental fall-through.

### 3B: Type patterns and exhaustiveness

Refactor `quote` to return a switch expression over `shipment`.

1. Start with type patterns: `case Letter letter`, `case Parcel parcel`, and `case ColdChainParcel cold`.
2. Because `Shipment` is sealed and all permitted types are covered, omit `default`.
3. Run `mvn -Dtest=QuoteServiceTest test`.
4. Comment out one case and read the exhaustiveness error.
5. Put the case back.

### 3C: Record patterns and guards

Improve the `Parcel` cases by destructuring their components directly:

```java
case Parcel(
        var trackingId,
        Address(var country, var city, var postalCode),
        Dimensions(var length, var width, var height),
        Weight(var grams)) when ... -> ...
```

1. Put the oversized case before the general parcel case.
2. Use a `when` guard for volume greater than `100_000` cubic centimetres.
3. Add an unguarded `Parcel(...)` case for all remaining parcels.
4. Pass primitive `grams` to a revised `weightCharge(long grams)` helper.
5. Run the quote tests.

Questions:

- What happens if the general parcel case appears first?
- Why is a guard a good fit for “same shape, additional condition”?
- What happens when the selector is `null`?

## Exercise 4 — Validate before superclass construction (12 minutes)

Open `domain/ColdChainParcel.java`. Before Java 25, an explicit `super(...)` or `this(...)` call had to be the first constructor statement.

Refactor the constructor:

1. Normalize the tracking ID into a local variable.
2. Check `destination`, `dimensions`, and `weight` for null.
3. Validate the temperature range.
4. Perform all those steps before `super(normalizedTrackingId)`.
5. Leave field assignment after the superclass call.
6. Run `mvn -Dtest=ColdChainParcelTest test`.

Try referring to `this` before `super(...)`. Keep the resulting compiler message in mind, then undo the change. The object exists in an early construction context, but it is not safe to use the not-yet-constructed instance.

## Exercise 5 — Match primitive values exactly (12 minutes)

Primitive patterns are a preview feature in JDK 25.

Open `service/WeightClassifier.java`. Refactor the range-checking ladder to a switch over the `long` selector:

```java
return switch (grams) {
    case long value when value < 0 -> throw ...;
    case byte ignored -> "byte";
    case short ignored -> "short";
    case int ignored -> "int";
    case long ignored -> "long";
};
```

A narrowing primitive pattern matches only when the conversion is exact. For example, `127L` matches `byte`, but `128L` does not.

Run:

```shell
mvn -Dtest=WeightClassifierTest test
```

Experiment with boundary values and reorder the cases. Explain the compiler's dominance checks.

## Final challenge — Evolve the domain (10 minutes or take-home)

Add this new shipment kind:

```java
record FragileParcel(
        String trackingId,
        Address destination,
        Dimensions dimensions,
        Weight weight,
        String handlingInstruction) implements Shipment {
}
```

1. Add it to the `permits` clause.
2. Observe the compiler error in the exhaustive quote switch.
3. Add a record-pattern case that charges a base price of 1,200 cents and includes the handling instruction in the description.
4. Add a unit test.

The compiler error is the lesson: changing a closed data model reveals every exhaustive operation that must now be reconsidered.

## Takeaways

- Records describe transparent data aggregates; they are not merely boilerplate reduction.
- Sealed types describe a closed choice between alternatives.
- Pattern switches are operations over those alternatives.
- Record patterns separate data navigation from business logic.
- Flexible constructor bodies improve validation while preserving safe construction.
- Primitive patterns express exact conversion tests, but remain preview in JDK 25.

