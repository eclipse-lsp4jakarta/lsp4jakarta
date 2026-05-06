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

import jakarta.json.bind.annotation.JsonbProperty;

public class JsonbStaticNestedClass {

    private String firstName;

    private SubChild subChild;

    public SubChild getSubChild() {
        return subChild;
    }

    public void setSubChild(SubChild subChild) {
        this.subChild = subChild;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @JsonbProperty("fav_lang")
    private String favoriteEditor;

    public String getFavoriteEditor() {
        return favoriteEditor;
    }

    public void setFavoriteEditor(String favoriteEditor) {
        this.favoriteEditor = favoriteEditor;
    }

    // Invalid: private static nested class
    private static class SubChild {

        private int token;

        public int getToken() {
            return token;
        }

        public void setToken(int token) {
            this.token = token;
        }

        private String title;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    // Valid: protected static nested class (spec allows public or protected)
    protected static class ProtectedChild {

        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    // Invalid: package-private (default) static nested class
    static class PackagePrivateChild {

        private String description;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    // Valid: public static nested class
    public static class PublicChild {

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}