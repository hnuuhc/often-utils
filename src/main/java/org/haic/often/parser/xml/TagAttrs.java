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

	public TagAttrs(@NotNull String tag) {
		var body = new ParserStringBuilder(tag);
		int index = body.indexOf(" ");
		if (index == -1) return;
		do {
			int end = body.indexOf("=", ++index);
			if (end == -1) break;
			var key = body.substring(index, index = end).strip();
			int keyIndex = key.indexOf(" ");
			if (keyIndex != -1) {
				this.put(key.substring(0, keyIndex), null);
				key = key.substring(keyIndex + 1);
			}
			String value;
			if (body.charAt(++index) == '"') {
				value = body.substring(++index, index = body.indexOf("\"", index));
				this.put(key, value);
			} else if (body.charAt(index) == '\'') {
				value = body.substring(++index, index = body.indexOf("'", index));
				this.put(key, value);
			} else if (body.charAt(index) == '&') {
				value = body.substring(index = body.indexOf(";", index + 1) + 1, index = body.indexOf("&", index + 1));
				index = body.indexOf(";", index + 1);
				this.put(key, value);
			} else {
				int thisEnd = body.indexOf(" ", index + 1);
				value = body.substring(index, index = thisEnd == -1 ? body.charAt(body.length() - 2) == '/' ? body.length() - 2 : body.length() - 1 : thisEnd).strip();
			}
			this.put(key, Document.unescape(value));
		} while (++index < body.length() && body.charAt(index) != '/' && body.charAt(index) != '>');
	}

	@Override
	public String toString() {
		return this.entrySet().stream().map(l -> " " + (l.getValue() == null ? l.getKey() : l.getKey() + "=\"" + l.getValue() + '"')).collect(Collectors.joining());
	}

}
