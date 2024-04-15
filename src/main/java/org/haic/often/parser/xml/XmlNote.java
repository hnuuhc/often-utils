package org.haic.often.parser.xml;

import org.jetbrains.annotations.NotNull;

public class XmlNote {

	private final String note;

	public XmlNote(@NotNull String note) {
		this.note = note;
	}

	public String toString() {
		return "<!-- " + note + " -->";
	}

}
