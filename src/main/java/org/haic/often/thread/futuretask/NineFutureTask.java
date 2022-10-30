package org.haic.often.thread.futuretask;

import java.util.concurrent.Callable;

/**
 * record class nine parameter's  parameterized FutureTask Thread
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/3/14 17:46
 */
public record NineFutureTask<A, B, C, D, E, F, G, H, I, V>(A A, B B, C C, D D, E E, F F, G G, H H, I I, Callable<A, B, C, D, E, F, G, H, I, V> callable) implements Callable<V> {

	@Override
	public V call() throws Exception {
		return callable.call(A, B, C, D, E, F, G, H, I);
	}

	/**
	 * FutureTaskThread defines the start method for starting a thread.
	 */
	public interface Callable<A, B, C, D, E, F, G, H, I, V> {
		V call(A A, B B, C C, D D, E E, F F, G G, H H, I I) throws Exception;
	}

}