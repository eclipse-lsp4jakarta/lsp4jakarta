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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Invalid usage example: Calling Jsonb.close() without ensuring all threads
 * have finished interaction with Jsonb.
 * 
 * This class demonstrates scenarios that should trigger the WARNING diagnostic
 * because close() is called while threads might still be using the Jsonb instance.
 */
public class JsonbCloseInvalid {

    /**
     * WARNING: Close called immediately after starting threads.
     * Threads are created but close() is called without waiting for them to finish.
     * This is the exact dangerous scenario from the spec.
     * @throws Exception 
     */
    public void closeWithoutJoiningThreads() throws Exception {
        Jsonb jsonb = JsonbBuilder.create();
        
        // Start multiple threads that use jsonb
        for (int i = 0; i < 5; i++) {
            final int threadNum = i;
            new Thread(() -> {
                String json = jsonb.toJson(new Person("Person" + threadNum, 20 + threadNum));
                System.out.println(json);
            }).start();
        }
        
        // WARNING: Calling close() while threads might still be running
        // No join() or synchronization before close()
        jsonb.close();
    }

    /**
     * WARNING: Using ExecutorService but calling close() before shutdown.
     * The threads in the executor might still be processing.
     * @throws Exception 
     */
    public void closeBeforeExecutorShutdown() throws Exception {
        Jsonb jsonb = JsonbBuilder.create();
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        // Submit tasks that use jsonb
        for (int i = 0; i < 10; i++) {
            final int taskNum = i;
            executor.submit(() -> {
                String json = jsonb.toJson(new Person("Person" + taskNum, 20 + taskNum));
                System.out.println(json);
            });
        }
        
        // WARNING: Calling close() before executor.shutdown()
        // Threads might still be executing tasks
        jsonb.close();
        
        executor.shutdown();
    }

    /**
     * WARNING: Close called after shutdown but before awaitTermination.
     * Shutdown is called but we don't wait for threads to finish.
     * @throws Exception 
     */
    public void closeAfterShutdownWithoutAwait() throws Exception {
        Jsonb jsonb = JsonbBuilder.create();
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        for (int i = 0; i < 10; i++) {
            final int taskNum = i;
            executor.submit(() -> {
                String json = jsonb.toJson(new Person("Person" + taskNum, 20 + taskNum));
                System.out.println(json);
            });
        }
        
        executor.shutdown();
        
        // WARNING: Calling close() right after shutdown without awaitTermination
        // Threads might still be finishing their work
        jsonb.close();
    }

    /**
     * WARNING: Threads are created and close() is called,
     * but join() happens AFTER close() (wrong order).
     * @throws Exception 
     */
    public void closeBeforeJoin() throws Exception {
        Jsonb jsonb = JsonbBuilder.create();
        Thread[] threads = new Thread[5];
        
        for (int i = 0; i < 5; i++) {
            final int threadNum = i;
            threads[i] = new Thread(() -> {
                String json = jsonb.toJson(new Person("Person" + threadNum, 20 + threadNum));
                System.out.println(json);
            });
            threads[i].start();
        }
        
        // WARNING: Calling close() BEFORE join()
        // This is unsafe - threads are still running
        jsonb.close();
        
        // Join happens too late
        for (Thread thread : threads) {
            thread.join();
        }
    }

    /**
     * Helper class for demonstration
     */
    private static class Person {
        private String name;
        private int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }
}

// Made with Bob
