package com.fasterxml.jackson.datatype.optional;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

import java.io.IOException;
import java.util.Optional;

public class OptionalDeserializer extends StdDeserializer<Optional<?>> implements ContextualDeserializer {
    protected final JavaType fullType;
    protected final JavaType referenceType;
    protected final JsonDeserializer<?> valueDeserializer;
    protected final TypeDeserializer valueTypeDeserializer;

    public OptionalDeserializer(JavaType fullType, JavaType refType, TypeDeserializer typeDeser, JsonDeserializer<?> valueDeser) {
        super(fullType);
        this.fullType = fullType;
        referenceType = refType;
        valueTypeDeserializer = typeDeser;
        valueDeserializer = valueDeser;
    }

    @Override
    public Optional<?> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        Object refd;
        if (valueTypeDeserializer == null)
            refd = valueDeserializer.deserialize(jp, ctxt);
        else
            refd = valueDeserializer.deserializeWithType(jp, ctxt, valueTypeDeserializer);
        return Optional.of(refd);
    }

    /* NOTE: usually should not need this method... but for some reason, it is needed here.
     */
    @Override
    public Optional<?> deserializeWithType(JsonParser jp, DeserializationContext ctxt, TypeDeserializer typeDeserializer) throws IOException {
        final JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_NULL)
            return getNullValue();

        //   This gets rather tricky with "natural" types
        //   (String, Integer, Boolean), which do NOT include type information.
        //   These might actually be handled ok except that nominal type here
        //   is `Optional`, so special handling is not invoked; instead, need
        //   to do a work-around here.
        if (t != null && t.isScalarValue())
            return deserialize(jp, ctxt);

        // with type deserializer to use here? Looks like we get passed same one?
        return Optional.of(typeDeserializer.deserializeTypedFromAny(jp, ctxt));
    }

    @Override
    public JavaType getValueType() { return fullType; }

    @Override
    public Optional<?> getNullValue() { return Optional.empty(); }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        JsonDeserializer<?> deser = valueDeserializer;
        TypeDeserializer typeDeser = valueTypeDeserializer;

        deser = ctxt.findContextualValueDeserializer(referenceType, property);
        if (typeDeser != null)
            typeDeser = typeDeser.forProperty(property);
        if (deser == valueDeserializer && typeDeser == valueTypeDeserializer)
            return this;
        return new OptionalDeserializer(fullType, referenceType, typeDeser, deser);
    }
}