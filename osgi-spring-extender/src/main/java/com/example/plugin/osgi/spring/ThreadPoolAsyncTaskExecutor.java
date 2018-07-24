package com.example.plugin.osgi.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Executes spring tasks using a cached thread pool that expands as necessary.  Overrides the default Spring executor
 * that spawns a new thread for every application context creation.
 *
 * @since 2.5.0
 */
public class ThreadPoolAsyncTaskExecutor implements AsyncTaskExecutor {
    private static final Logger log = LoggerFactory.getLogger(ThreadPoolAsyncTaskExecutor.class);

    private final ExecutorService executor = Executors.newCachedThreadPool(new NamedThreadFactory());

    /**
     * Executes the runnable
     *
     * @param task         The runnable task
     * @param startTimeout The start timeout (ignored)
     */
    @Override
    public void execute(Runnable task, long startTimeout) {
        // yes, we ignore the start timeout
        executor.execute(task);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return executor.submit(task);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }

    /**
     * Executes the runnable
     *
     * @param task The runnable task
     */
    @Override
    public void execute(Runnable task) {
        this.execute(task, -1);
    }

    /**
     * Shuts down the internal {@code ExecutorService} to ensure that all threads are stopped in order to allow the JVM
     * to terminate cleanly in a timely fashion.
     */
    public void shutdown() {
        log.debug("Attempting to shutdown ExecutorService");

        executor.shutdown();
        try {
            if (executor.awaitTermination(5, TimeUnit.SECONDS)) {
                log.debug("ExecutorService has shutdown gracefully");
            } else {
                //The executor did not shutdown within the timeout. We can't wait forever, though, so issue a
                //shutdownNow() and give it another 5 seconds
                log.warn("ExecutorService did not shutdown within the timeout; forcing shutdown");

                executor.shutdownNow();
                if (executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    //The forced shutdown has brought the executor down. Not ideal, but acceptable
                    log.debug("ExecutorService has been forced to shutdown");
                } else {
                    //We can't delay execution indefinitely waiting, so log a warning. The JVM may not shut down
                    //if this service does not stop (because it uses non-daemon threads), so this may be helpful
                    //in debugging should that happen.
                    log.warn("ExecutorService did not shutdown; it will be abandoned");
                }
            }
        } catch (InterruptedException e) {
            log.warn("Interrupted while waiting for the executor service to shutdown; some worker threads may " +
                    "still be running");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Thread factory that names the threads for the executor
     */
    private static class NamedThreadFactory implements ThreadFactory {
        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setDaemon(false);
            thread.setName("ThreadPoolAsyncTaskExecutor::Thread " + counter.incrementAndGet());
            return thread;
        }
    }
}
