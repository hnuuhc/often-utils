package org.haic.often.Multithread.Parameterized;

/**
 * record class four parameter's  parameterized Thread
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/3/14 17:23
 */
public record FourParameterized<T>(T A, T B, T C, T D, Runnable<T, T, T, T> runnable) implements Runnable {
	/**
	 * run method to be called in that separately executing thread.
	 */
	@Override public void run() {
		runnable.run(A, B, C, D);
	}

	/**
	 * Runnable defines the start method for starting a thread.
	 */
	public interface Runnable<A, B, C, D> {
		/**
		 * a method with parameter
		 */
		void run(A A, B B, C C, D D);
	}
}