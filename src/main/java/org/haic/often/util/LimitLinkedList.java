package org.haic.often.util;

import java.io.Serial;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author haicdust
 * @version 1.0
 * @since 2022/10/27 14:08
 */
public class LimitLinkedList<E> extends LinkedList<E> {

	@Serial private static final long serialVersionUID = -3407809689385613161L;

	protected int maxCapacity;

	/**
	 * 调用构造实例对象
	 */
	public LimitLinkedList() {
		this(Integer.MAX_VALUE - 8);
	}

	/**
	 * 调用构造实例对象
	 *
	 * @param maxCapacity 最大容量
	 */
	public LimitLinkedList(int maxCapacity) {
		super();
		this.maxCapacity = maxCapacity;
	}

	/**
	 * 修改链表长度
	 *
	 * @param maxCapacity 链表长度
	 */
	public void maxCapacity(int maxCapacity) {
		this.maxCapacity = maxCapacity;
	}

	@Override
	public void add(int index, E element) {
		super.add(index, element);
		if (size() > maxCapacity) {
			super.removeFirst();
		}

	}

	@Override
	public boolean add(E t) {
		if (size() + 1 > maxCapacity) {
			super.removeFirst();
		}
		return super.add(t);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean modified = super.addAll(c);
		if (size() > maxCapacity) {
			super.subList(0, size() - maxCapacity);
		}
		return modified;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		boolean modified = super.addAll(index, c);
		if (size() > maxCapacity) {
			super.subList(0, size() - maxCapacity);
		}
		return modified;
	}

}
