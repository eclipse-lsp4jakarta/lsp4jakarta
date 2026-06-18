/*******************************************************************************
 * Copyright (c) 2020, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *
 *******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.internal.jsonb;

import java.util.List;

/**
 * JSON Binding (JSON-B) constants.
 */
public class Constants {

    /* Source */
    public static final String DIAGNOSTIC_SOURCE = "jakarta-jsonb";

    /* Annotation Constants */
    public static final String JSONB_PACKAGE = "jakarta.json.bind.annotation.";
    public static final String JSONB_PREFIX = "Jsonb";

    public static final String JSONB_CREATOR = JSONB_PACKAGE + JSONB_PREFIX + "Creator";
    public static final int MAX_METHOD_WITH_JSONBCREATOR = 1;
    public static final int MAX_DUPLICATE_PROPERTY_COUNT = 1;
    public static final String JSONB_PROPERTYNAME_UNICODE = "\\\\u([0-9A-Fa-f]{4})";

    public static final String JSONB_TRANSIENT = JSONB_PREFIX + "Transient";
    public static final String JSONB_TRANSIENT_FQ_NAME = JSONB_PACKAGE + JSONB_TRANSIENT;

    public static final String JSONB_ANNOTATION = JSONB_PACKAGE + JSONB_PREFIX + "Annotation";
    public static final String JSONB_DATE_FORMAT = JSONB_PACKAGE + JSONB_PREFIX + "DateFormat";
    public static final String JSONB_NILLABLE = JSONB_PACKAGE + JSONB_PREFIX + "Nillable";
    public static final String JSONB_NUMBER_FORMAT = JSONB_PACKAGE + JSONB_PREFIX + "NumberFormat";
    public static final String JSONB_PROPERTY = JSONB_PACKAGE + JSONB_PREFIX + "Property";
    public static final String JSONB_PROPERTY_ORDER = JSONB_PACKAGE + JSONB_PREFIX + "PropertyOrder";
    public static final String JSONB_TYPE_ADAPTER = JSONB_PACKAGE + JSONB_PREFIX + "TypeAdapter";
    public static final String JSONB_TYPE_DESERIALIZER = JSONB_PACKAGE + JSONB_PREFIX + "TypeDeserializer";
    public static final String JSONB_TYPE_SERIALIZER = JSONB_PACKAGE + JSONB_PREFIX + "TypeSerializer";
    public static final String JSONB_VISIBILITY = JSONB_PACKAGE + JSONB_PREFIX + "Visibility";

    public static final List<String> JSONB_ANNOTATIONS = List.of(JSONB_CREATOR, JSONB_TRANSIENT_FQ_NAME,
                                                                 JSONB_ANNOTATION,
                                                                 JSONB_DATE_FORMAT, JSONB_NILLABLE, JSONB_NUMBER_FORMAT, JSONB_PROPERTY, JSONB_PROPERTY_ORDER,
                                                                 JSONB_TYPE_ADAPTER, JSONB_TYPE_DESERIALIZER, JSONB_TYPE_SERIALIZER, JSONB_VISIBILITY);

    // Individual thread-related type constants
    public static final String JAVA_LANG_THREAD = "java.lang.Thread";
    public static final String JAVA_LANG_RUNNABLE = "java.lang.Runnable";
    public static final String JAVA_UTIL_CONCURRENT_CALLABLE = "java.util.concurrent.Callable";
    public static final String JAVA_UTIL_TIMER = "java.util.Timer";
    public static final String JAVA_UTIL_TIMER_TASK = "java.util.TimerTask";
    public static final String JAVA_UTIL_CONCURRENT_EXECUTOR = "java.util.concurrent.Executor";
    public static final String JAVA_UTIL_CONCURRENT_EXECUTOR_SERVICE = "java.util.concurrent.ExecutorService";
    public static final String JAVA_UTIL_CONCURRENT_THREAD_POOL_EXECUTOR = "java.util.concurrent.ThreadPoolExecutor";
    public static final String JAVA_UTIL_CONCURRENT_SCHEDULED_EXECUTOR_SERVICE = "java.util.concurrent.ScheduledExecutorService";
    public static final String JAVA_UTIL_CONCURRENT_FORK_JOIN_POOL = "java.util.concurrent.ForkJoinPool";
    public static final String JAVA_UTIL_CONCURRENT_EXECUTORS = "java.util.concurrent.Executors";
    public static final String JAVA_UTIL_CONCURRENT_COMPLETABLE_FUTURE = "java.util.concurrent.CompletableFuture";
    public static final String JAVA_UTIL_STREAM_STREAM = "java.util.stream.Stream";

    //Thread methods
    public static final List<String> THREAD_METHODS = List.of(
                                                              "submit", "execute", "schedule", "scheduleAtFixedRate",
                                                              "scheduleWithFixedDelay", "runAsync", "supplyAsync",
                                                              "parallelStream", "newThread", "start",
                                                              "invokeAll", "invokeAny", "map", "forEach");

    // Base thread-related types for hierarchy checking (interfaces and base classes that can be extended/implemented)
    // Excludes concrete utility classes like ThreadPoolExecutor, ForkJoinPool, Executors, Timer
    public static final List<String> THREAD_HIERARCHY_TYPES = List.of(
                                                                      JAVA_LANG_RUNNABLE,
                                                                      JAVA_UTIL_CONCURRENT_CALLABLE,
                                                                      JAVA_UTIL_CONCURRENT_EXECUTOR,
                                                                      JAVA_UTIL_CONCURRENT_EXECUTOR_SERVICE,
                                                                      JAVA_UTIL_CONCURRENT_SCHEDULED_EXECUTOR_SERVICE,
                                                                      JAVA_LANG_THREAD,
                                                                      JAVA_UTIL_TIMER_TASK);

    // Core threading classes
    public static final List<String> CORE_THREAD_CLASSES = List.of(
                                                                   JAVA_LANG_THREAD,
                                                                   JAVA_LANG_RUNNABLE,
                                                                   JAVA_UTIL_CONCURRENT_CALLABLE,
                                                                   JAVA_UTIL_TIMER,
                                                                   JAVA_UTIL_TIMER_TASK);

    // Executor framework classes (java.util.concurrent)
    public static final List<String> EXECUTOR_CLASSES = List.of(
                                                                JAVA_UTIL_CONCURRENT_EXECUTOR,
                                                                JAVA_UTIL_CONCURRENT_EXECUTOR_SERVICE,
                                                                JAVA_UTIL_CONCURRENT_THREAD_POOL_EXECUTOR,
                                                                JAVA_UTIL_CONCURRENT_SCHEDULED_EXECUTOR_SERVICE,
                                                                JAVA_UTIL_CONCURRENT_FORK_JOIN_POOL,
                                                                JAVA_UTIL_CONCURRENT_EXECUTORS);

    // Asynchronous computation classes
    public static final List<String> ASYNC_CLASSES = List.of(JAVA_UTIL_CONCURRENT_COMPLETABLE_FUTURE);

    // Stream API classes
    public static final List<String> STREAM_CLASSES = List.of(JAVA_UTIL_STREAM_STREAM);

    // Combined list of all thread classes
    public static final List<String> THREAD_CLASSES = List.of(
                                                              CORE_THREAD_CLASSES,
                                                              EXECUTOR_CLASSES,
                                                              ASYNC_CLASSES,
                                                              STREAM_CLASSES).stream().flatMap(List::stream).toList();

    // Closeable-related constants
    public static final String JAKARTA_JSONB_BIND = "jakarta.json.bind.";
    public static final String JAKARTA_JSONB = JAKARTA_JSONB_BIND + JSONB_PREFIX;
    public static final String JSONB_BUILDER_SHORT = "JsonbBuilder";
    public static final String JAKARTA_JSONB_BUILDER = JAKARTA_JSONB_BIND + JSONB_BUILDER_SHORT;
    public static final String JSONB_CREATE_METHOD = "create";
    public static final String JSONB_BUILD_METHOD = "build";
    public static final String CLOSE_METHOD = "close";
    public static final String CLOSABLE_CLOSE = "java.io.Closeable";
    public static final String AUTOCLOSABLE_CLOSE = "java.lang.AutoCloseable";
}
