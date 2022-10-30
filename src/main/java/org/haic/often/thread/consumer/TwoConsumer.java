package org.haic.often.thread.consumer;

/**
 * <p>This is a parameterized interface from {@link java.lang.Runnable}</a>
 * allowing parameter operation.
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/3/14 17:23
 */
public record TwoConsumer<A, B>(A A, B B, Runnable<A, B> runnable) implements Runnable {

	@Override
	public void run() {
		runnable.run(A, B);
	}

	/**
	 * Runnable defines the start method for starting a thread.
	 */
	public interface Runnable<A, B> {
		/**
		 * a method with parameter
		 */
		void run(A A, B B);
	}

}