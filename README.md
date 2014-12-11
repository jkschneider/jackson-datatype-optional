# Jackson Optional Module

This module provides basic support for the Java8 `java.util.Optional`.  Fields whose values are empty `Optional` instances are not serialized.  Fields whose values are non-empty `Optional` instances are serialized as if they were instances of the parametric type.

Given the following type:

```java
static class Person {
   String firstName
   Optional<String> middleName = Optional.empty()
   Optional<List<String>> nicknames = Optional.empty()
   Optional<Optional<String>> uhhh = Optional.empty()
}
```

  1. `new Person(middleName: Optional.of("k"))` serializes to `{"middleName":"k"}`
 2. `new Person(nicknames: Optional.of(['j', 'johnny']))` serializes to `{"nicknames":["j","johnny"]}`
 3. `new Person(uhhh: Optional.of(Optional.of('foo')))` serializes to `{"uhhh":"foo"}`
 4.  `new Person(uhhh: Optional.of(Optional.empty()))` serializes to `{"uhhh":null}` (this could be improved to not serialize this field at all)

## Building the code

Running `gradlew build` will compile, run the tests, and build a jar.
