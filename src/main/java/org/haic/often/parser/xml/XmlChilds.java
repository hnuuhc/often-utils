package org.haic.often.parser.xml;

import java.util.ArrayList;

/**
 * XML子属性数组
 *
 * @author haicdust
 * @version 1.0
 * @since 2023/1/22 17:36
 */
public class XmlChilds extends ArrayList<Object> {

	@Override
	public String toString() {
		return toString(0);
	}

	public String toString(int depth) {
		StringBuilder sb = new StringBuilder();
		for (var child : this) sb.append("\n").append(child instanceof XmlTree e ? e.toString(depth) : "    ".repeat(depth) + child);
		return sb.toString();
	}

}
