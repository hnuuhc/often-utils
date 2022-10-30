package org.haic.often.tuple;

/**
 * @author haicdust
 * @version 1.0
 * @since 2022/1/19 0:22
 */
public class EightTuple<A, B, C, D, E, F, G, H> extends SevenTuple<A, B, C, D, E, F, G> {

	public final H eighth;

	public EightTuple(A a, B b, C c, D d, E e, F f, G g, H h) {
		super(a, b, c, d, e, f, g);
		eighth = h;
	}

}