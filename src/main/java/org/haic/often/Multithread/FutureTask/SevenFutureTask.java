package org.haic.often.Multithread.FutureTask;

import java.util.concurrent.Callable;

/**
 * record class seven parameter's  parameterized FutureTask Thread
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/3/14 17:46
 */
public record SevenFutureTask<T, V>(T A, T B, T C, T D, T E, T F, T G, Callable<T, T, T, T, T, T, T, V> callable) implements Callable<V> {
	/**
	 * run method to be called in that separately executing thread.
	 */
	@Override public V call() throws Exception {
		return callable.call(A, B, C, D, E, F, G);
	}

	/**
	 * FutureTaskThread defines the start method for starting a thread.
	 */
	public interface Callable<A, B, C, D, E, F, G, V> {
		V call(A A, B B, C C, D D, E E, F F, G G) throws Exception;
	}
}