package org.haic.often.thread.futuretask;

import java.util.concurrent.Callable;

/**
 * record class seven parameter's  parameterized FutureTask Thread
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/3/14 17:46
 */
public record SevenFutureTask<A, B, C, D, E, F, G, V>(A A, B B, C C, D D, E E, F F, G G, Callable<A, B, C, D, E, F, G, V> callable) implements Callable<V> {

	@Override
	public V call() throws Exception {
		return callable.call(A, B, C, D, E, F, G);
	}

	/**
	 * FutureTaskThread defines the start method for starting a thread.
	 */
	public interface Callable<A, B, C, D, E, F, G, V> {
		V call(A A, B B, C C, D D, E E, F F, G G) throws Exception;
	}

}