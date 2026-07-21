package io.openliberty.sample.jakarta.jsonb;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import java.util.concurrent.*;

/**
 * Test scenarios for GLOBAL Jsonb instances (class-level fields).
 * These scenarios should NOT trigger diagnostics because global instances
 * should not be closed in individual methods.
 */
public class JsonbGlobalInstanceClosableValid {

    // Global Jsonb instances - shared across all methods
    private static final Jsonb STATIC_JSONB = JsonbBuilder.create();
    private Jsonb instanceJsonb = JsonbBuilder.create();

    // ========== ALL SCENARIOS BELOW SHOULD NOT TRIGGER DIAGNOSTICS ==========

    /**
     * Global Jsonb with ExecutorService
     * EXPECTED: :white_check_mark: NO diagnostic (global instance, correct to not close)
     */
    public void globalJsonbWithExecutor() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(() -> STATIC_JSONB.toJson(new Person("Alice", 30)));
        // No close() - CORRECT for global instance
    }

    /**
     * Global Jsonb with CompletableFuture
     * EXPECTED: :white_check_mark: NO diagnostic (global instance, correct to not close)
     */
    public void globalJsonbWithCompletableFuture() {
        CompletableFuture.runAsync(() -> STATIC_JSONB.toJson(new Person("Bob", 25)));
        // No close() - CORRECT for global instance
    }

    /**
     * Global Jsonb with Thread
     * EXPECTED: :white_check_mark: NO diagnostic (global instance, correct to not close)
     */
    public void globalJsonbWithThread() {
        new Thread(() -> STATIC_JSONB.toJson(new Person("Charlie", 35))).start();
        // No close() - CORRECT for global instance
    }

    /**
     * Instance field Jsonb with threads
     * EXPECTED: :white_check_mark: NO diagnostic (field instance, correct to not close)
     */
    public void instanceFieldJsonbWithThreads() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(() -> instanceJsonb.toJson(new Person("Dave", 28)));
        // No close() - CORRECT for field instance
    }

    /**
     * Global Jsonb without threads
     * EXPECTED: :white_check_mark: NO diagnostic (no threads)
     */
    public void globalJsonbWithoutThreads() {
        String json = STATIC_JSONB.toJson(new Person("Eve", 32));
        // No threads, no diagnostic
    }

    /**
     * Multiple operations with global Jsonb
     * EXPECTED: :white_check_mark: NO diagnostic (global instance)
     */
    public void multipleOperationsWithGlobalJsonb() {
        for (int i = 0; i < 5; i++) {
            STATIC_JSONB.toJson(new Person("Person" + i, 20 + i));
        }
        // No close() - CORRECT for global instance
    }

    // ========== HELPER CLASS ==========

    public static class Person {
        public String name;
        public int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public Person() {
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