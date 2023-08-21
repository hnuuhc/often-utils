package org.haic.often.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * @author haicdust
 * @version 1.0
 * @since 2023/5/15 21:11
 */
public class SafetyLinkedHashMap<K, V> extends LinkedHashMap<K, V> {

	public SafetyLinkedHashMap() {
		super();
	}

	public SafetyLinkedHashMap(Map<? extends K, ? extends V> m) {
		super(m);
	}

	public synchronized V put(K key, V value) {
		return super.put(key, value);
	}

	public synchronized void putAll(Map<? extends K, ? extends V> m) {
		super.putAll(m);
	}

	public synchronized V get(Object key) {
		return super.get(key);
	}

	public synchronized V remove(Object key) {
		return super.remove(key);
	}

	public synchronized void clear() {
		super.clear();
	}

	public synchronized int size() {
		return super.size();
	}

	public synchronized boolean isEmpty() {
		return super.isEmpty();
	}

	public synchronized boolean containsKey(Object key) {
		return super.containsKey(key);
	}

	public synchronized boolean containsValue(Object value) {
		return super.containsValue(value);
	}

	public synchronized Set<K> keySet() {
		return super.keySet();
	}

	public synchronized Collection<V> values() {
		return super.values();
	}

	public synchronized Set<Map.Entry<K, V>> entrySet() {
		return super.entrySet();
	}

	public synchronized Object clone() {
		return super.clone();
	}

	public synchronized V replace(K key, V value) {
		return super.replace(key, value);
	}

	public synchronized boolean replace(K key, V oldValue, V newValue) {
		return super.replace(key, oldValue, newValue);
	}

	public synchronized void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
		super.replaceAll(function);
	}

}
