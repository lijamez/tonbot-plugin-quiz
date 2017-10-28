package net.tonbot.plugin.trivia;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Preconditions;

class TriviaQuestionTimer {

	private Timer timer = null;
	private ReentrantLock lock = new ReentrantLock();

	/**
	 * Schedules a task, replacing the previous one, if any.
	 * 
	 * @param runnable
	 *            A new runnable to be run. Non-null.
	 * @param delayMs
	 *            The delay in milliseconds.
	 */
	public void replaceSchedule(Runnable runnable, long delayMs) {
		Preconditions.checkNotNull(runnable, "runnable must be non-null.");
		Preconditions.checkArgument(delayMs > 0, "delayMs must be positive.");

		lock.lock();
		try {
			if (this.timer != null) {
				this.timer.cancel();
			}

			this.timer = new Timer();
			this.timer.schedule(new TimerTask() {

				@Override
				public void run() {
					runnable.run();
				}

			}, delayMs);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Cancels the previously scheduled task, if it exists. Tasks may still be
	 * scheduled after cancellation of the previous task.
	 */
	public void cancel() {
		lock.lock();
		try {
			if (this.timer != null) {
				this.timer.cancel();
				this.timer = null;
			}
		} finally {
			lock.unlock();
		}
	}
}
