package org.haic.often.list;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;

/**
 * Thread safe lru cache cache based on LinkedHashMap
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/10/27 0:33
 */
public class SafetyLinkedHashMap<K, V> extends LinkedHashMap<K, V> {

	@Serial private static final long serialVersionUID = -4407809689385629881L;

	private final Lock lock = new ReentrantLock();

	private int maxCapacity;

	/**
	 * 调用构造实例对象
	 */
	public SafetyLinkedHashMap() {
		this(1 << 30);
	}

	/**
	 * 调用构造实例对象
	 *
	 * @param maxCapacity 最大容量
	 */
	public SafetyLinkedHashMap(int maxCapacity) {
		super();
		maxCapacity(maxCapacity);
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
	public V get(Object key) {
		lock.lock();
		try {
			return super.get(key);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public V put(K key, V value) {
		lock.lock();
		try {
			return super.put(key, value);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		lock.lock();
		try {
			super.putAll(m);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public V remove(Object key) {
		lock.lock();
		try {
			return super.remove(key);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public V replace(K key, V value) {
		lock.lock();
		try {
			return super.replace(key, value);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		lock.lock();
		try {
			return super.replace(key, oldValue, newValue);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		lock.lock();
		try {
			super.replaceAll(function);
		} finally {
			lock.unlock();
		}
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > maxCapacity;
	}

}
