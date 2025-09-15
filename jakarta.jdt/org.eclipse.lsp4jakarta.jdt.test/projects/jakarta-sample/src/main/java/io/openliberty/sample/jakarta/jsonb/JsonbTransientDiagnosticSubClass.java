package io.openliberty.sample.jakarta.jsonb;

import jakarta.json.bind.annotation.JsonbProperty;

public class JsonbTransientDiagnosticSubClass extends JsonbTransientDiagnostic{
	

    @JsonbProperty("hello")
    private String subFirstName;
    
    @JsonbProperty("fav_lang")
    private String subfavoriteEditor;    // Diagnostic: @JsonbProperty property uniqueness in subclass, multiple properties cannot have same property names.
    
    @JsonbProperty("fav_lang1")
    private String subfavoriteEditor1;

}
