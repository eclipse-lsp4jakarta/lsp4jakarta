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
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package io.openliberty.sample.jakarta.jsonb;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

/**
 * Test class for Jsonb.fromJson() null parameter diagnostics.
 * According to Jakarta JSON Binding specification, fromJson() must not accept null parameters.
 */
public class JsonbFromJsonNullParameter {

    static class Person {
        private int id;
        private String name;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public void testNullFirstParameter() {
        try (Jsonb jsonb = JsonbBuilder.create()) {
            // ERROR: First parameter (JSON input) is null
            Person p = jsonb.fromJson((String) null, Person.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testNullSecondParameter() {
        try (Jsonb jsonb = JsonbBuilder.create()) {
            String json = "{ \"id\": 1, \"name\": \"Test\" }";
            // ERROR: Second parameter (target type) is null
            Object obj = jsonb.fromJson(json, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testBothParametersNull() {
        try (Jsonb jsonb = JsonbBuilder.create()) {
            // ERROR: Both parameters are null
            Object obj = jsonb.fromJson(null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testNullWithoutCast() {
        try (Jsonb jsonb = JsonbBuilder.create()) {
            // ERROR: First parameter is null without cast
            Person p = jsonb.fromJson(null, Person.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void testValidUsage() {
        try (Jsonb jsonb = JsonbBuilder.create()) {
            String json = "{ \"id\": 1, \"name\": \"Valid\" }";
            // VALID: Both parameters are non-null
            Person p = jsonb.fromJson(json, Person.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}