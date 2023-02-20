package org.haic.often.parser.xml;

import org.haic.often.annotations.NotNull;
import org.haic.often.parser.ParserStringBuilder;

import java.util.Map;

/**
 * XML标签
 *
 * @author haicdust
 * @version 1.0
 * @since 2023/1/22 17:02
 */
public class Tag {

	private String name;
	private TagAttrs attrs;
	private boolean isClose;

	protected Tag(@NotNull ParserStringBuilder node) {
		if (node.offset(1).charAt() == '/' || node.charAt() == '!') return; // 结束标签和注释
		var name = new StringBuilder();
		for (var c = node.charAt(); c != '>'; c = node.offset(1).charAt()) {
			if (c == '/' && node.charAt(node.pos() + 1) == '>') {
				var e = node.charAt(node.pos() + 1);
				if (e == '>' || (e == ' ' && node.stripLeading().charAt() == '>')) {
					this.isClose = true;
					this.attrs = new TagAttrs();
					node.offset(1);
					break;
				}
				throw new IllegalArgumentException("在索引 " + node.pos() + " 处存在未知意义 '/' 符号");
			}
			if (c == ' ') {
				this.attrs = new TagAttrs(node);
				if (node.charAt() == '/') {
					this.isClose = true;
					node.offset(1);
				}
				break;
			}
			if (c == '<') return;
			name.append(c);
		}
		this.name = name.toString().strip().toLowerCase();
		if (this.attrs == null) this.attrs = new TagAttrs();
	}

	/**
	 * 获取当前标签名称
	 *
	 * @return 标签名称
	 */
	public String name() {
		return name;
	}

	/**
	 * 获取当前标签的全部属性
	 *
	 * @return 全部标签属性
	 */
	public TagAttrs attrs() {
		return attrs;
	}

	/**
	 * 获取当前标签指定属性的值
	 *
	 * @param key 属性名称
	 * @return 属性值
	 */
	public String attr(@NotNull String key) {
		return attrs.get(key);
	}

	/**
	 * 添加属性
	 *
	 * @param key   属性名称
	 * @param value 属性值
	 * @return 当前标签
	 */
	public Tag attr(@NotNull String key, @NotNull String value) {
		this.attrs.put(key, value);
		return this;
	}

	/**
	 * 判断当前标签是否存在指定属性
	 *
	 * @param key 属性名称
	 * @return 判断结果
	 */
	public boolean containsAttr(@NotNull String key) {
		return attrs.containsKey(key);
	}

	/**
	 * 如果此映射将一个或多个键映射到指定值，则返回 true
	 *
	 * @param value 要测试此地图中是否存在的值
	 * @return 判断结果
	 */
	public boolean containsAttrOfValue(String value) {
		return attrs.containsValue(value);
	}

	/**
	 * 判断当前标签是否存在指定属性
	 *
	 * @param key   属性名称
	 * @param value 属性值
	 * @return 判断结果
	 */
	public boolean containsAttrValue(@NotNull String key, String value) {
		return attrs.containsKey(key) && attrs.get(key).equals(value);
	}

	/**
	 * 为当前标签添加所有属性
	 *
	 * @param attrs 属性数组
	 * @return 当前标签
	 */
	public Tag addAttrs(@NotNull Map<String, String> attrs) {
		this.attrs.putAll(attrs);
		return this;
	}

	/**
	 * 删除当前标签的指定属性
	 *
	 * @param key 属性名称
	 * @return 当前标签
	 */
	public Tag removeAttr(@NotNull String key) {
		this.attrs.remove(key);
		return this;
	}

	/**
	 * 设置当前节点是否为自闭合(警告: 如果设置为自闭合,在格式化时将不会输出子节点以及文本,查询不受影响)
	 *
	 * @param isClose true为是自闭合标签,反之同理
	 * @return 当前节点
	 */
	public Tag close(boolean isClose) {
		this.isClose = isClose;
		return this;
	}

	/**
	 * 判断当前表示是否为闭合标签
	 *
	 * @return 判断结果
	 */
	public boolean isClose() {
		return isClose;
	}

	@Override
	public String toString() {
		return "<" + name + attrs + (isClose ? "/>" : ">");
	}

}
