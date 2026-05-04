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
import java.util.concurrent.TimeUnit;

/**
 * Valid usage example: Using Jsonb safely with proper thread synchronization.
 * 
 * This class demonstrates scenarios that should NOT trigger the WARNING diagnostic
 * because either no threads are used, or close() is called after proper synchronization.
 */
public class JsonbCloseValid {

    /**
     * VALID: This method uses Jsonb but doesn't call close().
     * No diagnostic should appear.
     */
    public void processDataWithoutClose() {
        Jsonb jsonb = JsonbBuilder.create();
        
        try {
            // Serialize some data
            String json = jsonb.toJson(new Person("John", 30));
            System.out.println(json);
            
            // Deserialize data
            Person person = jsonb.fromJson(json, Person.class);
            System.out.println("Name: " + person.getName());
            
            // No close() call - this is valid and no diagnostic should appear
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * VALID: Single-threaded usage with close().
     * No threads are created, so close() is safe.
     * @throws Exception 
     */
    public void singleThreadedWithClose() throws Exception {
        Jsonb jsonb = JsonbBuilder.create();
        
        try {
            String json = jsonb.toJson(new Person("Jane", 25));
            System.out.println(json);
        } finally {
            // VALID: No threads, so close() is safe
            jsonb.close();
        }
    }

    /**
     * VALID: Threads are properly joined before calling close().
     * This ensures all threads have finished before cleanup.
     * @throws Exception 
     */
    public void closeAfterJoiningThreads() throws Exception {
        Jsonb jsonb = JsonbBuilder.create();
        Thread[] threads = new Thread[5];
        
        // Start multiple threads that use jsonb
        for (int i = 0; i < 5; i++) {
            final int threadNum = i;
            threads[i] = new Thread(() -> {
                String json = jsonb.toJson(new Person("Person" + threadNum, 20 + threadNum));
                System.out.println(json);
            });
            threads[i].start();
        }
        
        // VALID: Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        // Now it's safe to close - all threads have finished
        jsonb.close();
    }

    /**
     * VALID: ExecutorService is properly shut down and awaited before close().
     * @throws Exception 
     */
    public void closeAfterExecutorTermination() throws Exception {
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
        
        // VALID: Properly shutdown and wait for termination
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        
        // Now it's safe to close - all tasks have completed
        jsonb.close();
    }

    /**
     * VALID: Using Jsonb in a multi-threaded context without calling close().
     * This is safe because we're not calling close() at all.
     */
    public void multiThreadedProcessingWithoutClose() throws InterruptedException {
        Jsonb jsonb = JsonbBuilder.create();
        Thread[] threads = new Thread[5];
        
        // Start multiple threads that use jsonb
        for (int i = 0; i < 5; i++) {
            final int threadNum = i;
            threads[i] = new Thread(() -> {
                String json = jsonb.toJson(new Person("Person" + threadNum, 20 + threadNum));
                System.out.println(json);
            });
            threads[i].start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }
        
        // VALID: Even after threads complete, we don't call close()
        // This is safe - no diagnostic should appear
    }

    /**
     * VALID: Method that doesn't use Jsonb at all.
     * Obviously no diagnostic should appear.
     */
    public void processDataWithoutJsonb() {
        Person person = new Person("Alice", 28);
        System.out.println("Name: " + person.getName() + ", Age: " + person.getAge());
    }

    /**
     * VALID: Reusing a Jsonb instance across multiple operations without threads.
     * No close() is called, so no diagnostic.
     */
    public void reuseJsonbInstance() {
        Jsonb jsonb = JsonbBuilder.create();
        
        // Multiple operations with the same instance
        for (int i = 0; i < 5; i++) {
            String json = jsonb.toJson(new Person("Person" + i, 20 + i));
            System.out.println(json);
        }
        
        // No close() - instance can be reused or garbage collected naturally
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

        public Person() {
            // Default constructor for JSON-B
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }
}

// Made with Bob
