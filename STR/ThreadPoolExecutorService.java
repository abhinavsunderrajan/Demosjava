package utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Singleton instance of a thread-pool of fixed size.
 * 
 * @author abhinav.sunderrajan
 *
 */
public class ThreadPoolExecutorService {

    // private ScheduledThreadPoolExecutor executor;
    private ThreadPoolExecutor executor;
    private static ThreadPoolExecutorService executorUtils;

    private ThreadPoolExecutorService(int numberofThreads) {

	ThreadFactory threadFactory = Executors.defaultThreadFactory();

	RejectedExecutionHandler handler = new RejectedExecutionHandler() {
	    @Override
	    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		System.out.println("Task Rejected : " + (r));
	    }
	};

	executor = new ThreadPoolExecutor(numberofThreads, numberofThreads, 5000000, TimeUnit.SECONDS,
		new ArrayBlockingQueue<Runnable>(5000000), threadFactory, handler);

	Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

	    @Override
	    public void run() {
		System.out.println("Shutting down executor");
		executor.shutdown();

	    }
	}));

    }

    /**
     * Get a singleton instance of a thread pool executor.
     * 
     * @param numberofThreads
     *            number of threads.
     * @return the singleton instance of the thread pool executors.
     */
    public static ThreadPoolExecutorService getExecutorInstance(int numberofThreads) {
	if (executorUtils == null) {
	    executorUtils = new ThreadPoolExecutorService(numberofThreads);
	}

	return executorUtils;

    }

    /**
     * @return the executor
     */
    public ThreadPoolExecutor getExecutor() {
	return executor;
    }

    /**
     * Invalidate the instance to create a new one.
     */
    public static void invalidateInstance() {
	executorUtils = null;
    }

}