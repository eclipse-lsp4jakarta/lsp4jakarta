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
 *     IBM Corporation, Archana Iyer - initial API and implementation
 *******************************************************************************/
package io.openliberty.sample.jakarta.jsonp;

import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;

public class CreateInvalidJsonObjectBuilder {

    public static void makePointers() {
        JsonBuilderFactory factory = Json.createBuilderFactory(null);
    	JsonObject value = factory.createObjectBuilder().add(null, "hello").build();
    	
    	if(value.isEmpty()) {
    		System.out.print(false);
    	}
    	
    	JsonBuilderFactory factory2 = Json.createBuilderFactory(null);
    	JsonObject value2= factory2.createObjectBuilder().add(null, "hello").build();
    	
    	if(value2.isEmpty()) {
    		System.out.print(false);
    	}
    }
}
