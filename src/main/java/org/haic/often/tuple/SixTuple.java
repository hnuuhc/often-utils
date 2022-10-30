package org.haic.often.tuple;

/**
 * @author haicdust
 * @version 1.0
 * @since 2022/1/19 0:16
 */
public class SixTuple<A, B, C, D, E, F> extends FiveTuple<A, B, C, D, E> {

	public final F sixth;

	public SixTuple(A a, B b, C c, D d, E e, F f) {
		super(a, b, c, d, e);
		sixth = f;
	}

}