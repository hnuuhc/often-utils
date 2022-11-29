package org.haic.often.net.analyze.nodes;

import org.haic.often.net.analyze.parser.Tag;
import org.haic.often.net.analyze.select.Elements;

/**
 * A HTML Form Element provides ready access to the form fields/controls that are associated with it. It also allows a
 * form to easily be submitted.
 */
public class FormElement extends Element {
	private final Elements elements = new Elements();

	/**
	 * Create a new, standalone form element.
	 *
	 * @param tag        tag of this element
	 * @param baseUri    the base URI
	 * @param attributes initial attributes
	 */
	public FormElement(Tag tag, String baseUri, Attributes attributes) {
		super(tag, baseUri, attributes);
	}

	/**
	 * Get the list of form control elements associated with this form.
	 *
	 * @return form controls associated with this element.
	 */
	public Elements elements() {
		return elements;
	}

	/**
	 * Add a form control element to this form.
	 *
	 * @param element form control to add
	 * @return this form element, for chaining
	 */
	public FormElement addElement(Element element) {
		elements.add(element);
		return this;
	}

	@Override
	protected void removeChild(Node out) {
		super.removeChild(out);
		//noinspection SuspiciousMethodCalls
		elements.remove(out);
	}

	@Override
	public FormElement clone() {
		return (FormElement) super.clone();
	}
}
