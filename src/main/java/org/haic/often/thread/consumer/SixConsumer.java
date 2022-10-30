package org.haic.often.thread.consumer;

/**
 * <p>This is a parameterized interface from {@link java.lang.Runnable}</a>
 * allowing parameter operation.
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/3/14 17:23
 */
public record SixConsumer<A, B, C, D, E, F>(A A, B B, C C, D D, E E, F F, Runnable<A, B, C, D, E, F> runnable) implements Runnable {

	@Override
	public void run() {
		runnable.run(A, B, C, D, E, F);
	}

	/**
	 * Runnable defines the start method for starting a thread.
	 */
	public interface Runnable<A, B, C, D, E, F> {
		/**
		 * a method with parameter
		 */
		void run(A A, B B, C C, D D, E E, F F);
	}

}