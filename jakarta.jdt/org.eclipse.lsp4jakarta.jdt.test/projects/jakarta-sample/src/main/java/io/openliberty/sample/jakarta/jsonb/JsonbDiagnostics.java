/******************************************************************************* 
* Copyright (c) 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Archana Iyer - initial API and implementation
 *******************************************************************************/

package io.openliberty.sample.jakarta.jsonb;

import jakarta.json.bind.annotation.JsonbAnnotation;
import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.json.bind.annotation.JsonbNillable;
import jakarta.json.bind.annotation.JsonbNumberFormat;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbPropertyOrder;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.json.bind.annotation.JsonbTypeAdapter;
import jakarta.json.bind.annotation.JsonbTypeDeserializer;
import jakarta.json.bind.annotation.JsonbTypeSerializer;
import jakarta.json.bind.annotation.JsonbVisibility;

@JsonbPropertyOrder({"id", "name", "favoriteLanguage", "favoriteDatabase", "favoriteEditor", "title1", "title2"})
@JsonbVisibility(VisbilityClass.class)
@JsonbNillable
public class JsonbDiagnostics {
 
    // Diagnostic will appear as field accessors have @JsonbTransient,
    // but field itself has annotation other than transient
    @JsonbProperty("fav_editor")
    private String favoriteEditor;
    
    
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
    private String title1;

    @JsonbProperty("title2")
    private String title2;   
    
    @JsonbTypeAdapter(DateAdapter.class)
    private String givenDate;
    
	@JsonbTransient
    @JsonbCreator
    private String getTitle1() {
    	return title1;
    }
    
    @JsonbTransient
    @JsonbDateFormat
    @JsonbNumberFormat
    private void setTitle1(String title1){
    	this.title1 = title1;
    }
    
    @JsonbTransient
    private String getTitle2() {
    	return title2;
    }
    
    private void setTitle2(String title2){
    	this.title2 = title2;
    }
    
    @JsonbTransient
    @JsonbTypeDeserializer(DeserializerClass.class)
    @JsonbTypeSerializer(SerializerClass.class)
    public String getGivenDate() {
		return givenDate;
	}

    @JsonbTypeSerializer(SerializerClass.class)
	public void setGivenDate(String givenDate) {
		this.givenDate = givenDate;
	}

}