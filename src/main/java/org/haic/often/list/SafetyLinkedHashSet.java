package org.haic.often.list;

import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread safe lru cache cache based on LinkedHashSet
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/10/27 10:58
 */

public class SafetyLinkedHashSet<E> extends LinkedHashSet<E> {

	@Serial private static final long serialVersionUID = -4851667679971038691L;

	private final Lock lock = new ReentrantLock();

	private int maxCapacity;

	/**
	 * 调用构造实例对象
	 */
	public SafetyLinkedHashSet() {
		this(1 << 30);
	}

	/**
	 * 调用构造实例对象
	 *
	 * @param maxCapacity 最大容量
	 */
	public SafetyLinkedHashSet(int maxCapacity) {
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
	public boolean add(E e) {
		lock.lock();
		try {
			if (size() + 1 > maxCapacity) {
				super.remove(super.iterator().next());
			}
			return super.add(e);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean addAll(@NotNull Collection<? extends E> c) {
		lock.lock();
		try {
			boolean modified = super.addAll(c);
			if (size() > maxCapacity) {
				Iterator<E> iterator = super.iterator();
				int count = size() - maxCapacity;
				for (int i = 0; i < count; i++) {
					super.remove(iterator.next());
				}
			}
			return modified;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean remove(Object o) {
		lock.lock();
		try {
			return super.remove(o);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean removeAll(@NotNull Collection<?> c) {
		lock.lock();
		try {
			return super.removeAll(c);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean retainAll(@NotNull Collection<?> c) {
		lock.lock();
		try {
			return super.retainAll(c);
		} finally {
			lock.unlock();
		}
	}

}
