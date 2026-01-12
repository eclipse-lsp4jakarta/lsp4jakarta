package io.openliberty.sample.jakarta.jsonb;

import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;

public class SerializerClass implements JsonbSerializer<String> {
    @Override
    public void serialize(String obj, JsonGenerator generator, SerializationContext ctx) {
        generator.write(encrypt(obj)); // custom encrypt logic
    }

    private String encrypt(String input) {
        return "encrypted-" + input;
    }
}