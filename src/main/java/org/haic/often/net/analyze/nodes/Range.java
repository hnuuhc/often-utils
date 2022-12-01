package org.haic.often.net.analyze.nodes;

import org.haic.often.net.analyze.helper.Validate;
import org.haic.often.net.analyze.parser.Parser;

/**
 * A Range object tracks the character positions in the original input source where a Node starts or ends. If you want to
 * track these positions, tracking must be enabled in the Parser with
 * {@link Parser#setTrackPosition(boolean)}.
 *
 * @see Node#sourceRange()
 * @since 1.15.2
 */
public record Range(Position start, Position end) {
	private static final String RangeKey = Attributes.internalKey("jsoup.sourceRange");
	private static final String EndRangeKey = Attributes.internalKey("jsoup.endSourceRange");
	private static final Position UntrackedPos = new Position(-1, -1, -1);
	private static final Range Untracked = new Range(UntrackedPos, UntrackedPos);

	/**
	 * Creates a new Range with start and end Positions. Called by TreeBuilder when position tracking is on.
	 *
	 * @param start the start position
	 * @param end   the end position
	 */
	public Range {
	}

	/**
	 * Get the start position of this node.
	 *
	 * @return the start position
	 */
	@Override
	public Position start() {
		return start;
	}

	/**
	 * Get the end position of this node.
	 *
	 * @return the end position
	 */
	@Override
	public Position end() {
		return end;
	}

	/**
	 * Test if this source range was tracked during parsing.
	 *
	 * @return true if this was tracked during parsing, false otherwise (and all fields will be {@code -1}).
	 */
	public boolean isTracked() {
		return this != Untracked;
	}

	/**
	 * Retrieves the source range for a given Node.
	 *
	 * @param node  the node to retrieve the position for
	 * @param start if this is the starting range. {@code false} for Element end tags.
	 * @return the Range, or the Untracked (-1) position if tracking is disabled.
	 */
	static Range of(Node node, boolean start) {
		final String key = start ? RangeKey : EndRangeKey;
		if (!node.hasAttr(key)) return Untracked;
		else return (Range) Validate.ensureNotNull(node.attributes().getUserData(key));
	}

	/**
	 * Internal method, called by the TreeBuilder. Tracks a Range for a Node.
	 *
	 * @param node  the node to associate this position to
	 * @param start if this is the starting range. {@code false} for Element end tags.
	 */
	public void track(Node node, boolean start) {
		node.attributes().putUserData(start ? RangeKey : EndRangeKey, this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Range range = (Range) o;

		if (!start.equals(range.start)) return false;
		return end.equals(range.end);
	}

	/**
	 * Gets a String presentation of this Range, in the format {@code line,column:pos-line,column:pos}.
	 *
	 * @return a String
	 */
	@Override
	public String toString() {
		return start + "-" + end;
	}

	/**
	 * A Position object tracks the character position in the original input source where a Node starts or ends. If you want to
	 * track these positions, tracking must be enabled in the Parser with
	 * {@link Parser#setTrackPosition(boolean)}.
	 *
	 * @see Node#sourceRange()
	 */
	public record Position(int pos, int lineNumber, int columnNumber) {
		/**
		 * Create a new Position object. Called by the TreeBuilder if source position tracking is on.
		 *
		 * @param pos          position index
		 * @param lineNumber   line number
		 * @param columnNumber column number
		 */
		public Position {
		}

		/**
		 * Gets the position index (0-based) of the original input source that this Position was read at. This tracks the
		 * total number of characters read into the source at this position, regardless of the number of preceeding lines.
		 *
		 * @return the position, or {@code -1} if untracked.
		 */
		@Override
		public int pos() {
			return pos;
		}

		/**
		 * Gets the line number (1-based) of the original input source that this Position was read at.
		 *
		 * @return the line number, or {@code -1} if untracked.
		 */
		@Override
		public int lineNumber() {
			return lineNumber;
		}

		/**
		 * Gets the cursor number (1-based) of the original input source that this Position was read at. The cursor number
		 * resets to 1 on every new line.
		 *
		 * @return the cursor number, or {@code -1} if untracked.
		 */
		@Override
		public int columnNumber() {
			return columnNumber;
		}

		/**
		 * Test if this position was tracked during parsing.
		 *
		 * @return true if this was tracked during parsing, false otherwise (and all fields will be {@code -1}).
		 */
		public boolean isTracked() {
			return this != UntrackedPos;
		}

		/**
		 * Gets a String presentation of this Position, in the format {@code line,column:pos}.
		 *
		 * @return a String
		 */
		@Override
		public String toString() {
			return lineNumber + "," + columnNumber + ":" + pos;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Position position = (Position) o;
			if (pos != position.pos) return false;
			if (lineNumber != position.lineNumber) return false;
			return columnNumber == position.columnNumber;
		}

	}
}
