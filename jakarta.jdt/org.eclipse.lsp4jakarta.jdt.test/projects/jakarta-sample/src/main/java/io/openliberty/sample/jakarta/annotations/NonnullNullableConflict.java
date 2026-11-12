package io.openliberty.sample.jakarta.annotations;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class NonnullNullableConflict {

    // Conflicting: field has both annotations
    @Nonnull
    @Nullable
    private String conflictingField;

    // Valid: only @Nonnull
    @Nonnull
    private String nonnullField;

    // Valid: only @Nullable
    @Nullable
    private String nullableField;

    // Conflicting: method has both annotations
    @Nonnull
    @Nullable
    public String conflictingMethod() {
        return null;
    }

    // Valid: only @Nonnull
    @Nonnull
    public String nonnullMethod() {
        return "";
    }

    // Conflicting: p1 has both annotations; p2 and p3 are valid
    public void conflictingParam(@Nonnull @Nullable String p1, @Nonnull String p2, @Nullable String p3) {
    }
}
