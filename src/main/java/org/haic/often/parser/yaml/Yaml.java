package org.haic.often.parser.yaml;

import org.haic.often.exception.YAMLException;
import org.haic.often.parser.ParserStringBuilder;
import org.jetbrains.annotations.NotNull;

public class Yaml {

	public static int indentation(@NotNull ParserStringBuilder body) {
		var nextdepth = 0;
		findnextdepth:
		for (var i = body.site(); i < body.length(); i++, nextdepth++) {
			var c = body.charAt(i);
			switch (c) {
				case '\t' -> throw new YAMLException("禁止使用TAB缩进");
				case '\n' -> {
					body.site(i + 1);
					nextdepth = -1;
				}
				case '#' -> {
					i = body.indexOf("\n");
					body.site(i + 1);
					nextdepth = -1;
				}
				case ' ' -> {}
				default -> {break findnextdepth;}
			}
		}
		return nextdepth;
	}

	public static String deserialization(@NotNull String str) {
		switch (str.charAt(0)) {
			case '\'' -> {
				var end = str.length() - 1;
				if (str.charAt(end) == '\'') return str.substring(1, end);
				throw new YAMLException("未找到封闭字符'''");
			}
			case '"' -> {
				if (str.charAt(str.length() - 1) == '"') return new ParserStringBuilder(str).intercept();
				throw new YAMLException("未找到封闭字符'\"'");
			}
		}
		return str;
	}

}
