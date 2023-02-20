package org.haic.often.parser.xml;

import org.haic.often.annotations.NotNull;
import org.haic.often.parser.ParserStringBuilder;

import java.util.HashMap;
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
			if (c == ' ') continue;
			if (c == '<') return;
			if (node.charAt() == '/' && node.charAt(node.pos() + 1) == '>') return;
			var key = new StringBuilder();
			for (var ck = node.charAt(); ck != '='; ck = node.offset(1).charAt()) {
				if (node.charAt() == ' ') {
					this.put(key.toString(), null);
					continue node;
				}
				key.append(ck);
			}
			switch (node.offset(1).charAt()) {
				case '"', '\'' -> this.put(key.toString(), node.intercept());
				case '&' -> {
					if (node.startsWith("&quot;")) {
						int index = node.offset(6).indexOf("&quot;");
						this.put(key.toString(), node.offset(6).substring(node.pos(), index));
						node.offset(5);
					} else {
						throw new IllegalArgumentException("在索引 " + node.pos() + " 处存在未知意义符号");
					}
				}
			}
		}
	}

	@Override
	public String toString() {
		return this.entrySet().stream().map(l -> " " + (l.getValue() == null ? l.getKey() : l.getKey() + "=\"" + l.getValue() + '"')).collect(Collectors.joining());
	}

}
