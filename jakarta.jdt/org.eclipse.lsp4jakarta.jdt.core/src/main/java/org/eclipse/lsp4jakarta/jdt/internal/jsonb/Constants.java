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

    //JSONB Closable constants
    public static final List<String> THREAD_METHODS = List.of(
                                                              "submit", "execute", "schedule", "scheduleAtFixedRate",
                                                              "scheduleWithFixedDelay", "runAsync", "supplyAsync",
                                                              "parallelStream", "newThread", "start",
                                                              "runLater", "invokeLater", "subscribeOn",
                                                              "publishOn", "observeOn", "scheduleJob", "scheduleTask");
    public static final List<String> THREAD_CLASSES = List.of(
                                                              "java.util.concurrent.ExecutorService",
                                                              "java.util.concurrent.ThreadPoolExecutor",
                                                              "java.util.concurrent.ScheduledExecutorService",
                                                              "java.util.concurrent.ForkJoinPool",
                                                              "java.util.concurrent.CompletableFuture",
                                                              "java.util.Timer",
                                                              "java.lang.Thread",
                                                              "java.util.concurrent.Executors",
                                                              "javax.swing.SwingWorker",
                                                              "javafx.application.Platform",
                                                              "reactor.core.publisher.Flux",
                                                              "reactor.core.publisher.Mono",
                                                              "io.reactivex.Observable",
                                                              "io.reactivex.Flowable",
                                                              "org.quartz.Scheduler",
                                                              "org.springframework.scheduling.TaskScheduler");

    public static final String CLOSE_METHOD = "close";
    public static final String JAKARTA_JSONB = "jakarta.json.bind.Jsonb";
    public static final String CLOSABLE_CLOSE = "java.io.Closeable";
    public static final String AUTOCLOSABLE_CLOSE = "java.lang.AutoCloseable";
}
