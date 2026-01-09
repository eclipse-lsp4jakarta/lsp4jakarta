package io.openliberty.sample.jakarta.jsonb;

import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.bind.serializer.DeserializationContext;
import jakarta.json.stream.JsonParser;
import java.lang.reflect.Type;

public class DeserializerClass implements JsonbDeserializer<String> {
    @Override
    public String deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
        return decrypt(parser.getString()); 
     }

    private String decrypt(String input) {
        return "decrypted-" + input;
    }
}