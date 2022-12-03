package org.haic.often.net.parser.xml;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * @author haicdust
 * @version 1.0
 * @since 2022/11/30 9:36
 */
public class Elements extends ArrayList<Element> {

	public Elements() {
		super();
	}

	public Elements(Elements el) {
		super(el);
	}

	/**
	 * 获取第一个标签
	 *
	 * @return 第一个标签
	 */
	public Element first() {
		return super.get(0);
	}

	/**
	 * 获取当前标签下所有文本,以空格分割(警告: 文本本身也可能存在空格)
	 * <p>
	 * 默认排除文本类标签 例: script,textarea,style
	 *
	 * @return 所有文本内容
	 */
	public String text() {
		StringBuilder text = new StringBuilder();
		for (var child : this) {
			switch (child.name()) {
				case "script", "textarea", "style" -> {} // 特殊文本标签
				default -> {
					String childText = child.text();
					if (!childText.isEmpty()) {
						text.append(" ").append(childText);
					}
				}
			}
		}
		return text.toString().strip();
	}

	/**
	 * 按照指定规则查询标签第一个
	 *
	 * @param cssQuery 查询规则
	 * @return 查询结果
	 */
	public Element selectFirst(String cssQuery) {
		Elements result = select(cssQuery);
		return result.isEmpty() ? null : result.get(0);
	}

	/**
	 * 按照指定规则查询标签
	 *
	 * @param cssQuery 查询规则
	 * @return 查询结果
	 */
	public Elements select(String cssQuery) {
		var result = new Elements(this);
		var querys = cssQuery.split(" ");
		for (int i = 0; i < querys.length; i++) {
			if (querys[i].charAt(0) == '.') {
				var value = querys[i].substring(1);
				result = selectByAttr("class", value);
			} else if (querys[i].charAt(0) == '#') {
				var value = querys[i].substring(1);
				result = selectByAttr("id", value);
			} else {
				int index = querys[i].indexOf("[");
				if (index == -1) {
					result = result.selectByName(querys[i]); // 查询标签名
				} else {
					var name = querys[i].substring(0, index);
					result = result.selectByName(name); // 查询标签名
					String attr = querys[i];
					if (!attr.endsWith("]")) { // 属性值中存在空格,重新拼接
						do { // 可能存在多个空格
							//noinspection StringConcatenationInLoop
							attr += " " + querys[++i];
						} while (!attr.endsWith("]"));
					}
					attr = attr.substring(index + 1, attr.length() - 1);
					int indexAttr = attr.indexOf("=");
					if (indexAttr == -1) { // 不存在等号
						result = result.selectByNameAndAttrKey(name, attr); // 查询标签名
					} else {
						var key = attr.substring(0, indexAttr);
						var value = attr.charAt(attr.length() - 1) == '\'' ? attr.substring(indexAttr + 2, attr.length() - 1) : attr.substring(indexAttr + 1);
						result = result.selectByNameAndAttr(name, key, value); // 查询标签名
					}
				}
			}
		}
		return result;
	}

	/**
	 * 按照ID查询标签
	 *
	 * @param id id值
	 * @return 查询结果
	 */
	public Elements selectById(@NotNull String id) {
		Elements result = new Elements();
		this.forEach(child -> result.addAll(child.selectById(id)));
		return result;
	}

	/**
	 * 按照标签名称查询标签
	 *
	 * @param name 标签名称
	 * @return 查询结果
	 */
	public Elements selectByName(@NotNull String name) {
		Elements result = new Elements();
		this.forEach(child -> result.addAll(child.selectByName(name)));
		return result;
	}

	/**
	 * 按照标签名称和属性名查询标签
	 *
	 * @param name 标签名称
	 * @param key  属性名
	 * @return 查询结果
	 */
	public Elements selectByNameAndAttrKey(@NotNull String name, @NotNull String key) {
		Elements result = new Elements();
		this.forEach(child -> result.addAll(child.selectByNameAndAttrKey(name, key)));
		return result;
	}

	/**
	 * 按照标签名称和属性值查询标签
	 *
	 * @param name  标签名称
	 * @param key   属性名
	 * @param value 属性值
	 * @return 查询结果
	 */
	public Elements selectByNameAndAttr(@NotNull String name, @NotNull String key, @NotNull String value) {
		Elements result = new Elements();
		this.forEach(child -> result.addAll(child.selectByNameAndAttr(name, key, value)));
		return result;
	}

	/**
	 * 按照属性名称查询标签
	 *
	 * @param key 属性名称
	 * @return 查询结果
	 */
	public Elements selectByAttr(@NotNull String key) {
		Elements result = new Elements();
		this.forEach(child -> result.addAll(child.selectByAttr(key)));
		return result;
	}

	/**
	 * 按照属性名称和属性值查询标签
	 *
	 * @param key   属性名称
	 * @param value 属性值
	 * @return 查询结果
	 */
	public Elements selectByAttr(@NotNull String key, @NotNull String value) {
		Elements result = new Elements();
		this.forEach(child -> result.addAll(child.selectByAttr(key, value)));
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Element child : this) {
			sb.append(child.toString(0)).append("\n");
		}
		return sb.toString();
	}

}
