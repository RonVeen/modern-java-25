# Modern Java workshop — Part 1

## Modeling data: Data-oriented programming

This 90-minute module uses a parcel-delivery domain to introduce the language features that connect records, sealed hierarchies, and pattern matching.

### Contents

- `starter/` — working Java 17-style implementation to refactor during the lab
- `solution/` — completed Java 25 implementation, including the optional `FragileParcel` challenge
- `student-handout.md` — participant-facing instructions
- `instructor-guide.md` — timings, explanations, demonstrations, and debrief notes

### Requirements

- JDK 25
- Maven 3.9 or newer

Primitive patterns are a preview feature in JDK 25. Both Maven projects are already configured with `--enable-preview`.

### Verify the projects

```shell
cd starter
mvn test

cd ../solution
mvn test
```

The starter is deliberately functional. Participants use its tests as a safety net while changing the implementation. Search for `TODO LAB` to find the exercise locations.

