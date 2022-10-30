package org.haic.often.thread.futuretask;

import java.util.concurrent.Callable;

/**
 * record class two parameter's  parameterized FutureTask Thread
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/3/14 17:46
 */
public record TwoFutureTask<A, B, V>(A A, B B, Callable<A, B, V> callable) implements Callable<V> {

	/**
	 * call method to be called in that separately executing thread.
	 */
	@Override
	public V call() throws Exception {
		return callable.call(A, B);
	}

	/**
	 * FutureTaskThread defines the start method for starting a thread.
	 */
	public interface Callable<A, B, V> {
		/**
		 * a method with parameter
		 */
		V call(A A, B B) throws Exception;
	}
}