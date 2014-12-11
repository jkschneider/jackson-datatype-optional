package com.fasterxml.jackson.datatype.optional

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.optional.OptionalModule
import groovy.transform.Canonical
import spock.lang.Specification

class OptionalModuleSpec extends Specification {
    @Canonical
    static class Person {
        String firstName
        Optional<String> middleName = Optional.empty()
        Optional<List<String>> nicknames = Optional.empty()
        Optional<Optional<String>> uhhh = Optional.empty()
    }

    def mapper = new ObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        .registerModule(new OptionalModule())

    void "Java 8 Optional is serializable and deserializable with Jackson"() {
        when:
        def p = new Person(firstName: "jon")
        def optional = Optional.of(p)
        def optionalPresentStr = mapper.writeValueAsString(optional)
        def optionalEmptyStr = mapper.writeValueAsString(Optional.empty())

        TypeReference<Optional<Person>> optionalPersonType = new TypeReference<Optional<Person>>() {};

        def optionalRead = mapper.readValue(optionalPresentStr, optionalPersonType);

        then:
        optionalRead.isPresent()
        optionalRead.get() == p
        !mapper.readValue(optionalEmptyStr, optionalPersonType).isPresent()
    }

    void "Nested Optional field is serialized as if it were a regular field if present"() {
        when:
        def p = new Person(middleName: Optional.of("k"))

        then:
        mapper.writeValueAsString(p) == """{"middleName":"k"}"""
    }

    void "Nested polymorphic type inside an Optional is serialized as if it were a regular field if present"() {
        when:
        def p = new Person(nicknames: Optional.of(['j', 'johnny']))

        then:
        mapper.writeValueAsString(p) == """{"nicknames":["j","johnny"]}"""
    }

    void "Recursive Optional is serialized as if it were a regular field if present"() {
        when:
        def p = new Person(uhhh: Optional.of(Optional.of('foo')))
        def p2 = new Person(uhhh: Optional.of(Optional.empty()))

        then:
        mapper.writeValueAsString(p) == """{"uhhh":"foo"}"""
        mapper.writeValueAsString(p2) == """{"uhhh":null}""" // TODO I think this is imperfect
    }
}
