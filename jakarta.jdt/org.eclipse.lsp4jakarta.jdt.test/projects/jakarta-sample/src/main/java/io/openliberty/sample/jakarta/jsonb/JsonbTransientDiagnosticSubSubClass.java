package io.openliberty.sample.jakarta.jsonb;

import jakarta.json.bind.annotation.JsonbProperty;

public class JsonbTransientDiagnosticSubSubClass extends JsonbTransientDiagnosticSubClass{
	

    @JsonbProperty("name")
    private String subFirstName;    // Diagnostic: @JsonbProperty property uniqueness in subclass, multiple properties cannot have same property names.
    
    @JsonbProperty("fav_editor")
    private String subfavoriteEditor;    // Diagnostic: @JsonbProperty property uniqueness in subclass, multiple properties cannot have same property names.
    
    @JsonbProperty("fav_lang1")
    private String subfavoriteEditor2;    // Diagnostic: @JsonbProperty property uniqueness in subclass, multiple properties cannot have same property names.

}
