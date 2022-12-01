package org.haic.often.net.analyze.select;

import org.haic.often.net.analyze.helper.Validate;
import org.haic.often.net.analyze.nodes.Element;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.IdentityHashMap;

/**
 * CSS-like element selector, that finds elements matching a query.
 *
 * @author Jonathan Hedley, jonathan@hedley.net
 * @see Element#select(String)
 */
public class Selector {
	// not instantiable
	private Selector() {}

	/**
	 * Find elements matching selector.
	 *
	 * @param query CSS selector
	 * @param root  root element to descend into
	 * @return matching elements, empty if none
	 * @throws SelectorParseException (unchecked) on an invalid CSS query.
	 */
	public static Elements select(String query, Element root) {
		Validate.notEmpty(query);
		return select(QueryParser.parse(query), root);
	}

	/**
	 * Find elements matching selector.
	 *
	 * @param evaluator CSS selector
	 * @param root      root element to descend into
	 * @return matching elements, empty if none
	 */
	public static Elements select(Evaluator evaluator, Element root) {
		Validate.notNull(evaluator);
		Validate.notNull(root);
		return Collector.collect(evaluator, root);
	}

	/**
	 * Find elements matching selector.
	 *
	 * @param query CSS selector
	 * @param roots root elements to descend into
	 * @return matching elements, empty if none
	 */
	public static Elements select(String query, Iterable<Element> roots) {
		Validate.notEmpty(query);
		Validate.notNull(roots);
		Evaluator evaluator = QueryParser.parse(query);
		Elements elements = new Elements();
		IdentityHashMap<Element, Boolean> seenElements = new IdentityHashMap<>();
		// dedupe elements by identity, not equality

		for (Element root : roots) {
			final Elements found = select(evaluator, root);
			for (Element el : found) {
				if (seenElements.put(el, Boolean.TRUE) == null) {
					elements.add(el);
				}
			}
		}
		return elements;
	}

	// exclude set. package open so that Elements can implement .not() selector.
	static Elements filterOut(Collection<Element> elements, Collection<Element> outs) {
		Elements output = new Elements();
		for (Element el : elements) {
			boolean found = false;
			for (Element out : outs) {
				if (el.equals(out)) {
					found = true;
					break;
				}
			}
			if (!found) output.add(el);
		}
		return output;
	}

	/**
	 * Find the first element that matches the query.
	 *
	 * @param cssQuery CSS selector
	 * @param root     root element to descend into
	 * @return the matching element, or <b>null</b> if none.
	 */
	public static @Nullable Element selectFirst(String cssQuery, Element root) {
		Validate.notEmpty(cssQuery);
		return Collector.findFirst(QueryParser.parse(cssQuery), root);
	}

	public static class SelectorParseException extends IllegalStateException {
		public SelectorParseException(String msg) {
			super(msg);
		}

		public SelectorParseException(String msg, Object... params) {
			super(String.format(msg, params));
		}
	}
}
