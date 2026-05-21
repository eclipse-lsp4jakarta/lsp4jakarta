package io.openliberty.sample.jakarta.jsonb;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Timer;
import java.util.TimerTask;

public class JsonbCloseCustomThreads {

    private static final Jsonb jsonb = JsonbBuilder.create();

    // Custom Runnable implementation
    static class CustomTask implements Runnable {
        @Override
        public void run() {
            String json = jsonb.toJson("Custom Runnable");
        }
    }

    // Custom Callable implementation
    static class CustomCallable implements Callable<String> {
        @Override
        public String call() {
            return jsonb.toJson("Custom Callable");
        }
    }

    // Custom Thread subclass
    static class CustomThread extends Thread {
        @Override
        public void run() {
            String json = jsonb.toJson("Custom Thread");
        }
    }

    // Custom ExecutorService wrapper
    static class CustomExecutor implements ExecutorService {
        private final ExecutorService delegate = Executors.newFixedThreadPool(2);

        @Override
        public void execute(Runnable command) {
            delegate.execute(command);
        }

        // Other ExecutorService methods delegated...
        @Override
        public void shutdown() {
            delegate.shutdown();
        }

        @Override
        public java.util.List<Runnable> shutdownNow() {
            return delegate.shutdownNow();
        }

        @Override
        public boolean isShutdown() {
            return delegate.isShutdown();
        }

        @Override
        public boolean isTerminated() {
            return delegate.isTerminated();
        }

        @Override
        public boolean awaitTermination(long timeout, java.util.concurrent.TimeUnit unit) {
            try {
                return delegate.awaitTermination(timeout, unit);
            } catch (InterruptedException e) {
                return false;
            }
        }

        @Override
        public <T> java.util.concurrent.Future<T> submit(Callable<T> task) {
            return delegate.submit(task);
        }

        @Override
        public <T> java.util.concurrent.Future<T> submit(Runnable task, T result) {
            return delegate.submit(task, result);
        }

        @Override
        public java.util.concurrent.Future<?> submit(Runnable task) {
            return delegate.submit(task);
        }

        @Override
        public <T> java.util.List<java.util.concurrent.Future<T>> invokeAll(
                java.util.Collection<? extends Callable<T>> tasks) {
            try {
                return delegate.invokeAll(tasks);
            } catch (InterruptedException e) {
                return null;
            }
        }

        @Override
        public <T> java.util.List<java.util.concurrent.Future<T>> invokeAll(
                java.util.Collection<? extends Callable<T>> tasks, long timeout, java.util.concurrent.TimeUnit unit) {
            try {
                return delegate.invokeAll(tasks, timeout, unit);
            } catch (InterruptedException e) {
                return null;
            }
        }

        @Override
        public <T> T invokeAny(java.util.Collection<? extends Callable<T>> tasks) {
            try {
                return delegate.invokeAny(tasks);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public <T> T invokeAny(java.util.Collection<? extends Callable<T>> tasks, long timeout,
                java.util.concurrent.TimeUnit unit) {
            try {
                return delegate.invokeAny(tasks, timeout, unit);
            } catch (Exception e) {
                return null;
            }
        }
    }

    // Should trigger warning - uses custom Runnable without close
    public static void useCustomRunnable() {
        CustomTask task = new CustomTask();
        new Thread(task).start();
    }

    // Should trigger warning - uses custom Callable without close
    public static void useCustomCallable() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new CustomCallable());
    }

    // Should trigger warning - uses custom Thread without close
    public static void useCustomThread() {
        CustomThread thread = new CustomThread();
        thread.start();
    }

    // Should trigger warning - uses custom ExecutorService without close
    public static void useCustomExecutor() {
        CustomExecutor executor = new CustomExecutor();
        executor.execute(() -> {
            String json = jsonb.toJson("Custom Executor");
        });
    }

    // Should NOT trigger warning - has close() call
    public static void useCustomRunnableWithClose() throws Exception {
        try {
            CustomTask task = new CustomTask();
            new Thread(task).start();
        } finally {
            jsonb.close();
        }
    }

    // Should trigger warning - uses ScheduledExecutorService with jsonb without close
    public static void useScheduledExecutorWithJsonb() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> {
            String json = jsonb.toJson("Scheduled Task");
        }, 1, TimeUnit.SECONDS);
    }

    // Should trigger warning - uses Timer/TimerTask with jsonb without close
    public static void useTimerTaskWithJsonb() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                String json = jsonb.toJson("Timer Task");
            }
        }, 1000);
    }

    // Should NOT trigger warning - no jsonb usage even with threads
    public static void useThreadsWithoutJsonb() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            return "done";
        });
    }

    // Should trigger warning - multiple thread operations with jsonb without close
    public static void multipleThreadOperationsWithJsonb() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(() -> jsonb.toJson("Task 1"));
        executor.submit(() -> jsonb.toJson("Task 2"));
    }

    // Should NOT trigger warning - try-with-resources ensures close
    public static void useThreadsWithTryWithResources() throws Exception {
        try (Jsonb localJsonb = JsonbBuilder.create()) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> localJsonb.toJson("Safe usage"));
        }
    }

    // ========== COMPREHENSIVE TEST SCENARIOS ==========

    // SCENARIO 1: ForkJoinPool with jsonb (should warn)
    public static void useForkJoinPoolWithJsonb() {
        java.util.concurrent.ForkJoinPool pool = new java.util.concurrent.ForkJoinPool();
        pool.submit(() -> {
            String json = jsonb.toJson("ForkJoinPool Task");
            return json;
        });
    }

    // SCENARIO 2: Parallel Stream with jsonb (should warn)
    public static void useParallelStreamWithJsonb() {
        java.util.Arrays.asList("a", "b", "c").parallelStream()
            .map(s -> jsonb.toJson(s))
            .count(); // Just consume the stream without printing
    }

    // SCENARIO 3: CompletableFuture with jsonb (should warn)
    public static void useCompletableFutureWithJsonb() {
        java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            return jsonb.toJson("CompletableFuture");
        });
    }

    // SCENARIO 4: Thread.start() with jsonb (should warn)
    public static void useThreadStartWithJsonb() {
        new Thread(() -> {
            String json = jsonb.toJson("Thread.start");
        }).start();
    }

    // SCENARIO 5: Executor.execute() with jsonb (should warn)
    public static void useExecutorExecuteWithJsonb() {
        java.util.concurrent.Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            String json = jsonb.toJson("Executor.execute");
        });
    }

    // SCENARIO 6: ScheduledExecutorService.scheduleAtFixedRate with jsonb (should warn)
    public static void useScheduleAtFixedRateWithJsonb() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            String json = jsonb.toJson("Fixed Rate Task");
        }, 0, 1, TimeUnit.SECONDS);
    }

    // SCENARIO 7: ScheduledExecutorService.scheduleWithFixedDelay with jsonb (should warn)
    public static void useScheduleWithFixedDelayWithJsonb() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleWithFixedDelay(() -> {
            String json = jsonb.toJson("Fixed Delay Task");
        }, 0, 1, TimeUnit.SECONDS);
    }

    // SCENARIO 8: Timer.schedule with TimerTask and jsonb (should warn)
    public static void useTimerScheduleWithJsonb() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                String json = jsonb.toJson("Timer Schedule");
            }
        }, 1000);
    }

    // SCENARIO 9: Timer.scheduleAtFixedRate with jsonb (should warn)
    public static void useTimerScheduleAtFixedRateWithJsonb() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                String json = jsonb.toJson("Timer Fixed Rate");
            }
        }, 0, 1000);
    }

    // SCENARIO 10: Nested thread creation with jsonb (should warn)
    public static void useNestedThreadsWithJsonb() {
        new Thread(() -> {
            new Thread(() -> {
                String json = jsonb.toJson("Nested Thread");
            }).start();
        }).start();
    }

    // SCENARIO 11: Lambda with ExecutorService.submit and jsonb (should warn)
    public static void useLambdaSubmitWithJsonb() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> jsonb.toJson("Lambda Submit"));
    }

    // SCENARIO 12: Method reference with thread and jsonb (should warn)
    public static void useMethodReferenceWithJsonb() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> processWithJsonb());
    }

    private static String processWithJsonb() {
        return jsonb.toJson("Method Reference");
    }

    // SCENARIO 13: Anonymous Runnable with jsonb (should warn)
    public static void useAnonymousRunnableWithJsonb() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String json = jsonb.toJson("Anonymous Runnable");
            }
        }).start();
    }

    // SCENARIO 14: Anonymous Callable with jsonb (should warn)
    public static void useAnonymousCallableWithJsonb() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new Callable<String>() {
            @Override
            public String call() {
                return jsonb.toJson("Anonymous Callable");
            }
        });
    }

    // SCENARIO 15: Thread with Runnable constructor and jsonb (should warn)
    public static void useThreadConstructorWithJsonb() {
        Thread thread = new Thread(() -> {
            String json = jsonb.toJson("Thread Constructor");
        });
        thread.start();
    }

    // SCENARIO 16: invokeAll with jsonb (should warn)
    public static void useInvokeAllWithJsonb() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        java.util.List<Callable<String>> tasks = java.util.Arrays.asList(
            () -> jsonb.toJson("Task 1"),
            () -> jsonb.toJson("Task 2")
        );
        executor.invokeAll(tasks);
    }

    // SCENARIO 17: invokeAny with jsonb (should warn)
    public static void useInvokeAnyWithJsonb() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        java.util.List<Callable<String>> tasks = java.util.Arrays.asList(
            () -> jsonb.toJson("Task 1"),
            () -> jsonb.toJson("Task 2")
        );
        executor.invokeAny(tasks);
    }

    // SCENARIO 18: Local jsonb variable with thread (should NOT warn - different instance)
    public static void useLocalJsonbWithThread() throws Exception {
        Jsonb localJsonb = JsonbBuilder.create();
        new Thread(() -> {
            String json = localJsonb.toJson("Local Jsonb");
        }).start();
        localJsonb.close();
    }

    // SCENARIO 19: Thread without jsonb usage (should NOT warn)
    public static void useThreadWithoutJsonb() {
        new Thread(() -> {
        }).start();
    }

    // SCENARIO 20: ExecutorService without jsonb usage (should NOT warn)
    public static void useExecutorWithoutJsonb() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            return "done";
        });
    }

    // SCENARIO 21: Jsonb usage without threads (should NOT warn)
    public static void useJsonbWithoutThreads() {
        String json = jsonb.toJson("No threads");
    }

    // SCENARIO 22: Thread with jsonb and close() in finally (should NOT warn)
    public static void useThreadWithFinallyClose() throws Exception {
        try {
            new Thread(() -> {
                String json = jsonb.toJson("Thread with finally");
            }).start();
        } finally {
            jsonb.close();
        }
    }

    // SCENARIO 23: Thread with jsonb and close() after thread (should NOT warn)
    public static void useThreadWithCloseAfter() throws Exception {
        new Thread(() -> {
            String json = jsonb.toJson("Thread with close after");
        }).start();
        jsonb.close();
    }

    // SCENARIO 24: Multiple threads with single close() (should NOT warn)
    public static void useMultipleThreadsWithSingleClose() throws Exception {
        new Thread(() -> jsonb.toJson("Thread 1")).start();
        new Thread(() -> jsonb.toJson("Thread 2")).start();
        jsonb.close();
    }

    // SCENARIO 25: Cached thread pool with jsonb (should warn)
    public static void useCachedThreadPoolWithJsonb() {
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.submit(() -> jsonb.toJson("Cached Thread Pool"));
    }

    // SCENARIO 26: Fixed thread pool with jsonb (should warn)
    public static void useFixedThreadPoolWithJsonb() {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        executor.submit(() -> jsonb.toJson("Fixed Thread Pool"));
    }

    // SCENARIO 27: Single thread executor with jsonb (should warn)
    public static void useSingleThreadExecutorWithJsonb() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> jsonb.toJson("Single Thread Executor"));
    }

    // SCENARIO 28: Work stealing pool with jsonb (should warn)
    public static void useWorkStealingPoolWithJsonb() {
        ExecutorService executor = Executors.newWorkStealingPool();
        executor.submit(() -> jsonb.toJson("Work Stealing Pool"));
    }

    // SCENARIO 29: Daemon thread with jsonb (should warn)
    public static void useDaemonThreadWithJsonb() {
        Thread thread = new Thread(() -> {
            String json = jsonb.toJson("Daemon Thread");
        });
        thread.setDaemon(true);
        thread.start();
    }

    public static void useThreadGroupWithJsonb() throws Exception {
        ThreadGroup group = new ThreadGroup("MyGroup");
        new Thread(group, () -> {
            String json = jsonb.toJson("Thread Group");
        }).start();
        try (Jsonb localJsonb = JsonbBuilder.create()) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> localJsonb.toJson("Safe usage"));
        }
         finally {
            jsonb.close();
        }
    }
}
