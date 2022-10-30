package org.haic.often.thread.consumer;

/**
 * <p>This is a parameterized interface from {@link java.lang.Runnable}</a>
 * allowing parameter operation.
 * s
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/3/14 17:23
 */
public record FiveConsumer<A, B, C, D, E>(A A, B B, C C, D D, E E, Runnable<A, B, C, D, E> runnable) implements Runnable {

	@Override
	public void run() {
		runnable.run(A, B, C, D, E);
	}

	/**
	 * Runnable defines the start method for starting a thread.
	 */
	public interface Runnable<A, B, C, D, E> {
		/**
		 * a method with parameter
		 */
		void run(A A, B B, C C, D D, E E);
	}

}