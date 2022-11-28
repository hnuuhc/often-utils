package org.haic.often.tuple;

import org.haic.often.tuple.record.*;

/**
 * 用于记录多个参数
 */
public class Tuple {

	public static <A, B> TwoTuple<A, B> of(A A, B b) {
		return new TwoTuple<>(A, b);
	}

	public static <A, B, C> ThreeTuple<A, B, C> of(A A, B B, C c) {
		return new ThreeTuple<>(A, B, c);
	}

	public static <A, B, C, D> FourTuple<A, B, C, D> of(A A, B B, C C, D d) {
		return new FourTuple<>(A, B, C, d);
	}

	public static <A, B, C, D, E> FiveTuple<A, B, C, D, E> of(A A, B B, C C, D D, E e) {
		return new FiveTuple<>(A, B, C, D, e);
	}

	public static <A, B, C, D, E, F> SixTuple<A, B, C, D, E, F> of(A A, B B, C C, D D, E E, F f) {
		return new SixTuple<>(A, B, C, D, E, f);
	}

	public static <A, B, C, D, E, F, G> SevenTuple<A, B, C, D, E, F, G> of(A A, B B, C C, D D, E E, F F, G g) {
		return new SevenTuple<>(A, B, C, D, E, F, g);
	}

	public static <A, B, C, D, E, F, G, H> EightTuple<A, B, C, D, E, F, G, H> of(A A, B B, C C, D D, E E, F F, G G, H h) {
		return new EightTuple<>(A, B, C, D, E, F, G, h);
	}

	public static <A, B, C, D, E, F, G, H, I> NineTuple<A, B, C, D, E, F, G, H, I> of(A A, B B, C C, D D, E E, F F, G G, H H, I i) {
		return new NineTuple<>(A, B, C, D, E, F, G, H, i);
	}

	public static <A, B, C, D, E, F, G, H, I, J> TenTuple<A, B, C, D, E, F, G, H, I, J> of(A A, B B, C C, D D, E E, F F, G G, H H, I I, J J) {
		return new TenTuple<>(A, B, C, D, E, F, G, H, I, J);
	}

}
