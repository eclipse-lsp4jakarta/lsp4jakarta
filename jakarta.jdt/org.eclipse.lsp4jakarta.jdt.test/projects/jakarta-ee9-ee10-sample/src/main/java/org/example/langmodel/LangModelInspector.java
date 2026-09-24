package org.example.langmodel;

import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.AnnotationMember;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.types.Type;

/**
 * Sample using Jakarta Lang Model 4.0 (EE 10).
 * Introduced in EE 10 — EE 9=n/a, EE 10=4.0.1, EE 11=4.1.0 — version-distinct per tier.
 *
 * The Lang Model API is used by CDI portable extensions and build-time
 * extension processors to inspect class declarations at build time.
 */
public class LangModelInspector {

    /**
     * Checks whether a class has a specific annotation by name.
     */
    @SuppressWarnings("rawtypes")
    public boolean hasAnnotation(ClassInfo classInfo, String annotationName) {
        return classInfo.annotations().stream()
                .map(AnnotationInfo::declaration)
                .anyMatch(decl -> decl.name().equals(annotationName));
    }

    /**
     * Retrieves the string value of a named annotation member.
     */
    public String getAnnotationMemberValue(AnnotationInfo annotation, String memberName) {
        AnnotationMember member = annotation.member(memberName);
        if (member != null && member.isString()) {
            return member.asString();
        }
        return null;
    }

    /**
     * Returns the fully qualified name of the given type.
     */
    public String getTypeName(Type type) {
        return type.toString();
    }
}
