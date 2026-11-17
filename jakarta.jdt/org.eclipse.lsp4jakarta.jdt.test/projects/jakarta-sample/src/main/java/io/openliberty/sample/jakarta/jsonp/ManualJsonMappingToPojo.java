/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package io.openliberty.sample.jakarta.jsonp;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

public class ManualJsonMappingToPojo {

    public static class User {
        public String email;
        private String name;
        private int age;

        public User() {}

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }

    // Diagnostic expected: values read from the JsonObject variable are copied into setters
    public User mapThroughVariable(String jsonString) {
        JsonReader reader = Json.createReader(new StringReader(jsonString));
        JsonObject json = reader.readObject();

        User user = new User();
        user.setName(json.getString("name"));
        user.setAge(json.getInt("age"));
        return user;
    }

    // Diagnostic expected: chained createReader().readObject()
    public User mapThroughChainedCall(String jsonString) {
        JsonObject json = Json.createReader(new StringReader(jsonString)).readObject();

        User user = new User();
        user.setName(json.getString("name"));
        user.setAge(json.getInt("age"));
        return user;
    }

    // Diagnostic expected: values are passed to a constructor
    public User mapThroughConstructor(String jsonString) {
        JsonReader reader = Json.createReader(new StringReader(jsonString));
        JsonObject json = reader.readObject();
        return new User(json.getString("name"), json.getInt("age"));
    }

    // Diagnostic expected: values are assigned to fields
    public User mapThroughFieldAssignment(String jsonString) {
        JsonReader reader = Json.createReader(new StringReader(jsonString));
        JsonObject json = reader.readObject();

        User user = new User();
        user.email = json.getString("email");
        user.setAge(json.getInt("age"));
        return user;
    }

    // Valid: JSON-B binds directly to the POJO
    public User mapWithJsonb(String jsonString) {
        Jsonb jsonb = JsonbBuilder.create();
        return jsonb.fromJson(jsonString, User.class);
    }

    // Diagnostic expected: even a single value is copied into the object
    public User mapSingleValue(String jsonString) {
        JsonReader reader = Json.createReader(new StringReader(jsonString));
        JsonObject json = reader.readObject();

        User user = new User();
        user.setName(json.getString("name"));
        return user;
    }

    // Valid: values are not copied into an object, they are only inspected
    public boolean inspectValues(String jsonString) {
        JsonReader reader = Json.createReader(new StringReader(jsonString));
        JsonObject json = reader.readObject();
        return json.getString("name").isEmpty() || json.getInt("age") < 0;
    }

    // Valid: schema is not known in advance, the object is processed generically
    public Map<String, String> readGenerically(String jsonString) {
        JsonReader reader = Json.createReader(new StringReader(jsonString));
        JsonObject json = reader.readObject();

        Map<String, String> values = new HashMap<>();
        for (String key : json.keySet()) {
            values.put(key, json.get(key).toString());
        }
        return values;
    }

    // Valid: getters are called on a JsonObject that was not produced by readObject() in this method
    public User mapFromParameter(JsonObject json) {
        User user = new User();
        user.setName(json.getString("name"));
        user.setAge(json.getInt("age"));
        return user;
    }

    // Diagnostic expected: values are read with other JsonObject getters and converted before copying
    public User mapThroughJsonValues(String jsonString) {
        JsonReader reader = Json.createReader(new StringReader(jsonString));
        JsonObject json = reader.readObject();

        User user = new User();
        user.setName(json.getJsonString("name").getString());
        user.setAge(json.getJsonNumber("age").intValue());
        return user;
    }

    // Valid: raw JSON values are stored as they are, nothing is converted into plain values
    public void storeRawJson(String jsonString, java.util.List<Object> store) {
        JsonReader reader = Json.createReader(new StringReader(jsonString));
        JsonObject json = reader.readObject();

        Holder holder = new Holder();
        holder.setAddress(json.getJsonObject("address"));
        holder.setScores(json.getJsonArray("scores"));
        store.add(holder);
    }

    public static class Holder {
        private JsonObject address;
        private jakarta.json.JsonArray scores;

        public void setAddress(JsonObject address) { this.address = address; }
        public void setScores(jakarta.json.JsonArray scores) { this.scores = scores; }
    }

}
