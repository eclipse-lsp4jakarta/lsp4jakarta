package org.eclipse.lsp4jakarta.jdt.internal.jsonb;

/**
 * JsonbUtils
 */
public class JsonbUtils {
	/**
     * Checks if the source code contains a reference to Jsonb.
     *
     * @param source the method source code
     * @return true if Jsonb is referenced
     */
	public static boolean hasJsonbReference(String source) {
        return source.contains(Constants.JSONB_PREFIX) || source.contains(Constants.JAKARTA_JSONB_CLOSEABLE);
    }
    
    /**
     * Checks if the source code contains a close() method call.
     *
     * @param source the method source code
     * @return true if close() is called
     */
    public static boolean hasCloseCall(String source) {
        return source.contains(Constants.CLOSEABLE_CLOSE_METHOD);
    }
    
    /**
     * Checks if the source code creates threads or uses thread pools.
     *
     * @param source the method source code
     * @return true if threads are created
     */
    public static boolean hasThreadCreation(String source) {
        return source.contains("new Thread") ||
               source.contains("ExecutorService") ||
               source.contains("ThreadPoolExecutor") ||
               source.contains("Executors.") ||
               source.contains(".submit(") ||
               source.contains(".execute(");
    }
    
    /**
     * Checks if the source code contains thread synchronization mechanisms.
     *
     * @param source the method source code
     * @return true if synchronization mechanisms are present
     */
    private static boolean hasThreadSynchronization(String source) {
        return source.contains(".join()") ||
               source.contains(".shutdown()") ||
               source.contains(".awaitTermination(") ||
               source.contains("CountDownLatch") ||
               source.contains("CyclicBarrier") ||
               source.contains("Phaser");
    }
    
    /**
     * Determines if thread synchronization occurs before close() calls.
     *
     * This method checks the ordering of synchronization points (join, awaitTermination)
     * relative to close() calls to determine if the code is thread-safe.
     *
     * Note: shutdown() alone is NOT sufficient synchronization - it only initiates shutdown
     * but doesn't wait for tasks to complete. Only join() or awaitTermination() provide
     * complete synchronization.
     *
     * @param source the method source code
     * @return true if synchronization properly occurs before close(), false otherwise
     */
    public static boolean isSynchronizationBeforeClose(String source) {
        if (!hasThreadSynchronization(source)) {
            return false; // No synchronization present
        }
        
        int closeIndex = source.indexOf(Constants.CLOSEABLE_CLOSE_METHOD);
        int joinIndex = source.indexOf(".join()");
        int shutdownIndex = source.indexOf(".shutdown()");
        int awaitIndex = source.indexOf(".awaitTermination(");
        
        // For ExecutorService: shutdown() must be followed by awaitTermination()
        // shutdown() alone is incomplete synchronization
        if (shutdownIndex > 0 && awaitIndex < 0) {
            // shutdown() without awaitTermination() is incomplete
            return false;
        }
        
        // Find the last COMPLETE synchronization point
        // Only join() or awaitTermination() count as complete synchronization
        int lastCompleteSyncIndex = Math.max(joinIndex, awaitIndex);
        
        if (lastCompleteSyncIndex < 0) {
            // No complete synchronization found
            return false;
        }
        
        // Synchronization must occur before close()
        // If close() comes before synchronization, it's unsafe
        if (closeIndex > 0 && closeIndex < lastCompleteSyncIndex) {
            return false;
        }
        
        // If synchronization happens before close(), it's safe
        if (closeIndex > 0 && lastCompleteSyncIndex < closeIndex) {
            return true;
        }
        
        // Unable to determine order clearly
        return false;
    }
}
