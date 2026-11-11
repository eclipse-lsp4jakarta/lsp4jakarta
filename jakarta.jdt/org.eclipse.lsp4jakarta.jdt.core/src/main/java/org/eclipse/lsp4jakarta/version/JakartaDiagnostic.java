package org.eclipse.lsp4jakarta.version;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum JakartaDiagnostic {

    // --- EE 9 Diagnostics (grouped by diagnostics participant) ---
    // --- DiagnosticUtils ---
    NAME_MUST_START_WITH_SET("NameMustStartWithSet", JakartaVersion.EE_9),
    FIELD_MUST_EXIST_IN_SETTER("FieldMustExistInSetter", JakartaVersion.EE_9),
    RETURN_TYPE_MUST_BE_VOID("ReturnTypeMustBeVoid", JakartaVersion.EE_9),
    MUST_DECLARE_EXACTLY_ONE_PARAM("MustDeclareExactlyOneParam", JakartaVersion.EE_9),
    METHOD_MUST_BE_PUBLIC("MethodMustBePublic", JakartaVersion.EE_9),

    // --- AnnotationDiagnosticsParticipant ---
    INVALID_DATE_FORMAT("InvalidDateFormat", JakartaVersion.EE_9),
    MISSING_RESOURCE_ANNOTATION("MissingResourceAnnotation", JakartaVersion.EE_9),
    MISSING_RESOURCE_NAME_ATTRIBUTE("MissingResourceNameAttribute", JakartaVersion.EE_9),
    MISSING_RESOURCE_TYPE_ATTRIBUTE("MissingResourceTypeAttribute", JakartaVersion.EE_9),
    POST_CONSTRUCT_EXCEPTION("PostConstructException", JakartaVersion.EE_9),
    POST_CONSTRUCT_PARAMS("PostConstructParams", JakartaVersion.EE_9),
    POST_CONSTRUCT_RETURN_TYPE("PostConstructReturnType", JakartaVersion.EE_9),
    PRE_DESTROY_EXCEPTION("PreDestroyException", JakartaVersion.EE_9),
    PRE_DESTROY_PARAMS("PreDestroyParams", JakartaVersion.EE_9),
    PRE_DESTROY_STATIC("PreDestroyStatic", JakartaVersion.EE_9),
    PRIORITY_SHOULD_BE_NON_NEGATIVE("PriorityShouldBeNonNegative", JakartaVersion.EE_9),
    RESOURCE_TYPE_MISMATCH("ResourceTypeMismatch", JakartaVersion.EE_9),
    GENERATED_VALUE_EMPTY("GeneratedValueEmpty", JakartaVersion.EE_9),
    GENERATED_VALUE_INVALID_FORMAT("GeneratedValueInvalidFormat", JakartaVersion.EE_9),
    RESOURCE_NAME_MUST_START_WITH_SET("ResourceNameMustStartWithSet", JakartaVersion.EE_9),
    RESOURCE_RETURN_TYPE_MUST_BE_VOID("ResourceReturnTypeMustBeVoid", JakartaVersion.EE_9),
    RESOURCE_MUST_DECLARE_EXACTLY_ONE_PARAM("ResourceMustDeclareExactlyOneParam", JakartaVersion.EE_9),

    // --- BeanValidationDiagnosticsParticipant ---
    INVALID_ANNOTATION_ON_NON_BOOLEAN_METHOD_OR_FIELD("InvalidAnnotationOnNonBooleanMethodOrField", JakartaVersion.EE_9),
    INVALID_ANNOTATION_ON_NON_BIGDECIMAL_CHAR_BYTE_SHORT_INT_LONG_METHOD_OR_FIELD("InvalidAnnotationOnNonBigDecimalCharByteShortIntLongMethodOrField", JakartaVersion.EE_9),
    INVALID_ANNOTATION_ON_NON_DATETIME_METHOD_OR_FIELD("InvalidAnnotationOnNonDateTimeMethodOrField", JakartaVersion.EE_9),
    INVALID_ANNOTATION_ON_NON_MIN_MAX_METHOD_OR_FIELD("InvalidAnnotationOnNonMinMaxMethodOrField", JakartaVersion.EE_9),
    INVALID_ANNOTATION_ON_NON_POSITIVE_METHOD_OR_FIELD("InvalidAnnotationOnNonPositiveMethodOrField", JakartaVersion.EE_9),
    INVALID_ANNOTATION_ON_NON_SIZE_METHOD_OR_FIELD("InvalidAnnotationOnNonSizeMethodOrField", JakartaVersion.EE_9),
    INVALID_VALID_ANNOTATION_ON_NON_CASCADABLE_TYPE("InvalidValidAnnotationOnNonCascadableType", JakartaVersion.EE_9),
    INVALID_CONSTRAIN_ANNOTATION_ON_STATIC_METHOD_OR_FIELD("InvalidConstrainAnnotationOnStaticMethodOrField", JakartaVersion.EE_9),
    INVALID_ANNOTATION_ON_NON_STRING_METHOD_OR_FIELD("InvalidAnnotationOnNonStringMethodOrField", JakartaVersion.EE_9),
    CONFLICTING_CONSTRAINT_ANNOTATIONS("ConflictingConstraintAnnotations", JakartaVersion.EE_9),

    // --- CdiDecoratorDiagnosticsParticipant ---
    INVALID_DECORATOR_DELEGATE_INJECTION_POINTS("InvalidDecoratorDelegateInjectionPoints", JakartaVersion.EE_9),
    INVALID_DELEGATE_INJECTION_POINT("InvalidDelegateInjectionPoint", JakartaVersion.EE_9),
    INVALID_DECORATOR_DELEGATE_TYPE_ASSIGNABILITY("InvalidDecoratorDelegateTypeAssignability", JakartaVersion.EE_9),
    INVALID_DECORATOR_WITH_NO_DECORATED_TYPES("InvalidDecoratorWithNoDecoratedTypes", JakartaVersion.EE_9),

    // --- CdiObserverMethodDiagnosticsParticipant ---
    INVALID_OBSERVER_METHOD_WITHOUT_NOTIFY("InvalidObserverMethodWithoutNotify", JakartaVersion.EE_9),

    // --- CdiRawEventTypeDiagnosticsParticipant ---
    INVALID_RAW_EVENT_TYPE_INJECTION_POINT("InvalidRawEventTypeInjectionPoint", JakartaVersion.EE_9),

    // --- CdiSpecializesDiagnosticsParticipant ---
    INVALID_SPECIALIZED_BEAN_WITH_NAMED_ANNOTATION("InvalidSpecializedBeanWithNamedAnnotation", JakartaVersion.EE_9),
    INVALID_SPECIALIZES_ANNOTATION_ON_NON_BEAN_SUPERCLASS("InvalidSpecializesAnnotationOnNonBeanSuperclass", JakartaVersion.EE_9),

    // --- CdiWildcardDiagnosticsParticipant ---
    INVALID_WILDCARD_TYPE_IN_INJECT_FIELD("InvalidWildcardTypeInInjectField", JakartaVersion.EE_9),
    INVALID_WILDCARD_TYPE_IN_PRODUCER_FIELD("InvalidWildcardTypeInProducerField", JakartaVersion.EE_9),
    INVALID_PRODUCER_FIELD_WITH_BARE_TYPE_VARIABLE_TYPE("InvalidProducerFieldWithBareTypeVariableType", JakartaVersion.EE_9),
    INVALID_PRODUCER_FIELD_WITH_TYPE_VARIABLE_AND_NON_DEPENDENT_SCOPE("InvalidProducerFieldWithTypeVariableAndNonDependentScope", JakartaVersion.EE_9),
    INVALID_WILDCARD_TYPE_IN_INJECT_METHOD("InvalidWildcardTypeInInjectMethod", JakartaVersion.EE_9),
    INVALID_WILDCARD_TYPE_IN_PRODUCER_METHOD("InvalidWildcardTypeInProducerMethod", JakartaVersion.EE_9),
    INVALID_PRODUCER_METHOD_WITH_BARE_TYPE_VARIABLE_RETURN_TYPE("InvalidProducerMethodWithBareTypeVariableReturnType", JakartaVersion.EE_9),
    INVALID_PRODUCER_METHOD_WITH_TYPE_VARIABLE_AND_NON_DEPENDENT_SCOPE("InvalidProducerMethodWithTypeVariableAndNonDependentScope", JakartaVersion.EE_9),

    // --- ManagedBeanDiagnosticsParticipant ---
    INVALID_MANAGED_BEAN_WITH_NON_STATIC_PUBLIC_FIELD("InvalidManagedBeanWithNonStaticPublicField", JakartaVersion.EE_9),
    INVALID_NUMBER_OF_SCOPE_ANNOTATIONS_BY_PRODUCER_FIELD("InvalidNumberOfScopeAnnotationsByProducerField", JakartaVersion.EE_9),
    INVALID_FIELD_WITH_PRODUCES_AND_INJECT_ANNOTATIONS("InvalidFieldWithProducesAndInjectAnnotations", JakartaVersion.EE_9),
    INVALID_PRODUCER_FIELD_WITH_NAMED_ANNOTATION("InvalidProducerFieldWithNamedAnnotation", JakartaVersion.EE_9),
    INVALID_NUMBER_OF_SCOPE_ANNOTATIONS_BY_PRODUCER_METHOD("InvalidNumberOfScopeAnnotationsByProducerMethod", JakartaVersion.EE_9),
    INVALID_METHOD_WITH_PRODUCES_AND_INJECT_ANNOTATIONS("InvalidMethodWithProducesAndInjectAnnotations", JakartaVersion.EE_9),
    INVALID_INTERCEPTOR_OR_DECORATOR_WITH_OBSERVER_METHOD("InvalidInterceptorOrDecoratorWithObserverMethod", JakartaVersion.EE_9),
    INVALID_OBSERVES_OBSERVES_ASYNC_METHOD_PARAMS("InvalidObservesObservesAsyncMethodParams", JakartaVersion.EE_9),
    INVALID_MULTIPLE_OBSERVER_PARAMS("InvalidMultipleObserverParams", JakartaVersion.EE_9),
    INVALID_DEPENDENT_SCOPE_WITH_CONDITIONAL_OBSERVER("InvalidDependentScopeWithConditionalObserver", JakartaVersion.EE_9),
    INVALID_INTERCEPTOR_OR_DECORATOR_WITH_DISPOSER_METHOD("InvalidInterceptorOrDecoratorWithDisposerMethod", JakartaVersion.EE_9),
    INVALID_NAMED_ANNOTATION_ON_NON_FIELD_INJECTION_POINT("InvalidNamedAnnotationOnNonFieldInjectionPoint", JakartaVersion.EE_9),
    INVALID_MANAGED_BEAN_WITH_INVALID_CONSTRUCTOR("InvalidManagedBeanWithInvalidConstructor", JakartaVersion.EE_9),
    INVALID_STATELESS_SESSION_BEAN_SCOPE("InvalidStatelessSessionBeanScope", JakartaVersion.EE_9),
    INVALID_GENERIC_MANAGED_BEAN_CLASS_WITH_NO_DEPENDENT_SCOPE("InvalidGenericManagedBeanClassWithNoDependentScope", JakartaVersion.EE_9),
    INVALID_NUMBER_OF_SCOPED_ANNOTATIONS_BY_MANAGED_BEAN("InvalidNumberOfScopedAnnotationsByManagedBean", JakartaVersion.EE_9),
    INVALID_DISPOSES_ANNOTATION_ON_MULTIPLE_METHOD_PARAMS("InvalidDisposesAnnotationOnMultipleMethodParams", JakartaVersion.EE_9),
    INVALID_DISPOSER_METHOD_PARAM_ANNOTATION("InvalidDisposerMethodParamAnnotation", JakartaVersion.EE_9),
    INVALID_INTERCEPTOR_OR_DECORATOR("InvalidInterceptorOrDecorator", JakartaVersion.EE_9),
    INVALID_SINGLETON_SESSION_BEAN_SCOPE("InvalidSingletonSessionBeanScope", JakartaVersion.EE_9),
    INVALID_PRODUCER_METHOD_PARAM_ANNOTATION("InvalidProducerMethodParamAnnotation", JakartaVersion.EE_9),
    INVALID_INJECT_ANNOTATION_ON_MULTIPLE_METHOD_PARAMS("InvalidInjectAnnotationOnMultipleMethodParams", JakartaVersion.EE_9),
    INVALID_INJECT_ANNOTATED_METHOD_PARAM_ANNOTATION("InvalidInjectAnnotatedMethodParamAnnotation", JakartaVersion.EE_9),
    INVALID_ORPHAN_DISPOSER_METHOD("InvalidOrphanDisposerMethod", JakartaVersion.EE_9),

    // --- DependencyInjectionDiagnosticsParticipant ---
    INVALID_SCOPE_ATTRIBUTES("InvalidScopeAttributes", JakartaVersion.EE_9),
    INVALID_INJECT_QUALIFIER_ON_FIELD_OR_PARAMETER("InvalidInjectQualifierOnFieldOrParameter", JakartaVersion.EE_9),
    INVALID_INJECT_ANNOTATION_ON_FINAL_FIELD("InvalidInjectAnnotationOnFinalField", JakartaVersion.EE_9),
    INVALID_INJECT_ANNOTATION_ON_NON_STATIC_INNER_CLASS("InvalidInjectAnnotationOnNonStaticInnerClass", JakartaVersion.EE_9),
    INVALID_INJECT_ANNOTATION_ON_FINAL_METHOD("InvalidInjectAnnotationOnFinalMethod", JakartaVersion.EE_9),
    INVALID_INJECT_ANNOTATION_ON_ABSTRACT_METHOD("InvalidInjectAnnotationOnAbstractMethod", JakartaVersion.EE_9),
    INVALID_INJECT_ANNOTATION_ON_STATIC_METHOD("InvalidInjectAnnotationOnStaticMethod", JakartaVersion.EE_9),
    INVALID_INJECT_ANNOTATION_ON_GENERIC_METHOD("InvalidInjectAnnotationOnGenericMethod", JakartaVersion.EE_9),
    INVALID_INJECT_ANNOTATION_ON_MULTIPLE_CONSTRUCTORS("InvalidInjectAnnotationOnMultipleConstructors", JakartaVersion.EE_9),
    INJECTION_POINT_INVALID_PRIMITIVE_BEAN("InjectionPointInvalidPrimitiveBean", JakartaVersion.EE_9),
    INJECTION_POINT_INVALID_INNER_CLASS_BEAN("InjectionPointInvalidInnerClassBean", JakartaVersion.EE_9),
    INJECTION_POINT_INVALID_ABSTRACT_CLASS_BEAN("InjectionPointInvalidAbstractClassBean", JakartaVersion.EE_9),
    INJECTION_POINT_INVALID_EXTENSION_PROVIDER_BEAN("InjectionPointInvalidExtensionProviderBean", JakartaVersion.EE_9),
    INJECTION_POINT_INVALID_VETOED_CLASS_BEAN("InjectionPointInvalidVetoedClassBean", JakartaVersion.EE_9),
    INJECTION_POINT_INVALID_CONSTRUCTOR_BEAN("InjectionPointInvalidConstructorBean", JakartaVersion.EE_9),

    // --- EjbDiagnosticsParticipant ---
    INVALID_MODIFIER_NOT_PUBLIC("InvalidModifierNotPublic", JakartaVersion.EE_9),
    INVALID_MODIFIER_FINAL("InvalidModifierFinal", JakartaVersion.EE_9),
    INVALID_MODIFIER_ABSTRACT("InvalidModifierAbstract", JakartaVersion.EE_9),
    INVALID_NON_TOP_LEVEL_CLASS("InvalidNonTopLevelClass", JakartaVersion.EE_9),
    INVALID_SESSION_BEAN_WITH_INTERCEPTOR_OR_DECORATOR("InvalidSessionBeanWithInterceptorOrDecorator", JakartaVersion.EE_9),
    CONFLICTING_SESSION_BEAN_ANNOTATIONS("ConflictingSessionBeanAnnotations", JakartaVersion.EE_9),
    INVALID_SESSION_SYNC_METHOD_FINAL("InvalidSessionSyncMethodFinal", JakartaVersion.EE_9),
    INVALID_SESSION_SYNC_METHOD_STATIC("InvalidSessionSyncMethodStatic", JakartaVersion.EE_9),
    INVALID_SESSION_SYNC_METHOD_NON_VOID("InvalidSessionSyncMethodNonVoid", JakartaVersion.EE_9),
    MISSING_PUBLIC_NO_ARG_CONSTRUCTOR("MissingPublicNoArgConstructor", JakartaVersion.EE_9),
    SESSION_BEAN_FINALIZE_METHOD("SessionBeanFinalizeMethod", JakartaVersion.EE_9),

    // --- EjbMessageDrivenDiagnosticsParticipant ---
    IMPLEMENT_MESSAGE_LISTENER("ImplementMessageListener", JakartaVersion.EE_9),

    // --- FacesDiagnosticsParticipant ---
    FACES_VALIDATOR_ANNOTATED_CLASS_NO_VALIDATOR_INTERFACE_IMPL("FacesValidatorAnnotatedClassNoValidatorInterfaceImpl", JakartaVersion.EE_9),

    // --- InterceptorDiagnosticsParticipant ---
    INVALID_INTERCEPTOR_ANNOTATION_ON_ABSTRACT_CLASS("InvalidInterceptorAnnotationOnAbstractClass", JakartaVersion.EE_9),
    INVALID_INTERCEPTOR_NO_ARGS_CONSTRUCTOR_MISSING("InvalidInterceptorNoArgsConstructorMissing", JakartaVersion.EE_9),
    INVALID_INTERCEPTOR_METHODS_PROCEED_MISSING("InvalidInterceptorMethodsProceedMissing", JakartaVersion.EE_9),
    INVALID_INTERCEPTOR_METHOD_ANNOTATION_ON_FINAL_METHOD("InvalidInterceptorMethodAnnotationOnFinalMethod", JakartaVersion.EE_9),
    INVALID_INTERCEPTOR_METHOD_ANNOTATION_ON_ABSTRACT_METHOD("InvalidInterceptorMethodAnnotationOnAbstractMethod", JakartaVersion.EE_9),
    INVALID_INTERCEPTOR_METHOD_ANNOTATION_ON_STATIC_METHOD("InvalidInterceptorMethodAnnotationOnStaticMethod", JakartaVersion.EE_9),
    INVALID_MULTIPLE_INTERCEPTOR_METHODS_OF_SAME_TYPE("InvalidMultipleInterceptorMethodsOfSameType", JakartaVersion.EE_9),
    INVALID_INTERCEPTOR_NEGATIVE_PRIORITY("InvalidInterceptorNegativePriority", JakartaVersion.EE_9),
    INVALID_INTERCEPTOR_MISSING_INTERCEPTOR_BINDING("InvalidInterceptorMissingInterceptorBinding", JakartaVersion.EE_9),
    INVALID_FINAL_INTERCEPTOR_BINDING_CLASS("InvalidFinalInterceptorBindingClass", JakartaVersion.EE_9),
    INVALID_METHOD_ON_INTERCEPTOR_BINDING_CLASS("InvalidMethodOnInterceptorBindingClass", JakartaVersion.EE_9),

    // --- ClassConstructorDiagnosticsParticipant ---
    NO_PUBLIC_CONSTRUCTORS("NoPublicConstructors", JakartaVersion.EE_9),
    UNUSED_CONSTRUCTOR("UnusedConstructor", JakartaVersion.EE_9),
    AMBIGUOUS_CONSTRUCTORS("AmbiguousConstructors", JakartaVersion.EE_9),

    // --- JaxrsDiagnosticsParticipant ---
    INVALID_CONSTRAINT_TARGET("InvalidConstraintTarget", JakartaVersion.EE_9),

    // --- ResourceMethodDiagnosticsParticipant ---
    NON_PUBLIC_RESOURCE_METHOD("NonPublicResourceMethod", JakartaVersion.EE_9),
    RESOURCE_METHOD_MULTIPLE_ENTITY_PARAMS("ResourceMethodMultipleEntityParams", JakartaVersion.EE_9),

    // --- JsonbDiagnosticsParticipant ---
    INVALID_NUMER_OF_JSONB_CREATOR_ANNOTATIONS_IN_CLASS("InvalidNumerOfJsonbCreatorAnnotationsInClass", JakartaVersion.EE_9),
    INVALID_JSONB_FROM_JSON_NULL_PARAMETER("InvalidJsonbFromJsonNullParameter", JakartaVersion.EE_9),
    JSONB_CLOSEABLE_THREAD_SAFETY("JsonbCloseableThreadSafety", JakartaVersion.EE_9),
    INVALID_JSON_B_NO_ARGS_CONSTRUCTOR_MISSING("InvalidJsonBNoArgsConstructorMissing", JakartaVersion.EE_9),
    INVALID_JSON_B_NON_STATIC_INNER_CLASS("InvalidJsonBNonStaticInnerClass", JakartaVersion.EE_9),
    INVALID_JSON_B_NON_PUBLIC_PROTECTED_STATIC_NESTED_CLASS("InvalidJsonBNonPublicProtectedStaticNestedClass", JakartaVersion.EE_9),
    INVALID_PROPERTY_NAMES_ON_JSONB_FIELDS("InvalidPropertyNamesOnJsonbFields", JakartaVersion.EE_9),
    INVALID_J_SON_BINDIND_ANNOTATION_WITH_JSONB_TRANSIENT_ON_FIELD("InvalidJSonBindindAnnotationWithJsonbTransientOnField", JakartaVersion.EE_9),
    INVALID_J_SON_BINDIND_ANNOTATION_WITH_JSONB_TRANSIENT_ON_ACCESSOR("InvalidJSonBindindAnnotationWithJsonbTransientOnAccessor", JakartaVersion.EE_9),

    // --- JsonpDiagnosticParticipant ---
    INVALID_JSON_CREATE_POINTER_TARGET("InvalidJsonCreatePointerTarget", JakartaVersion.EE_9),
    INVALID_JSON_OBJECT_BUILDER_KEY("InvalidJsonObjectBuilderKey", JakartaVersion.EE_9),
    INVALID_JSON_ARRAY_BUILDER_VALUE("InvalidJsonArrayBuilderValue", JakartaVersion.EE_9),

    // --- PersistenceContextDiagnosticsParticipant ---
    PERSISTENCE_CONTEXT_NOT_IN_MANAGED_COMPONENT("PersistenceContextNotInManagedComponent", JakartaVersion.EE_9),
    EXTENDED_PERSISTENCE_CONTEXT_IN_NON_STATEFUL_BEAN("ExtendedPersistenceContextInNonStatefulBean", JakartaVersion.EE_9),

    // --- PersistenceEntityDiagnosticsParticipant ---
    NAMED_ENTITY_GRAPH_ON_NON_ENTITY_CLASS("NamedEntityGraphOnNonEntityClass", JakartaVersion.EE_9),
    NAMED_ENTITY_GRAPHS_ON_NON_ENTITY_CLASS("NamedEntityGraphsOnNonEntityClass", JakartaVersion.EE_9),
    NAMED_QUERY_ON_INVALID_CLASS("NamedQueryOnInvalidClass", JakartaVersion.EE_9),
    NAMED_QUERIES_ON_INVALID_CLASS("NamedQueriesOnInvalidClass", JakartaVersion.EE_9),
    NAMED_NATIVE_QUERY_ON_INVALID_CLASS("NamedNativeQueryOnInvalidClass", JakartaVersion.EE_9),
    NAMED_NATIVE_QUERIES_ON_INVALID_CLASS("NamedNativeQueriesOnInvalidClass", JakartaVersion.EE_9),
    INVALID_FINAL_METHOD_IN_ENTITY_ANNOTATED_CLASS("InvalidFinalMethodInEntityAnnotatedClass", JakartaVersion.EE_9),
    INVALID_PERSISTENT_FIELD_IN_ENTITY_ANNOTATED_CLASS("InvalidPersistentFieldInEntityAnnotatedClass", JakartaVersion.EE_9),
    INVALID_CONSTRUCTOR_IN_ENTITY_ANNOTATED_CLASS("InvalidConstructorInEntityAnnotatedClass", JakartaVersion.EE_9),
    INVALID_FINAL_MODIFIER_ON_ENTITY_ANNOTATED_CLASS("InvalidFinalModifierOnEntityAnnotatedClass", JakartaVersion.EE_9),
    MISSING_PRIMARY_KEY("MissingPrimaryKey", JakartaVersion.EE_9),
    MULTIPLE_EMBEDDED_ID_ANNOTATIONS("MultipleEmbeddedIdAnnotations", JakartaVersion.EE_9),
    MIXED_IDENTIFIER_ANNOTATIONS("MixedIdentifierAnnotations", JakartaVersion.EE_9),
    INHERITANCE_ANNOTATION_ON_NON_ROOT_ENTITY("InheritanceAnnotationOnNonRootEntity", JakartaVersion.EE_9),
    INHERITANCE_ANNOTATION_ON_NON_ENTITY_CLASS("InheritanceAnnotationOnNonEntityClass", JakartaVersion.EE_9),
    INVALID_VALUE_IN_TEMPORAL_ANNOTATION("InvalidValueInTemporalAnnotation", JakartaVersion.EE_9),
    MISSING_TEMPORAL_ANNOTATION("MissingTemporalAnnotation", JakartaVersion.EE_9),
    DUPLICATE_VERSION_ANNOTATION_IN_CLASS("DuplicateVersionAnnotationInClass", JakartaVersion.EE_9),
    DUPLICATE_VERSION_ANNOTATION_IN_HIERARCHY("DuplicateVersionAnnotationInHierarchy", JakartaVersion.EE_9),
    INVALID_ID_TYPE("InvalidIdType", JakartaVersion.EE_9),
    INVALID_VERSION_FIELD_OR_PROPERTY_TYPE("InvalidVersionFieldOrPropertyType", JakartaVersion.EE_9),
    TABLE_GENERATOR_INVALID_EMPTY_NAME("TableGeneratorInvalidEmptyName", JakartaVersion.EE_9),
    SEQUENCE_GENERATOR_INVALID_EMPTY_NAME("SequenceGeneratorInvalidEmptyName", JakartaVersion.EE_9),
    SECONDARY_TABLE_INVALID_EMPTY_NAME("SecondaryTableInvalidEmptyName", JakartaVersion.EE_9),
    TABLE_GENERATORS_MISSING_TABLE_GENERATOR_MAPPING("TableGeneratorsMissingTableGeneratorMapping", JakartaVersion.EE_9),
    SEQUENCE_GENERATORS_MISSING_SEQUENCE_GENERATOR_MAPPING("SequenceGeneratorsMissingSequenceGeneratorMapping", JakartaVersion.EE_9),
    SECONDARY_TABLES_MISSING_SECONDARY_TABLE_MAPPING("SecondaryTablesMissingSecondaryTableMapping", JakartaVersion.EE_9),
    EMBEDDED_TYPE_NOT_ANNOTATED_WITH_EMBEDDABLE("EmbeddedTypeNotAnnotatedWithEmbeddable", JakartaVersion.EE_9),

    // --- PersistenceMapKeyDiagnosticsParticipant ---
    INVALID_METHOD_WITH_MULTIPLE_MPJC_ANNOTATIONS("InvalidMethodWithMultipleMPJCAnnotations", JakartaVersion.EE_9),
    INVALID_FIELD_WITH_MULTIPLE_MPJC_ANNOTATIONS("InvalidFieldWithMultipleMPJCAnnotations", JakartaVersion.EE_9),
    INVALID_MAP_KEY_TEMPORAL_ON_NON_TEMPORAL_TYPE("InvalidMapKeyTemporalOnNonTemporalType", JakartaVersion.EE_9),
    INVALID_RETURN_TYPE_OF_METHOD("InvalidReturnTypeOfMethod", JakartaVersion.EE_9),
    INVALID_TYPE_OF_FIELD("InvalidTypeOfField", JakartaVersion.EE_9),
    INVALID_MAPKEY_ANNOTATIONS_ON_SAME_METHOD("InvalidMapKeyAnnotationsOnSameMethod", JakartaVersion.EE_9),
    INVALID_MAPKEY_ANNOTATIONS_ON_SAME_FIELD("InvalidMapKeyAnnotationsOnSameField", JakartaVersion.EE_9),
    INVALID_METHOD_ACCESS_SPECIFIER("InvalidMethodAccessSpecifier", JakartaVersion.EE_9),
    INVALID_METHOD_NAME("InvalidMethodName", JakartaVersion.EE_9),
    INVALID_MAPKEY_ANNOTATIONS_FIELD_NOT_FOUND("InvalidMapKeyAnnotationsFieldNotFound", JakartaVersion.EE_9),
    INVALID_MAP_KEY_ENUMERATED_NOT_ON_MAP_TYPE("InvalidMapKeyEnumeratedNotOnMapType", JakartaVersion.EE_9),
    INVALID_MAP_KEY_ENUMERATED_ON_NON_ENUM_TYPE("InvalidMapKeyEnumeratedOnNonEnumType", JakartaVersion.EE_9),

    // --- SecurityIdentityStoreDiagnosticsParticipant ---
    MISSING_APPLICATION_SCOPED_ON_IDENTITY_STORE_DEFINITION("MissingApplicationScopedOnIdentityStoreDefinition", JakartaVersion.EE_9),
    INVALID_SCOPE_ON_IDENTITY_STORE_DEFINITION("InvalidScopeOnIdentityStoreDefinition", JakartaVersion.EE_9),

    // --- FilterDiagnosticsParticipant ---
    CLASS_WEBFILTER_ANNOTATED_NO_FILTER_INTERFACE_IMPL("ClassWebFilterAnnotatedNoFilterInterfaceImpl", JakartaVersion.EE_9),
    WEBFILTER_ANNOTATION_MISSING_ATTRIBUTES("WebFilterAnnotationMissingAttributes", JakartaVersion.EE_9),
    WEBFILTER_ANNOTATION_ATTRIBUTE_CONFLICT("WebFilterAnnotationAttributeConflict", JakartaVersion.EE_9),

    // --- ListenerDiagnosticsParticipant ---
    WEBFILTER_ANNOTATED_CLASS_REQ_IFACE_NO_IMPL("WebFilterAnnotatedClassReqIfaceNoImpl", JakartaVersion.EE_9),

    // --- SecurityDiagnosticsParticipant ---
    DECLARE_ROLES_ON_NON_SERVLET_CLASS("DeclareRolesOnNonServletClass", JakartaVersion.EE_9),

    // --- ServletDiagnosticsParticipant ---
    WEBSERVLET_ANNOTATED_CLASS_DOES_NOT_EXTEND_HTTPSERVLET("WebServletAnnotatedClassDoesNotExtendHttpServlet", JakartaVersion.EE_9),
    WEBSERVLET_ANNOTATED_CLASS_UNKNOWN_SUPERTYPE_DOES_NOT_EXTEND_HTTPSERVLET("WebServletAnnotatedClassUnknownSuperTypeDoesNotExtendHttpServlet", JakartaVersion.EE_9),
    WEBSERVLET_ANNOTATION_MISSING_ATTRIBUTES("WebServletAnnotationMissingAttributes", JakartaVersion.EE_9),
    WEBSERVLET_ANNOTATION_ATTRIBUTE_CONFLICT("WebServletAnnotationAttributeConflict", JakartaVersion.EE_9),

    // --- VersionDiagnosticsParticipant ---
    MULTIPLE_JAKARTA_EE_VERSIONS("MultipleJakartaEEVersions", JakartaVersion.EE_9),

    // --- WebSocketDiagnosticsParticipant ---
    DUPLICATE_LIFE_CYCLE_ANNOTATION("DuplicateLifeCycleAnnotation", JakartaVersion.EE_9),
    INVALID_ON_OPEN_PARAMS("InvalidOnOpenParams", JakartaVersion.EE_9),
    INVALID_ON_CLOSE_PARAMS("InvalidOnCloseParams", JakartaVersion.EE_9),
    PATH_PARAMS_MISSING_FROM_PARAM("PathParamsMissingFromParam", JakartaVersion.EE_9),
    PATH_PARAM_DOES_NOT_MATCH_ENDPOINT_URI("PathParamDoesNotMatchEndpointURI", JakartaVersion.EE_9),
    ON_MESSAGE_DUPLICATE_METHOD("OnMessageDuplicateMethod", JakartaVersion.EE_9),
    INVALID_ENDPOINT_PATH_WITH_NO_STARTING_SLASH("InvalidEndpointPathWithNoStartingSlash", JakartaVersion.EE_9),
    INVALID_ENDPOINT_PATH_WITH_RELATIVE_PATHS("InvalidEndpointPathWithRelativePaths", JakartaVersion.EE_9),
    INVALID_ENDPOINT_PATH_NOT_TEMPLEATE_OR_PARTIAL_URI("InvalidEndpointPathNotTempleateOrPartialURI", JakartaVersion.EE_9),
    INVALID_ENDPOINT_PATH_DUPLICATE_VARIABLE("InvalidEndpointPathDuplicateVariable", JakartaVersion.EE_9),
    MISSING_PUBLIC_NO_ARG_CONSTRUCTOR_WEB_SOCKET_DIAGNOSTICS_PARTICIPANT("missingPublicNoArgConstructor", JakartaVersion.EE_9);

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