package org.haic.often.list;

import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * @author haicdust
 * @version 1.0
 * @since 2022/10/27 13:00
 */
public class SafetyLimitLinkedList<E> extends LimitLinkedList<E> {

	@Serial private static final long serialVersionUID = -4407809689385629761L;

	private final Lock lock = new ReentrantLock();

	/**
	 * 调用构造实例对象
	 */
	public SafetyLimitLinkedList() {
		super();
	}

	/**
	 * 调用构造实例对象
	 *
	 * @param maxCapacity 最大容量
	 */
	public SafetyLimitLinkedList(int maxCapacity) {
		super(maxCapacity);
	}

	@Override
	public void add(int index, E element) {
		lock.lock();
		try {
			super.add(index, element);
			if (size() > maxCapacity) {
				super.removeFirst();
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean add(E t) {
		lock.lock();
		try {
			if (size() + 1 > maxCapacity) {
				super.removeFirst();
			}
			return super.add(t);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		lock.lock();
		try {
			boolean modified = super.addAll(c);
			if (size() > maxCapacity) {
				super.subList(0, size() - maxCapacity);
			}
			return modified;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		lock.lock();
		try {
			boolean modified = super.addAll(index, c);
			if (size() > maxCapacity) {
				super.subList(0, size() - maxCapacity);
			}
			return modified;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		lock.lock();
		try {
			return super.subList(fromIndex, toIndex);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void addFirst(E e) {
		lock.lock();
		try {
			super.addFirst(e);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void addLast(E e) {
		lock.lock();
		try {
			super.addLast(e);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public E removeFirst() {
		lock.lock();
		try {
			return super.removeFirst();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean removeFirstOccurrence(Object o) {
		lock.lock();
		try {
			return super.removeFirstOccurrence(o);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public E removeLast() {
		lock.lock();
		try {
			return super.removeLast();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean removeLastOccurrence(Object o) {
		lock.lock();
		try {
			return super.removeLastOccurrence(o);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public E remove() {
		lock.lock();
		try {
			return super.remove();
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
	public E remove(int index) {
		lock.lock();
		try {
			return super.remove(index);
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
	public boolean removeIf(Predicate<? super E> filter) {
		lock.lock();
		try {
			return super.removeIf(filter);
		} finally {
			lock.unlock();
		}
	}

	@Override
	protected void removeRange(int fromIndex, int toIndex) {
		lock.lock();
		try {
			super.removeRange(fromIndex, toIndex);
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

	@Override
	public void replaceAll(UnaryOperator<E> operator) {
		lock.lock();
		try {
			super.replaceAll(operator);
		} finally {
			lock.unlock();
		}
	}

}
