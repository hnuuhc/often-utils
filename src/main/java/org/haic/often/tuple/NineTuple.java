package org.haic.often.tuple;

/**
 * @author haicdust
 * @version 1.0
 * @since 2022/1/19 0:27
 */
public class NineTuple<A, B, C, D, E, F, G, H, I> extends EightTuple<A, B, C, D, E, F, G, H> {

	public final I ninth;

	public NineTuple(A a, B b, C c, D d, E e, F f, G g, H h, I i) {
		super(a, b, c, d, e, f, g, h);
		ninth = i;
	}

}