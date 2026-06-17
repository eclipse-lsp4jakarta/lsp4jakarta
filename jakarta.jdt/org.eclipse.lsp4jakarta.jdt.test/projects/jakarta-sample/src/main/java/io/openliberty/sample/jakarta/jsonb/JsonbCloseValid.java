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

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import java.util.concurrent.TimeUnit;

/**
 * Test class demonstrating valid Jsonb.close() usage.
 * These cases should NOT trigger diagnostic warnings.
 */
public class JsonbCloseValid {

    /**
     * Valid: close() without any thread operations.
     */
    public void safeCloseNoThreads() throws Exception {
        Jsonb jsonb = JsonbBuilder.create();
        String json = jsonb.toJson(new Object());
        jsonb.close();
    }

    /**
     * Valid: close() after thread.join() synchronization.
     */
    public void safeCloseWithJoin() throws Exception {
        Jsonb jsonb = JsonbBuilder.create();

        Thread thread = new Thread(() -> {
            String json = jsonb.toJson(new Object());
        });
        thread.start();
        thread.join(); // Proper synchronization

        jsonb.close(); // Safe to close now
    }

    /**
     * Valid: close() after executor shutdown and awaitTermination.
     */
    public void safeCloseWithExecutorShutdown() throws Exception {
        Jsonb jsonb = JsonbBuilder.create();
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();

        executor.submit(() -> {
            String json = jsonb.toJson(new Object());
            return json;
        });

        executor.shutdown(); // Proper shutdown
        executor.awaitTermination(1, TimeUnit.SECONDS);

        jsonb.close(); // Safe to close now
    }

    /**
     * Valid: close() after joining multiple threads.
     */
    public void safeCloseWithMultipleThreads() throws Exception {
        Jsonb jsonb = JsonbBuilder.create();

        Thread t1 = new Thread(() -> jsonb.toJson("data1"));
        Thread t2 = new Thread(() -> jsonb.toJson("data2"));

        t1.start();
        t2.start();

        t1.join(); // Wait for both threads
        t2.join();

        jsonb.close(); // Safe to close now
    }

    /**
     * Valid: close() before any thread operations.
     */
    public void safeCloseBeforeThreads() throws Exception {
        Jsonb jsonb = JsonbBuilder.create();
        String json = jsonb.toJson(new Object());
        jsonb.close(); // Close before thread starts

        Thread thread = new Thread(() -> {
            // This thread doesn't use jsonb
            System.out.println("Thread running");
        });
        thread.start();
        thread.join();
    }

    /**
     * Valid: close() on a different object.
     */
    public void closeOnDifferentObject() throws Exception {
        Jsonb jsonb = JsonbBuilder.create();

        Thread thread = new Thread(() -> {
            String json = jsonb.toJson(new Object());
        });
        thread.start();

        // Closing a different AutoCloseable, not the jsonb instance
        java.io.StringReader reader = new java.io.StringReader("test");
        reader.close();

        thread.join();
        jsonb.close();
    }
}