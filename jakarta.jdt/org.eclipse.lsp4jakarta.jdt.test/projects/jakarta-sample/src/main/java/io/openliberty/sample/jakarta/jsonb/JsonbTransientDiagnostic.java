/******************************************************************************* 
* Copyright (c) 2022, 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Adit Rada, Yijia Jing - initial API and implementation
 *******************************************************************************/

package io.openliberty.sample.jakarta.jsonb;

import jakarta.json.bind.annotation.JsonbAnnotation;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;

public class JsonbTransientDiagnostic {
    @jakarta.json.bind.annotation.JsonbTransient
    private int id;

    @JsonbProperty("name")
    @JsonbTransient
    private String name;    // Diagnostic: JsonbTransient is mutually exclusive with other JsonB annotations

    @JsonbProperty("fav_lang")
    @JsonbAnnotation
    @JsonbTransient
    private String favoriteLanguage;    // Diagnostic: JsonbTransient is mutually exclusive with other JsonB annotations
    
    // No diagnostic as field is not annotated with other Jsonb annotations,
    // even though the accessors are annotated with @JsonbTransient
    private String favoriteDatabase;
    
    // Diagnostic will appear as field accessors have @JsonbTransient,
    // but field itself has annotation other than transient
    @JsonbProperty("fav_editor")
    private String favoriteEditor;
    
    @JsonbProperty("person-id")
    private int getId() { 
        // A diagnostic is expected on getId because as a getter, it is annotated with other 
        // Jsonb annotations while its corresponding field id is annotated with JsonbTransient
        return id;
    }
    
    @JsonbAnnotation
    private void setId(int id) {
        // A diagnostic is expected on setId because as a setter, it is annotated with other 
        // Jsonb annotations while its corresponding field id is annotated with JsonbTransient
        this.id = id;
    }
    
    @JsonbTransient
    private String getFavoriteDatabase() {
        return favoriteDatabase;
    }
    
    @JsonbTransient
    private void setFavoriteDatabase(String favoriteDatabase) {
        this.favoriteDatabase = favoriteDatabase;
    }
    
    // A diagnostic will appear as field has conflicting annotation
    @JsonbTransient
    private String getFavoriteEditor() {
        return favoriteEditor;
    }
    
    // A diagnostic will appear as @JsonbTransient is not mutually exclusive on this accessor
    @JsonbAnnotation
    @JsonbTransient
    private void setFavoriteEditor(String favoriteEditor) {
        this.favoriteEditor = favoriteEditor;
    }
    
    @JsonbProperty("title")
    private String title1;    // Diagnostic: @JsonbProperty property uniqueness, multiple properties cannot have same property names.

    @JsonbProperty("\u0074\u0069\u0074\u006C\u0065")
    private String title2;    // Diagnostic: @JsonbProperty property uniqueness, multiple properties cannot have same property names.
}