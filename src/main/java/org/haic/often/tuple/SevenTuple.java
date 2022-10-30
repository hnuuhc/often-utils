package org.haic.often.tuple;

/**
 * @author haicdust
 * @version 1.0
 * @since 2022/1/19 0:18
 */
public class SevenTuple<A, B, C, D, E, F, G> extends SixTuple<A, B, C, D, E, F> {

	public final G seventh;

	public SevenTuple(A a, B b, C c, D d, E e, F f, G g) {
		super(a, b, c, d, e, f);
		seventh = g;
	}

}