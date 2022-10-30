package org.haic.often.thread.futuretask;

import java.util.concurrent.Callable;

/**
 * record class three parameter's  parameterized FutureTask Thread
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/3/14 17:46
 */
public record ThreeFutureTask<A, B, C, V>(A A, B B, C C, Callable<A, B, C, V> callable) implements Callable<V> {

	@Override
	public V call() throws Exception {
		return callable.call(A, B, C);
	}

	/**
	 * FutureTaskThread defines the start method for starting a thread.
	 */
	public interface Callable<A, B, C, V> {
		V call(A A, B B, C C) throws Exception;
	}

}