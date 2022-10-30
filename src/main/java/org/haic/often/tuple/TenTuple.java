package org.haic.often.tuple;

/**
 * @author haicdust
 * @version 1.0
 * @since 2022/1/19 0:30
 */
public class TenTuple<A, B, C, D, E, F, G, H, I, J> extends NineTuple<A, B, C, D, E, F, G, H, I> {

	public final J tenth;

	public TenTuple(A a, B b, C c, D d, E e, F f, G g, H h, I i, J j) {
		super(a, b, c, d, e, f, g, h, i);
		tenth = j;
	}

}