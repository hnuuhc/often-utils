package org.haic.often.net.analyze.select;

import org.haic.often.net.analyze.nodes.Element;
import org.haic.often.net.analyze.helper.Validate;
import org.haic.often.net.analyze.nodes.Node;

/**
 * Depth-first node traversor. Use to iterate through all nodes under and including the specified root node.
 * <p>
 * This implementation does not use recursion, so a deep DOM does not risk blowing the stack.
 * </p>
 */
public class NodeTraversor {
	/**
	 * Start a depth-first traverse of the root and all of its descendants.
	 *
	 * @param visitor Node visitor.
	 * @param root    the root node point to traverse.
	 */
	public static void traverse(NodeVisitor visitor, Node root) {
		Validate.notNull(visitor);
		Validate.notNull(root);
		Node node = root;
		int depth = 0;

		while (node != null) {
			Node parent = node.parentNode(); // remember parent to find nodes that get replaced in .head
			int origSize = parent != null ? parent.childNodeSize() : 0;
			Node next = node.nextSibling();

			visitor.head(node, depth); // visit current node
			if (parent != null && !node.hasParent()) { // removed or replaced
				if (origSize == parent.childNodeSize()) { // replaced
					node = parent.childNode(node.siblingIndex()); // replace ditches parent but keeps sibling index
				} else { // removed
					node = next;
					if (node == null) { // last one, go up
						node = parent;
						depth--;
					}
					continue; // don't tail removed
				}
			}

			if (node.childNodeSize() > 0) { // descend
				node = node.childNode(0);
				depth++;
			} else {
				while (true) {
					assert node != null; // as depth > 0, will have parent
					if (!(node.nextSibling() == null && depth > 0)) break;
					visitor.tail(node, depth); // when no more siblings, ascend
					node = node.parentNode();
					depth--;
				}
				visitor.tail(node, depth);
				if (node == root) break;
				node = node.nextSibling();
			}
		}
	}

	/**
	 * Start a depth-first traverse of all elements.
	 *
	 * @param visitor  Node visitor.
	 * @param elements Elements to filter.
	 */
	public static void traverse(NodeVisitor visitor, Elements elements) {
		Validate.notNull(visitor);
		Validate.notNull(elements);
		for (Element el : elements)
			traverse(visitor, el);
	}

	/**
	 * Start a depth-first filtering of the root and all of its descendants.
	 *
	 * @param filter Node visitor.
	 * @param root   the root node point to traverse.
	 * @return The filter result of the root node, or {@link NodeFilter.FilterResult#STOP}.
	 */
	public static NodeFilter.FilterResult filter(NodeFilter filter, Node root) {
		Node node = root;
		int depth = 0;

		while (node != null) {
			NodeFilter.FilterResult result = filter.head(node, depth);
			if (result == NodeFilter.FilterResult.STOP) return result;
			// Descend into child nodes:
			if (result == NodeFilter.FilterResult.CONTINUE && node.childNodeSize() > 0) {
				node = node.childNode(0);
				++depth;
				continue;
			}
			// No siblings, move upwards:
			while (true) {
				assert node != null; // depth > 0, so has parent
				if (!(node.nextSibling() == null && depth > 0)) break;
				// 'tail' current node:
				if (result == NodeFilter.FilterResult.CONTINUE || result == NodeFilter.FilterResult.SKIP_CHILDREN) {
					result = filter.tail(node, depth);
					if (result == NodeFilter.FilterResult.STOP) return result;
				}
				Node prev = node; // In case we need to remove it below.
				node = node.parentNode();
				depth--;
				if (result == NodeFilter.FilterResult.REMOVE) prev.remove(); // Remove AFTER finding parent.
				result = NodeFilter.FilterResult.CONTINUE; // Parent was not pruned.
			}
			// 'tail' current node, then proceed with siblings:
			if (result == NodeFilter.FilterResult.CONTINUE || result == NodeFilter.FilterResult.SKIP_CHILDREN) {
				result = filter.tail(node, depth);
				if (result == NodeFilter.FilterResult.STOP) return result;
			}
			if (node == root) return result;
			Node prev = node; // In case we need to remove it below.
			node = node.nextSibling();
			if (result == NodeFilter.FilterResult.REMOVE) prev.remove(); // Remove AFTER finding sibling.
		}
		// root == null?
		return NodeFilter.FilterResult.CONTINUE;
	}

	/**
	 * Start a depth-first filtering of all elements.
	 *
	 * @param filter   Node filter.
	 * @param elements Elements to filter.
	 */
	public static void filter(NodeFilter filter, Elements elements) {
		Validate.notNull(filter);
		Validate.notNull(elements);
		for (Element el : elements)
			if (filter(filter, el) == NodeFilter.FilterResult.STOP) break;
	}
}
