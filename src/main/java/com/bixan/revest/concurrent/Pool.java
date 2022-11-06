/**
 * 
 */
package com.bixan.revest.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author sambit
 *
 */
@Component
public class Pool {
	private static final Logger log = LoggerFactory.getLogger(Pool.class);
	private static BlockingQueue<Runnable> taskQueue = null;
	
	@Value("${threadpool.size:20}")
	private int poolSize;
	
	@Value("${threadpool.max-size:50}")
	private int maxPoolSize;
	
	@Value("${threadpool.keepalive-ms:30000}")
	private int keepaliveMs;
	
	@PostConstruct
	public void init() {		
		// initiate thread-pool
		if (null == Pool.taskQueue) {
			Pool.taskQueue = new LinkedBlockingQueue<Runnable>();
		}
		ThreadPoolExecutor pool = new ThreadPoolExecutor(poolSize, maxPoolSize,
				keepaliveMs, TimeUnit.MILLISECONDS, taskQueue);
		
		pool.prestartAllCoreThreads();
	}
	
	public BlockingQueue<Runnable> getTaskQueue() {
		return taskQueue;
	}
}
