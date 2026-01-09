/*******************************************************************************
* Copyright (c) 2020, 2023 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/

package org.eclipse.lsp4jakarta.snippets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4jakarta.commons.JavaCursorContextKind;
import org.eclipse.lsp4jakarta.commons.JavaCursorContextResult;
import org.eclipse.lsp4jakarta.commons.ProjectLabelInfoEntry;
import org.eclipse.lsp4jakarta.ls.commons.snippets.ISnippetContext;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * A snippet context for Java files which matches java scope and dependency.
 *
 * @author Ankush Sharma, Credit to Angelo ZERR
 *
 */
public class SnippetContextForJava implements ISnippetContext<JavaSnippetCompletionContext> {

    public static final TypeAdapter<SnippetContextForJava> TYPE_ADAPTER = new SnippetContextForJavaAdapter();
    private static final Gson GSON = new Gson();
    private List<String> types;
    private SnippetContentType contentType;

    public SnippetContextForJava(List<String> types, SnippetContentType contentType) {
        this.types = types;
        this.contentType = contentType;
    }

    public SnippetContextForJava(List<String> types) {
        this(types, null);
    }

    public List<String> getTypes() {
        return types;
    }

    @Override
    public boolean isMatch(JavaSnippetCompletionContext context) {
        if (context == null) {
            return true;
        }

        // Check types
        boolean typeMatches = false;
        if (context.getProjectLabelInfoEntry() != null) {
            ProjectLabelInfoEntry label = context.getProjectLabelInfoEntry();
            if (types == null || types.isEmpty()) {
                return typeMatches = true;
            } else {
                for (String type : types) {
                    if (label.hasLabel(type)) {
                        typeMatches = true;
                        break;
                    }
                }
            }
        } else {
            typeMatches = true;
        }

        return typeMatches && snippetContentAppliesToContext(context.getJavaCursorContextResult());
    }

    public boolean snippetContentAppliesToContext(JavaCursorContextResult context) {
        // content/context being null signals that the client doesn't support getting
        // the completion context
        if (contentType == null || context == null) {
            return true;
        }
        JavaCursorContextKind kind = context.getKind();
        String prefix = context.getPrefix();
        boolean prefixMatchesAnnotation = prefix.startsWith("@");
        switch (contentType) {
            case METHOD_ANNOTATION:
                return prefixMatchesAnnotation && (kind == JavaCursorContextKind.BEFORE_METHOD
                                                   || kind == JavaCursorContextKind.IN_METHOD_ANNOTATIONS);
            case CLASS:
                return kind == JavaCursorContextKind.IN_EMPTY_FILE;
            case BEFORE_CLASS:
                return kind == JavaCursorContextKind.BEFORE_CLASS;
            case METHOD:
                return kind == JavaCursorContextKind.BEFORE_FIELD || kind == JavaCursorContextKind.BEFORE_METHOD
                       || kind == JavaCursorContextKind.IN_CLASS;
            case FIELD:
                return kind == JavaCursorContextKind.BEFORE_FIELD || kind == JavaCursorContextKind.BEFORE_METHOD
                       || kind == JavaCursorContextKind.IN_CLASS;
            default:
                return false;
        }
    }

    private static class SnippetContextForJavaAdapter extends TypeAdapter<SnippetContextForJava> {

        @Override
        public SnippetContextForJava read(final JsonReader in) throws IOException {
            JsonToken nextToken = in.peek();
            if (nextToken == JsonToken.NULL) {
                return null;
            }

            List<String> types = new ArrayList<>();
            SnippetContentType contentType = null;
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                switch (name) {
                    case "type":
                        if (in.peek() == JsonToken.BEGIN_ARRAY) {
                            in.beginArray();
                            while (in.peek() != JsonToken.END_ARRAY) {
                                types.add(in.nextString());
                            }
                            in.endArray();
                        } else {
                            types.add(in.nextString());
                        }
                        break;
                    case "contentType":
                        String contentTypeString = in.nextString();
                        contentType = GSON.fromJson(contentTypeString, SnippetContentType.class);
                        break;
                    default:
                        in.skipValue();
                }
            }
            in.endObject();
            return new SnippetContextForJava(types, contentType);
        }

        @Override
        public void write(JsonWriter out, SnippetContextForJava value) throws IOException {
            // Do nothing
        }
    }

}