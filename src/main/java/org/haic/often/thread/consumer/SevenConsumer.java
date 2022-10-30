package org.haic.often.thread.consumer;

/**
 * <p>This is a parameterized interface from {@link java.lang.Runnable}</a>
 * allowing parameter operation.
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/3/14 17:23
 */
public record SevenConsumer<A, B, C, D, E, F, G>(A A, B B, C C, D D, E E, F F, G G, Runnable<A, B, C, D, E, F, G> runnable) implements Runnable {

	@Override
	public void run() {
		runnable.run(A, B, C, D, E, F, G);
	}

	/**
	 * Runnable defines the start method for starting a thread.
	 */
	public interface Runnable<A, B, C, D, E, F, G> {
		/**
		 * a method with parameter
		 */
		void run(A A, B B, C C, D D, E E, F F, G G);
	}

}