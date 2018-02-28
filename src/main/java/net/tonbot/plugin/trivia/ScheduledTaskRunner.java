package net.tonbot.plugin.trivia;

import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

class ScheduledTaskRunner {

	private static Logger LOG = LoggerFactory.getLogger(ScheduledTaskRunner.class);
	
	private ScheduledExecutorService executorService;
	private ScheduledFuture<?> scheduledTaskFuture;
	private ReentrantLock lock;

	public ScheduledTaskRunner() {
		ThreadFactory tf = new ThreadFactoryBuilder().setNameFormat("Scheduled Task Runner %d").build();
		this.executorService = Executors.newScheduledThreadPool(1, tf);
		this.lock = new ReentrantLock();
	}

	/**
	 * Schedules a task, replacing the previous one, if any.
	 * 
	 * @param runnable
	 *            A new runnable to be run. Non-null.
	 * @param delay
	 *            The delay. Must be positive.
	 * @param timeUnit
	 *            The {@link TimeUnit}. Non-null.
	 */
	public void replaceSchedule(Runnable runnable, long delay, TimeUnit timeUnit) {
		Preconditions.checkNotNull(runnable, "runnable must be non-null.");
		Preconditions.checkArgument(delay > 0, "delay must be positive.");
		Preconditions.checkNotNull(timeUnit, "timeUnit must be positive.");

		lock.lock();
		try {
			if (this.scheduledTaskFuture != null) {
				this.scheduledTaskFuture.cancel(false);
			}

			this.scheduledTaskFuture = this.executorService.schedule(new TimerTask() {

				@Override
				public void run() {
					runnable.run();
				}

			}, delay, timeUnit);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Cancels the previously scheduled task, if it exists. Tasks may still be
	 * scheduled after cancellation of the previous task. 
	 * This operation does not interrupt any currently executing task.
	 */
	public void cancel() {
		lock.lock();
		try {
			if (this.scheduledTaskFuture != null) {
				this.scheduledTaskFuture.cancel(false);
			}
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * Attempts to stop all actively executing tasks, and prevent queued tasks from executing.
	 */
	public void shutdownNow() {
		this.executorService.shutdownNow();
		LOG.info("Shutting down ScheduledTaskRunner.");
	}
}
