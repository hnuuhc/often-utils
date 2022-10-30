package org.haic.often.tuple;

public class ThreeTuple<A, B, C> extends TwoTuple<A, B> {

	public final C third;

	public ThreeTuple(A a, B b, C c) {
		super(a, b);
		this.third = c;
	}

}
