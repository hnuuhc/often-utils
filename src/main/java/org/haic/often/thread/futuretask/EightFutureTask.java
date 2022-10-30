package org.haic.often.thread.futuretask;

import java.util.concurrent.Callable;

/**
 * record class eight parameter's  parameterized FutureTask Thread
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/3/14 17:46
 */
public record EightFutureTask<A, B, C, D, E, F, G, H, V>(A A, B B, C C, D D, E E, F F, G G, H H, Callable<A, B, C, D, E, F, G, H, V> callable) implements Callable<V> {

	@Override
	public V call() throws Exception {
		return callable.call(A, B, C, D, E, F, G, H);
	}

	/**
	 * FutureTaskThread defines the start method for starting a thread.
	 */
	public interface Callable<A, B, C, D, E, F, G, H, V> {
		V call(A A, B B, C C, D D, E E, F F, G G, H H) throws Exception;
	}

}