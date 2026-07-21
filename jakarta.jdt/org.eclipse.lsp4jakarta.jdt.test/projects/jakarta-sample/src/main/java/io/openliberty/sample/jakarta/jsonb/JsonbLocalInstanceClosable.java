package io.openliberty.sample.jakarta.jsonb;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import java.util.concurrent.*;

/**
 * Test scenarios for LOCAL Jsonb instances (method-level).
 * These scenarios test when diagnostics SHOULD and SHOULD NOT trigger for local instances.
 */
public class JsonbLocalInstanceClosable {

    // ========== INVALID SCENARIOS - DIAGNOSTIC SHOULD TRIGGER ==========

    /**
     * INVALID: Local Jsonb with ExecutorService, no close()
     * EXPECTED: :warning: Diagnostic SHOULD trigger
     */
    public void localJsonbWithExecutorNoClose() {
        Jsonb jsonb = JsonbBuilder.create();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(() -> jsonb.toJson(new Person("Alice", 30)));
        // Missing close() - DIAGNOSTIC EXPECTED
    }

    /**
     * INVALID: Local Jsonb with CompletableFuture, no close()
     * EXPECTED: :warning: Diagnostic SHOULD trigger
     */
    public void localJsonbWithCompletableFutureNoClose() {
        Jsonb jsonb = JsonbBuilder.create();
        CompletableFuture.runAsync(() -> jsonb.toJson(new Person("Bob", 25)));
        // Missing close() - DIAGNOSTIC EXPECTED
    }

    /**
     * INVALID: Local Jsonb with Thread, no close()
     * EXPECTED: :warning: Diagnostic SHOULD trigger
     */
    public void localJsonbWithThreadNoClose() {
        Jsonb jsonb = JsonbBuilder.create();
        new Thread(() -> jsonb.toJson(new Person("Charlie", 35))).start();
        // Missing close() - DIAGNOSTIC EXPECTED
    }

    // ========== VALID SCENARIOS - NO DIAGNOSTIC EXPECTED ==========

    /**
     * VALID: Local Jsonb with threads AND proper close()
     * EXPECTED: :white_check_mark: NO diagnostic
     */
    public void localJsonbWithThreadsAndClose() throws Exception {
        Jsonb jsonb = JsonbBuilder.create();
        try {
            ExecutorService executor = Executors.newFixedThreadPool(2);
            executor.submit(() -> jsonb.toJson(new Person("Dave", 28)));
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } finally {
            jsonb.close(); // Properly closed - NO DIAGNOSTIC
        }
    }

    /**
     * VALID: Try-with-resources (auto-closes)
     * EXPECTED: :warning: Diagnostic triggers (known limitation - false positive)
     * NOTE: This is actually CORRECT code, but diagnostic doesn't recognize try-with-resources
     */
    public void localJsonbWithTryWithResources() throws Exception {
        try (Jsonb jsonb = JsonbBuilder.create()) {
            ExecutorService executor = Executors.newFixedThreadPool(2);
            executor.submit(() -> jsonb.toJson(new Person("Eve", 32)));
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } // Auto-closed - but diagnostic may trigger (known limitation)
    }

    /**
     * VALID: Local Jsonb without threads
     * EXPECTED: :white_check_mark: NO diagnostic (no thread safety concerns)
     */
    public void localJsonbWithoutThreads() {
        Jsonb jsonb = JsonbBuilder.create();
        String json = jsonb.toJson(new Person("Frank", 40));
        // No threads, no diagnostic even without close()
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