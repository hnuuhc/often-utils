package org.haic.often.net.analyze.nodes;

import org.haic.often.net.analyze.parser.Tag;
import org.haic.often.net.analyze.select.Selector;

/**
 * Represents a {@link TextNode} as an {@link Element}, to enable text nodes to be selected with
 * the {@link Selector} {@code :matchText} syntax.
 */
public class PseudoTextElement extends Element {

	public PseudoTextElement(Tag tag, String baseUri, Attributes attributes) {
		super(tag, baseUri, attributes);
	}

	@Override
	void outerHtmlHead(Appendable accum, int depth, Document.OutputSettings out) {
	}

	@Override
	void outerHtmlTail(Appendable accum, int depth, Document.OutputSettings out) {
	}
}
