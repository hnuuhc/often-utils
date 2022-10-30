package org.haic.often.thread.futuretask;

import java.util.concurrent.Callable;

/**
 * record class five parameter's  parameterized FutureTask Thread
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/3/14 17:46
 */
public record FiveFutureTask<A, B, C, D, E, V>(A A, B B, C C, D D, E E, Callable<A, B, C, D, E, V> callable) implements Callable<V> {

	@Override
	public V call() throws Exception {
		return callable.call(A, B, C, D, E);
	}

	/**
	 * FutureTaskThread defines the start method for starting a thread.
	 */
	public interface Callable<A, B, C, D, E, V> {
		V call(A A, B B, C C, D D, E E) throws Exception;
	}

}