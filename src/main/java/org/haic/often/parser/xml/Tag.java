package org.haic.often.parser.xml;

import org.haic.often.annotations.NotNull;

import java.util.Map;

/**
 * XML标签
 *
 * @author haicdust
 * @version 1.0
 * @since 2023/1/22 17:02
 */
public class Tag {

	private final String name;
	private final TagAttrs attrs;
	private boolean close;

	public Tag(@NotNull String tag) {
		this.close = tag.charAt(tag.length() - 2) == '/';
		int index = tag.indexOf(" ");
		this.name = (index == -1 ? tag.substring(1, tag.length() - (close ? 2 : 1)) : tag.substring(1, tag.indexOf(" "))).strip().toLowerCase();
		this.attrs = new TagAttrs(tag);
	}

	public Tag(@NotNull String name, @NotNull TagAttrs attrs, boolean close) {
		this.name = name;
		this.attrs = attrs;
		this.close = close;
	}

	public String name() {
		return name;
	}

	public TagAttrs attrs() {
		return attrs;
	}

	public Tag attrs(@NotNull Map<String, String> attrs) {
		this.attrs.putAll(attrs);
		return this;
	}

	public Tag attr(@NotNull String key, @NotNull String value) {
		this.attrs.put(key, value);
		return this;
	}

	public Tag removeAttr(@NotNull String key) {
		this.attrs.remove(key);
		return this;
	}

	public boolean containsKey(@NotNull String key) {
		return attrs.containsKey(key);
	}

	public String attr(@NotNull String key) {
		return attrs.get(key);
	}

	public boolean isClose() {
		return close;
	}

	public Tag isClose(boolean close) {
		this.close = close;
		return this;
	}

	@Override
	public String toString() {
		return "<" + name + attrs + (close ? "/>" : ">");
	}

}
