package org.haic.often.parser.xml;

import org.haic.often.parser.json.JSONArray;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * XML子属性数组
 *
 * @author haicdust
 * @version 1.0
 * @since 2023/1/22 17:36
 */
public class XmlChilds extends ArrayList<Object> {

	/**
	 * 将树状结构转换为 {@link JSONArray} 类型
	 *
	 * @return JSON数据
	 */
	public JSONArray toJSONArray() {
		return this.stream().map(l -> l instanceof XmlTree e ? e.toJSONObject() : l).collect(Collectors.toCollection(JSONArray::new));
	}

	@Override
	public String toString() {
		return toString(0);
	}

	public String toString(int depth) {
		var sb = new StringBuilder();
		for (var child : this) sb.append("\n").append(child instanceof XmlTree e ? e.toString(depth) : "    ".repeat(depth) + child);
		return sb.toString();
	}

}
