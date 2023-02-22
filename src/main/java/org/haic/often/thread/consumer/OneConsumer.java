package org.haic.often.thread.consumer;

/**
 * <p>This is a parameterized interface from {@link java.lang.Runnable}</a>
 * allowing parameter operation.
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/3/14 17:23
 */
public record OneConsumer<A>(A A, Runnable<A> runnable) implements Runnable {

	@Override
	public void run() {
		runnable.run(A);
	}

	/**
	 * Runnable defines the start method for starting a thread.
	 */
	public interface Runnable<A> {
		/**
		 * a method with parameter
		 */
		void run(A A);
	}

}
