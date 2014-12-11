package com.fasterxml.jackson.datatype.optional;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.util.Optional;

public class OptionalModule extends Module {
    public OptionalModule() {
        super();
    }

    @Override public String getModuleName() { return "OptionalModule"; }
    @Override public Version version() { return new Version(1,0,0,null); }

    @Override
    public void setupModule(SetupContext context) {
        context.addDeserializers(new OptionalDeserializerBase());
        context.addSerializers(new OptionalSerializerBase());
    }

    @Override
    public int hashCode()
    {
        return OptionalModule.class.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        return this == o;
    }

    public class OptionalSerializerBase extends Serializers.Base {
        @Override
        public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
            Class<?> raw = type.getRawClass();
            if(Optional.class.isAssignableFrom(raw))
                return new OptionalSerializer(type);
            return super.findSerializer(config, type, beanDesc);
        }
    }

    protected static class OptionalDeserializerBase extends Deserializers.Base {
        @Override
        public JsonDeserializer<?> findBeanDeserializer(final JavaType type, DeserializationConfig config,
                                                        BeanDescription beanDesc) throws JsonMappingException {
            Class<?> raw = type.getRawClass();
            if (raw == Optional.class) {
                JavaType[] types = config.getTypeFactory().findTypeParameters(type, Optional.class);
                JavaType refType = (types == null) ? TypeFactory.unknownType() : types[0];
                JsonDeserializer<?> valueDeser = type.getValueHandler();
                TypeDeserializer typeDeser = type.getTypeHandler();
                // Polymorphic types need type deserializer?
//                if (typeDeser == null) {
//                    typeDeser = config.findTypeDeserializer(refType);
//                }
                return new OptionalDeserializer(type, refType, typeDeser, valueDeser);
            }
            return super.findBeanDeserializer(type, config, beanDesc);
        }
    }
}
