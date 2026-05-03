package io.openliberty.sample.jakarta.jsonp;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonValue;

public class CreateInvalidJsonArrayBuilder {
	
	public static void makePointers() {

	JsonArrayBuilder builder = Json.createArrayBuilder();
	builder.add("Bob");

	JsonArrayBuilder builder1 = Json.createArrayBuilder();
	builder1.add(JsonValue.NULL);

	JsonArrayBuilder builder2 = Json.createArrayBuilder();
	builder2.add((String) null);
	
	JsonArrayBuilder builder3 = Json.createArrayBuilder();
	builder2.add(null);
	
	}
	
}
