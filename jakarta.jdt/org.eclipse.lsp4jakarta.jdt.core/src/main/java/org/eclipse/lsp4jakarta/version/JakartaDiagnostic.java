package org.eclipse.lsp4jakarta.version;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum JakartaDiagnostic {

    // --- EE 9 Diagnostics ---
    // ---BeanValidationDiagnostics-----
    INVALID_CONSTRAIN_ANNOTATION_ON_STATIC_METHOD_OR_FIELD("InvalidConstrainAnnotationOnStaticMethodOrField", JakartaVersion.EE_9),
    INVALID_ANNOTATION_ON_NON_BOOLEAN_METHOD_OR_FIELD("InvalidAnnotationOnNonBooleanMethodOrField", JakartaVersion.EE_9),
    INVALID_ANNOTATION_ON_NON_BIGDECIMAL_CHAR_BYTE_SHORT_INT_LONG_METHOD_OR_FIELD("InvalidAnnotationOnNonBigDecimalCharByteShortIntLongMethodOrField", JakartaVersion.EE_9),
    INVALID_ANNOTATION_ON_NON_DATETIME_METHOD_OR_FIELD("InvalidAnnotationOnNonDateTimeMethodOrField", JakartaVersion.EE_9),
    INVALID_ANNOTATION_ON_NON_MIN_MAX_METHOD_OR_FIELD("InvalidAnnotationOnNonMinMaxMethodOrField", JakartaVersion.EE_9),
    INVALID_ANNOTATION_ON_NON_POSITIVE_METHOD_OR_FIELD("InvalidAnnotationOnNonPositiveMethodOrField", JakartaVersion.EE_9),
    INVALID_ANNOTATION_ON_NON_SIZE_METHOD_OR_FIELD("InvalidAnnotationOnNonSizeMethodOrField", JakartaVersion.EE_9),
    INVALID_ANNOTATION_ON_NON_STRING_METHOD_OR_FIELD("InvalidAnnotationOnNonStringMethodOrField", JakartaVersion.EE_9),

    //---ServletDiagnostics---
    CLASS_WEBFILTER_ANNOTATED_NO_FILTER_INTERFACE_IMPL("ClassWebFilterAnnotatedNoFilterInterfaceImpl", JakartaVersion.EE_9),
    WEBFILTER_ANNOTATION_MISSING_ATTRIBUTES("WebFilterAnnotationMissingAttributes", JakartaVersion.EE_9),
    WEBFILTER_ANNOTATION_ATTRIBUTE_CONFLICT("WebFilterAnnotationAttributeConflict", JakartaVersion.EE_9),
    WEBFILTER_ANNOTATED_CLASS_REQ_IFACE_NO_IMPL("WebFilterAnnotatedClassReqIfaceNoImpl", JakartaVersion.EE_9),
    WEBSERVLET_ANNOTATED_CLASS_DOES_NOT_EXTEND_HTTPSERVLET("WebServletAnnotatedClassDoesNotExtendHttpServlet", JakartaVersion.EE_9),
    WEBSERVLET_ANNOTATED_CLASS_UNKNOWN_SUPERTYPE_DOES_NOT_EXTEND_HTTPSERVLET("WebServletAnnotatedClassUnknownSuperTypeDoesNotExtendHttpServlet", JakartaVersion.EE_9),
    WEBSERVLET_ANNOTATION_MISSING_ATTRIBUTES("WebServletAnnotationMissingAttributes", JakartaVersion.EE_9),
    WEBSERVLET_ANNOTATION_ATTRIBUTE_CONFLICT("WebServletAnnotationAttributeConflict", JakartaVersion.EE_9),

    //---PersistenceMapKeyDiagnostics---
    INVALID_FINAL_METHOD_IN_ENTITY_ANNOTATED_CLASS("InvalidFinalMethodInEntityAnnotatedClass", JakartaVersion.EE_9),
    INVALID_PERSISTENT_FIELD_IN_ENTITY_ANNOTATED_CLASS("InvalidPersistentFieldInEntityAnnotatedClass", JakartaVersion.EE_9),
    INVALID_CONSTRUCTOR_IN_ENTITY_ANNOTATED_CLASS("InvalidConstructorInEntityAnnotatedClass", JakartaVersion.EE_9),
    INVALID_FINAL_MODIFIER_ON_ENTITY_ANNOTATED_CLASS("InvalidFinalModifierOnEntityAnnotatedClass", JakartaVersion.EE_9),
    INVALID_MAPKEY_ANNOTATIONS_ON_SAME_METHOD("InvalidMapKeyAnnotationsOnSameMethod", JakartaVersion.EE_9),
    INVALID_MAPKEY_ANNOTATIONS_ON_SAME_FIELD("InvalidMapKeyAnnotationsOnSameField", JakartaVersion.EE_9),
    INVALID_METHOD_WITH_MULTIPLE_MPJC_ANNOTATIONS("InvalidMethodWithMultipleMPJCAnnotations", JakartaVersion.EE_9),
    INVALID_FIELD_WITH_MULTIPLE_MPJC_ANNOTATIONS("InvalidFieldWithMultipleMPJCAnnotations", JakartaVersion.EE_9),
    INVALID_TYPE_OF_FIELD("InvalidTypeOfField", JakartaVersion.EE_9),
    INVALID_METHOD_NAME("InvalidMethodName", JakartaVersion.EE_9),
    INVALID_METHOD_ACCESS_SPECIFIER("InvalidMethodAccessSpecifier", JakartaVersion.EE_9),
    INVALID_RETURN_TYPE_OF_METHOD("InvalidReturnTypeOfMethod", JakartaVersion.EE_9),
    INVALID_MAPKEY_ANNOTATIONS_FIELD_NOT_FOUND("InvalidMapKeyAnnotationsFieldNotFound", JakartaVersion.EE_9);

    // --- EE 10 Diagnostics ---

    // --- EE 11 Diagnostics ---

    private final String code;
    private final JakartaVersion minVersion;
    private final JakartaVersion maxVersion;

    JakartaDiagnostic(String code, JakartaVersion minVersion) {
        this(code, minVersion, null);
    }

    JakartaDiagnostic(String code, JakartaVersion minVersion, JakartaVersion maxVersion) {
        this.code = code;
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
    }

    public String getCode() {
        return code;
    }

    public JakartaVersion getMinVersion() {
        return minVersion;
    }

    /**
     * Checks if this diagnostic is relevant for the given project version.
     * Usually, a rule introduced in EE9 is also valid in EE10 and EE11.
     */
    public boolean isApplicableTo(JakartaVersion projectVersion) {
        if (projectVersion == JakartaVersion.UNKNOWN)
            return false;

        int projectLevel = projectVersion.getLevel();
        int minLevel = this.minVersion.getLevel();

        // 1. Check if the project is too old for this rule
        if (projectLevel < minLevel) {
            return false;
        }

        // 2. Check if the project is too new for this rule (if a max limit exists)
        if (this.maxVersion != null) {
            int maxLevel = this.maxVersion.getLevel();
            if (projectLevel > maxLevel) {
                return false;
            }
        }

        return true;
    }

    /**
     * Helper to get all diagnostics for a specific version strictly.
     */
    public static List<JakartaDiagnostic> getRulesForExactly(JakartaVersion version) {
        return Arrays.stream(values()).filter(d -> d.minVersion == version).collect(Collectors.toList());
    }

    public static JakartaDiagnostic getByCodeOrNull(String code) {
        return Arrays.stream(values()).filter(d -> d.code.equals(code)).findFirst().orElse(null);
    }
}