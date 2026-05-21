package io.openliberty.sample.jakarta.jsonb;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

public class JsonbCloseInvalid {

	private static final Jsonb jsonb = JsonbBuilder.create();

	public static void main(String[] args) throws Exception {
	        useThreadFactory();
	        useExecutorService();
	        useCompletableFuture();
	        useThreadDirect();
	        useTimer();
	        reuseJsonbInstance();
	        singleThreadedWithClose();
	        closeAfterExecutorTermination();
	}
	private static void useThreadFactory() throws Exception {
	        ThreadFactory factory = r -> new Thread(r, "custom-thread");
	        Thread t = factory.newThread(() -> {
	            String json = jsonb.toJson("ThreadFactory example");
	        });
	        t.start();
	}
	private static void useExecutorService() throws Exception {
	        ExecutorService executor = Executors.newFixedThreadPool(2);
	        executor.submit(() -> {
	            jsonb.toJson("Executor example");
	        });
	}
	private static void useCompletableFuture() throws Exception {
	        CompletableFuture.runAsync(() -> {
	            String json = jsonb.toJson("CompletableFuture example");
	        });
	}
	private static void useThreadDirect() throws Exception {
        Thread t = new Thread(() -> {
            String json = jsonb.toJson("Direct Thread example");
        });
        t.start();
    }
	private static void useTimer() throws Exception {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                String json = jsonb.toJson("Timer example");
            }
        }, 1000);
    }
	public static void reuseJsonbInstance() {
        // Multiple operations with the same instance
        for (int i = 0; i < 5; i++) {
            String json = jsonb.toJson(new Person("Person" + i, 20 + i));
        }
        // No close() - instance can be reused or garbage collected naturally
    }
	public static void singleThreadedWithClose() throws Exception {
        try {
            String json = jsonb.toJson(new Person("Jane", 25));
        } finally {
            // VALID: No threads, so close() is safe
            jsonb.close();
        }
    }
	public static void closeAfterExecutorTermination() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        // Submit tasks that use jsonb
        for (int i = 0; i < 10; i++) {
            final int taskNum = i;
            executor.submit(() -> {
                String json = jsonb.toJson(new Person("Person" + taskNum, 20 + taskNum));
            });
        }
        jsonb.close();
        // VALID: Properly shutdown and wait for termination
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        // Now it's safe to close - all tasks have completed
    }
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