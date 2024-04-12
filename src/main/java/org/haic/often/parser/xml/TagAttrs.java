package org.haic.often.parser.xml;

import org.haic.often.annotations.NotNull;
import org.haic.often.parser.ParserStringBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * XML标签属性
 *
 * @author haicdust
 * @version 1.0
 * @since 2023/1/22 17:36
 */
public class TagAttrs extends HashMap<String, String> {

	public TagAttrs() {super();}

	protected TagAttrs(@NotNull ParserStringBuilder node) {
		node:
		for (var c = node.offset(1).stripLeading().charAt(); c != '>'; c = node.offset(1).stripLeading().charAt()) {
			if (c == '<') return;
			if (node.charAt() == '/' && node.charAt(node.site() + 1) == '>') return;
			var key = new StringBuilder();
			for (var ck = node.charAt(); ck != '='; ck = node.offset(1).charAt()) {
				if (node.charAt() == ' ' || node.charAt() == '/') {
					this.put(key.toString(), null);
					continue node;
				} else if (node.charAt() == '>') { // 最后一个属性退出循环,防止指针加一
					this.put(key.toString(), null);
					break node;
				}
				key.append(ck);
			}
			switch (node.offset(1).stripLeading().charAt()) {
				case '"', '\'' -> this.put(key.toString(), node.interceptNoEscape());
				case '&' -> {
					if (node.startsWith("&quot;")) {
						int index = node.offset(6).indexOf("&quot;");
						this.put(key.toString(), node.substring(node.site(), index));
						node.offset(6);
					} else if (node.startsWith("&#34;")) {
						int index = node.offset(5).indexOf("&#34;");
						this.put(key.toString(), node.substring(node.site(), index));
						node.offset(5);
					} else {
						throw new IllegalArgumentException("在索引 " + node.site() + " 处存在未知意义符号");
					}
				}
				default -> {
					var value = node.intercept(List.of(' ', '/', '>'));
					node.offset(-1); // 循环后会自动加一会导致越界
					this.put(key.toString(), value);
				}
			}
		}
	}

	@Override
	public String toString() {
		return this.entrySet().stream().map(l -> " " + (l.getValue() == null ? l.getKey() : l.getKey() + "=\"" + l.getValue() + '"')).collect(Collectors.joining());
	}

}
