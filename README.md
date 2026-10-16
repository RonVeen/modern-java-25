# Modern Java workshop — Part 2 code

## Simple, scalable concurrent programs

The two independent Maven projects contain the same parcel quote application at different stages:

- `starter/` — sequential quote aggregation executed by a small platform-thread pool, with request context passed explicitly
- `solution/` — virtual-thread-per-task execution, structured concurrency, deadlines, fail-fast cancellation, and scoped context

### Requirements

- JDK 25
- Maven 3.9 or newer

Structured concurrency is a preview API in JDK 25. Both projects enable preview features for compilation and testing.

### Verify

```shell
cd starter
mvn test

cd ../solution
mvn test
```

### Run the comparison demo

```shell
cd solution
mvn -q -DskipTests package
java --enable-preview -cp target/classes workshop.concurrency.App
```

The timings demonstrate relative behavior; they are not a production benchmark. The simulated services use `Thread.sleep(Duration)` to stand in for blocking network I/O.

