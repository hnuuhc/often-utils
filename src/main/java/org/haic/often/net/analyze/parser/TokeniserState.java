package org.haic.often.net.analyze.parser;

import org.haic.often.net.analyze.nodes.DocumentType;

/**
 * States and transition activations for the Tokeniser.
 */
enum TokeniserState {
	Data {
		// in data state, gather characters until a character reference or tag is found
		void read(Tokeniser t, CharacterReader r) {
			switch (r.current()) {
				case '&' -> t.advanceTransition(CharacterReferenceInData);
				case '<' -> t.advanceTransition(TagOpen);
				case nullChar -> {
					t.error(this); // NOT replacement character (oddly?)
					t.emit(r.consume());
				}
				case eof -> t.emit(new Token.EOF());
				default -> {
					String data = r.consumeData();
					t.emit(data);
				}
			}
		}
	},
	CharacterReferenceInData {
		// from & in data
		void read(Tokeniser t, CharacterReader r) {
			readCharRef(t, Data);
		}
	},
	Rcdata {
		/// handles data in title, textarea etc
		void read(Tokeniser t, CharacterReader r) {
			switch (r.current()) {
				case '&' -> t.advanceTransition(CharacterReferenceInRcdata);
				case '<' -> t.advanceTransition(RcdataLessthanSign);
				case nullChar -> {
					t.error(this);
					r.advance();
					t.emit(replacementChar);
				}
				case eof -> t.emit(new Token.EOF());
				default -> {
					String data = r.consumeData();
					t.emit(data);
				}
			}
		}
	},
	CharacterReferenceInRcdata {
		void read(Tokeniser t, CharacterReader r) {
			readCharRef(t, Rcdata);
		}
	},
	Rawtext {
		void read(Tokeniser t, CharacterReader r) {
			readRawData(t, r, this, RawtextLessthanSign);
		}
	},
	ScriptData {
		void read(Tokeniser t, CharacterReader r) {
			readRawData(t, r, this, ScriptDataLessthanSign);
		}
	},
	PLAINTEXT {
		void read(Tokeniser t, CharacterReader r) {
			switch (r.current()) {
				case nullChar -> {
					t.error(this);
					r.advance();
					t.emit(replacementChar);
				}
				case eof -> t.emit(new Token.EOF());
				default -> {
					String data = r.consumeTo(nullChar);
					t.emit(data);
				}
			}
		}
	},
	TagOpen {
		// from < in data
		void read(Tokeniser t, CharacterReader r) {
			switch (r.current()) {
				case '!':
					t.advanceTransition(MarkupDeclarationOpen);
					break;
				case '/':
					t.advanceTransition(EndTagOpen);
					break;
				case '?':
					t.createBogusCommentPending();
					t.transition(BogusComment);
					break;
				default:
					if (r.matchesAsciiAlpha()) {
						t.createTagPending(true);
						t.transition(TagName);
					} else {
						t.error(this);
						t.emit('<'); // char that got us here
						t.transition(Data);
					}
					break;
			}
		}
	},
	EndTagOpen {
		void read(Tokeniser t, CharacterReader r) {
			if (r.isEmpty()) {
				t.eofError(this);
				t.emit("</");
				t.transition(Data);
			} else if (r.matchesAsciiAlpha()) {
				t.createTagPending(false);
				t.transition(TagName);
			} else if (r.matches('>')) {
				t.error(this);
				t.advanceTransition(Data);
			} else {
				t.error(this);
				t.createBogusCommentPending();
				t.commentPending.append('/'); // push the / back on that got us here
				t.transition(BogusComment);
			}
		}
	},
	TagName {
		// from < or </ in data, will have start or end tag pending
		void read(Tokeniser t, CharacterReader r) {
			// previous TagOpen state did NOT consume, will have a letter char in current
			String tagName = r.consumeTagName();
			t.tagPending.appendTagName(tagName);

			char c = r.consume();
			switch (c) {
				case '\t':
				case '\n':
				case '\r':
				case '\f':
				case ' ':
					t.transition(BeforeAttributeName);
					break;
				case '/':
					t.transition(SelfClosingStartTag);
					break;
				case '<': // NOTE: out of spec, but clear author intent
					r.unconsume();
					t.error(this);
					// intended fall through to next >
				case '>':
					t.emitTagPending();
					t.transition(Data);
					break;
				case nullChar: // replacement
					t.tagPending.appendTagName(replacementStr);
					break;
				case eof: // should emit pending tag?
					t.eofError(this);
					t.transition(Data);
					break;
				default: // buffer underrun
					t.tagPending.appendTagName(c);
			}
		}
	},
	RcdataLessthanSign {
		// from < in rcdata
		void read(Tokeniser t, CharacterReader r) {
			if (r.matches('/')) {
				t.createTempBuffer();
				t.advanceTransition(RCDATAEndTagOpen);
			} else if (r.matchesAsciiAlpha() && t.appropriateEndTagName() != null && !r.containsIgnoreCase(t.appropriateEndTagSeq())) {
				// diverge from spec: got a start tag, but there's no appropriate end tag (</title>), so rather than
				// consuming to EOF; break out here
				t.tagPending = t.createTagPending(false).name(t.appropriateEndTagName());
				t.emitTagPending();
				t.transition(TagOpen); // straight into TagOpen, as we came from < and looks like we're on a start tag
			} else {
				t.emit("<");
				t.transition(Rcdata);
			}
		}
	},
	RCDATAEndTagOpen {
		void read(Tokeniser t, CharacterReader r) {
			if (r.matchesAsciiAlpha()) {
				t.createTagPending(false);
				t.tagPending.appendTagName(r.current());
				t.dataBuffer.append(r.current());
				t.advanceTransition(RCDATAEndTagName);
			} else {
				t.emit("</");
				t.transition(Rcdata);
			}
		}
	},
	RCDATAEndTagName {
		void read(Tokeniser t, CharacterReader r) {
			if (r.matchesAsciiAlpha()) {
				String name = r.consumeLetterSequence();
				t.tagPending.appendTagName(name);
				t.dataBuffer.append(name);
				return;
			}

			char c = r.consume();
			switch (c) {
				case '\t':
				case '\n':
				case '\r':
				case '\f':
				case ' ':
					if (t.isAppropriateEndTagToken()) t.transition(BeforeAttributeName);
					else anythingElse(t, r);
					break;
				case '/':
					if (t.isAppropriateEndTagToken()) t.transition(SelfClosingStartTag);
					else anythingElse(t, r);
					break;
				case '>':
					if (t.isAppropriateEndTagToken()) {
						t.emitTagPending();
						t.transition(Data);
					} else anythingElse(t, r);
					break;
				default:
					anythingElse(t, r);
			}
		}

		private void anythingElse(Tokeniser t, CharacterReader r) {
			t.emit("</");
			t.emit(t.dataBuffer);
			r.unconsume();
			t.transition(Rcdata);
		}
	},
	RawtextLessthanSign {
		void read(Tokeniser t, CharacterReader r) {
			if (r.matches('/')) {
				t.createTempBuffer();
				t.advanceTransition(RawtextEndTagOpen);
			} else {
				t.emit('<');
				t.transition(Rawtext);
			}
		}
	},
	RawtextEndTagOpen {
		void read(Tokeniser t, CharacterReader r) {
			readEndTag(t, r, RawtextEndTagName, Rawtext);
		}
	},
	RawtextEndTagName {
		void read(Tokeniser t, CharacterReader r) {
			handleDataEndTag(t, r, Rawtext);
		}
	},
	ScriptDataLessthanSign {
		void read(Tokeniser t, CharacterReader r) {
			switch (r.consume()) {
				case '/' -> {
					t.createTempBuffer();
					t.transition(ScriptDataEndTagOpen);
				}
				case '!' -> {
					t.emit("<!");
					t.transition(ScriptDataEscapeStart);
				}
				case eof -> {
					t.emit("<");
					t.eofError(this);
					t.transition(Data);
				}
				default -> {
					t.emit("<");
					r.unconsume();
					t.transition(ScriptData);
				}
			}
		}
	},
	ScriptDataEndTagOpen {
		void read(Tokeniser t, CharacterReader r) {
			readEndTag(t, r, ScriptDataEndTagName, ScriptData);
		}
	},
	ScriptDataEndTagName {
		void read(Tokeniser t, CharacterReader r) {
			handleDataEndTag(t, r, ScriptData);
		}
	},
	ScriptDataEscapeStart {
		void read(Tokeniser t, CharacterReader r) {
			if (r.matches('-')) {
				t.emit('-');
				t.advanceTransition(ScriptDataEscapeStartDash);
			} else {
				t.transition(ScriptData);
			}
		}
	},
	ScriptDataEscapeStartDash {
		void read(Tokeniser t, CharacterReader r) {
			if (r.matches('-')) {
				t.emit('-');
				t.advanceTransition(ScriptDataEscapedDashDash);
			} else {
				t.transition(ScriptData);
			}
		}
	},
	ScriptDataEscaped {
		void read(Tokeniser t, CharacterReader r) {
			if (r.isEmpty()) {
				t.eofError(this);
				t.transition(Data);
				return;
			}

			switch (r.current()) {
				case '-' -> {
					t.emit('-');
					t.advanceTransition(ScriptDataEscapedDash);
				}
				case '<' -> t.advanceTransition(ScriptDataEscapedLessthanSign);
				case nullChar -> {
					t.error(this);
					r.advance();
					t.emit(replacementChar);
				}
				default -> {
					String data = r.consumeToAny('-', '<', nullChar);
					t.emit(data);
				}
			}
		}
	},
	ScriptDataEscapedDash {
		void read(Tokeniser t, CharacterReader r) {
			if (r.isEmpty()) {
				t.eofError(this);
				t.transition(Data);
				return;
			}

			char c = r.consume();
			switch (c) {
				case '-' -> {
					t.emit(c);
					t.transition(ScriptDataEscapedDashDash);
				}
				case '<' -> t.transition(ScriptDataEscapedLessthanSign);
				case nullChar -> {
					t.error(this);
					t.emit(replacementChar);
					t.transition(ScriptDataEscaped);
				}
				default -> {
					t.emit(c);
					t.transition(ScriptDataEscaped);
				}
			}
		}
	},
	ScriptDataEscapedDashDash {
		void read(Tokeniser t, CharacterReader r) {
			if (r.isEmpty()) {
				t.eofError(this);
				t.transition(Data);
				return;
			}

			char c = r.consume();
			switch (c) {
				case '-' -> t.emit(c);
				case '<' -> t.transition(ScriptDataEscapedLessthanSign);
				case '>' -> {
					t.emit(c);
					t.transition(ScriptData);
				}
				case nullChar -> {
					t.error(this);
					t.emit(replacementChar);
					t.transition(ScriptDataEscaped);
				}
				default -> {
					t.emit(c);
					t.transition(ScriptDataEscaped);
				}
			}
		}
	},
	ScriptDataEscapedLessthanSign {
		void read(Tokeniser t, CharacterReader r) {
			if (r.matchesAsciiAlpha()) {
				t.createTempBuffer();
				t.dataBuffer.append(r.current());
				t.emit("<");
				t.emit(r.current());
				t.advanceTransition(ScriptDataDoubleEscapeStart);
			} else if (r.matches('/')) {
				t.createTempBuffer();
				t.advanceTransition(ScriptDataEscapedEndTagOpen);
			} else {
				t.emit('<');
				t.transition(ScriptDataEscaped);
			}
		}
	},
	ScriptDataEscapedEndTagOpen {
		void read(Tokeniser t, CharacterReader r) {
			if (r.matchesAsciiAlpha()) {
				t.createTagPending(false);
				t.tagPending.appendTagName(r.current());
				t.dataBuffer.append(r.current());
				t.advanceTransition(ScriptDataEscapedEndTagName);
			} else {
				t.emit("</");
				t.transition(ScriptDataEscaped);
			}
		}
	},
	ScriptDataEscapedEndTagName {
		void read(Tokeniser t, CharacterReader r) {
			handleDataEndTag(t, r, ScriptDataEscaped);
		}
	},
	ScriptDataDoubleEscapeStart {
		void read(Tokeniser t, CharacterReader r) {
			handleDataDoubleEscapeTag(t, r, ScriptDataDoubleEscaped, ScriptDataEscaped);
		}
	},
	ScriptDataDoubleEscaped {
		void read(Tokeniser t, CharacterReader r) {
			char c = r.current();
			switch (c) {
				case '-' -> {
					t.emit(c);
					t.advanceTransition(ScriptDataDoubleEscapedDash);
				}
				case '<' -> {
					t.emit(c);
					t.advanceTransition(ScriptDataDoubleEscapedLessthanSign);
				}
				case nullChar -> {
					t.error(this);
					r.advance();
					t.emit(replacementChar);
				}
				case eof -> {
					t.eofError(this);
					t.transition(Data);
				}
				default -> {
					String data = r.consumeToAny('-', '<', nullChar);
					t.emit(data);
				}
			}
		}
	},
	ScriptDataDoubleEscapedDash {
		void read(Tokeniser t, CharacterReader r) {
			char c = r.consume();
			switch (c) {
				case '-' -> {
					t.emit(c);
					t.transition(ScriptDataDoubleEscapedDashDash);
				}
				case '<' -> {
					t.emit(c);
					t.transition(ScriptDataDoubleEscapedLessthanSign);
				}
				case nullChar -> {
					t.error(this);
					t.emit(replacementChar);
					t.transition(ScriptDataDoubleEscaped);
				}
				case eof -> {
					t.eofError(this);
					t.transition(Data);
				}
				default -> {
					t.emit(c);
					t.transition(ScriptDataDoubleEscaped);
				}
			}
		}
	},
	ScriptDataDoubleEscapedDashDash {
		void read(Tokeniser t, CharacterReader r) {
			char c = r.consume();
			switch (c) {
				case '-' -> t.emit(c);
				case '<' -> {
					t.emit(c);
					t.transition(ScriptDataDoubleEscapedLessthanSign);
				}
				case '>' -> {
					t.emit(c);
					t.transition(ScriptData);
				}
				case nullChar -> {
					t.error(this);
					t.emit(replacementChar);
					t.transition(ScriptDataDoubleEscaped);
				}
				case eof -> {
					t.eofError(this);
					t.transition(Data);
				}
				default -> {
					t.emit(c);
					t.transition(ScriptDataDoubleEscaped);
				}
			}
		}
	},
	ScriptDataDoubleEscapedLessthanSign {
		void read(Tokeniser t, CharacterReader r) {
			if (r.matches('/')) {
				t.emit('/');
				t.createTempBuffer();
				t.advanceTransition(ScriptDataDoubleEscapeEnd);
			} else {
				t.transition(ScriptDataDoubleEscaped);
			}
		}
	},
	ScriptDataDoubleEscapeEnd {
		void read(Tokeniser t, CharacterReader r) {
			handleDataDoubleEscapeTag(t, r, ScriptDataEscaped, ScriptDataDoubleEscaped);
		}
	},
	BeforeAttributeName {
		// from tagname <xxx
		void read(Tokeniser t, CharacterReader r) {
			char c = r.consume();
			switch (c) {
				case '\t':
				case '\n':
				case '\r':
				case '\f':
				case ' ':
					break; // ignore whitespace
				case '/':
					t.transition(SelfClosingStartTag);
					break;
				case '<': // NOTE: out of spec, but clear (spec has this as a part of the attribute name)
					r.unconsume();
					t.error(this);
					// intended fall through as if >
				case '>':
					t.emitTagPending();
					t.transition(Data);
					break;
				case nullChar:
					r.unconsume();
					t.error(this);
					t.tagPending.newAttribute();
					t.transition(AttributeName);
					break;
				case eof:
					t.eofError(this);
					t.transition(Data);
					break;
				case '"':
				case '\'':
				case '=':
					t.error(this);
					t.tagPending.newAttribute();
					t.tagPending.appendAttributeName(c);
					t.transition(AttributeName);
					break;
				default: // A-Z, anything else
					t.tagPending.newAttribute();
					r.unconsume();
					t.transition(AttributeName);
			}
		}
	},
	AttributeName {
		// from before attribute name
		void read(Tokeniser t, CharacterReader r) {
			String name = r.consumeToAnySorted(attributeNameCharsSorted); // spec deviate - consume and emit nulls in one hit vs stepping
			t.tagPending.appendAttributeName(name);

			char c = r.consume();
			switch (c) {
				case '\t', '\n', '\r', '\f', ' ' -> t.transition(AfterAttributeName);
				case '/' -> t.transition(SelfClosingStartTag);
				case '=' -> t.transition(BeforeAttributeValue);
				case '>' -> {
					t.emitTagPending();
					t.transition(Data);
				}
				case eof -> {
					t.eofError(this);
					t.transition(Data);
				}
				case '"', '\'', '<' -> {
					t.error(this);
					t.tagPending.appendAttributeName(c);
				}
				default -> // buffer underrun
						t.tagPending.appendAttributeName(c);
			}
		}
	},
	AfterAttributeName {
		void read(Tokeniser t, CharacterReader r) {
			char c = r.consume();
			switch (c) {
				case '\t':
				case '\n':
				case '\r':
				case '\f':
				case ' ':
					// ignore
					break;
				case '/':
					t.transition(SelfClosingStartTag);
					break;
				case '=':
					t.transition(BeforeAttributeValue);
					break;
				case '>':
					t.emitTagPending();
					t.transition(Data);
					break;
				case nullChar:
					t.error(this);
					t.tagPending.appendAttributeName(replacementChar);
					t.transition(AttributeName);
					break;
				case eof:
					t.eofError(this);
					t.transition(Data);
					break;
				case '"':
				case '\'':
				case '<':
					t.error(this);
					t.tagPending.newAttribute();
					t.tagPending.appendAttributeName(c);
					t.transition(AttributeName);
					break;
				default: // A-Z, anything else
					t.tagPending.newAttribute();
					r.unconsume();
					t.transition(AttributeName);
			}
		}
	},
	BeforeAttributeValue {
		void read(Tokeniser t, CharacterReader r) {
			char c = r.consume();
			switch (c) {
				case '\t':
				case '\n':
				case '\r':
				case '\f':
				case ' ':
					// ignore
					break;
				case '"':
					t.transition(AttributeValue_doubleQuoted);
					break;
				case '\'':
					t.transition(AttributeValue_singleQuoted);
					break;
				case nullChar:
					t.error(this);
					t.tagPending.appendAttributeValue(replacementChar);
					t.transition(AttributeValue_unquoted);
					break;
				case eof:
					t.eofError(this);
					t.emitTagPending();
					t.transition(Data);
					break;
				case '>':
					t.error(this);
					t.emitTagPending();
					t.transition(Data);
					break;
				case '<':
				case '=':
				case '`':
					t.error(this);
					t.tagPending.appendAttributeValue(c);
					t.transition(AttributeValue_unquoted);
					break;
				case '&':
				default:
					r.unconsume();
					t.transition(AttributeValue_unquoted);
			}
		}
	},
	AttributeValue_doubleQuoted {
		void read(Tokeniser t, CharacterReader r) {
			String value = r.consumeAttributeQuoted(false);
			if (value.length() > 0) t.tagPending.appendAttributeValue(value);
			else t.tagPending.setEmptyAttributeValue();

			char c = r.consume();
			switch (c) {
				case '"' -> t.transition(AfterAttributeValue_quoted);
				case '&' -> {
					int[] ref = t.consumeCharacterReference('"', true);
					if (ref != null) t.tagPending.appendAttributeValue(ref);
					else t.tagPending.appendAttributeValue('&');
				}
				case nullChar -> {
					t.error(this);
					t.tagPending.appendAttributeValue(replacementChar);
				}
				case eof -> {
					t.eofError(this);
					t.transition(Data);
				}
				default -> // hit end of buffer in first read, still in attribute
						t.tagPending.appendAttributeValue(c);
			}
		}
	},
	AttributeValue_singleQuoted {
		void read(Tokeniser t, CharacterReader r) {
			String value = r.consumeAttributeQuoted(true);
			if (value.length() > 0) t.tagPending.appendAttributeValue(value);
			else t.tagPending.setEmptyAttributeValue();

			char c = r.consume();
			switch (c) {
				case '\'' -> t.transition(AfterAttributeValue_quoted);
				case '&' -> {
					int[] ref = t.consumeCharacterReference('\'', true);
					if (ref != null) t.tagPending.appendAttributeValue(ref);
					else t.tagPending.appendAttributeValue('&');
				}
				case nullChar -> {
					t.error(this);
					t.tagPending.appendAttributeValue(replacementChar);
				}
				case eof -> {
					t.eofError(this);
					t.transition(Data);
				}
				default -> // hit end of buffer in first read, still in attribute
						t.tagPending.appendAttributeValue(c);
			}
		}
	},
	AttributeValue_unquoted {
		void read(Tokeniser t, CharacterReader r) {
			String value = r.consumeToAnySorted(attributeValueUnquoted);
			if (value.length() > 0) t.tagPending.appendAttributeValue(value);

			char c = r.consume();
			switch (c) {
				case '\t', '\n', '\r', '\f', ' ' -> t.transition(BeforeAttributeName);
				case '&' -> {
					int[] ref = t.consumeCharacterReference('>', true);
					if (ref != null) t.tagPending.appendAttributeValue(ref);
					else t.tagPending.appendAttributeValue('&');
				}
				case '>' -> {
					t.emitTagPending();
					t.transition(Data);
				}
				case nullChar -> {
					t.error(this);
					t.tagPending.appendAttributeValue(replacementChar);
				}
				case eof -> {
					t.eofError(this);
					t.transition(Data);
				}
				case '"', '\'', '<', '=', '`' -> {
					t.error(this);
					t.tagPending.appendAttributeValue(c);
				}
				default -> // hit end of buffer in first read, still in attribute
						t.tagPending.appendAttributeValue(c);
			}

		}
	},
	// CharacterReferenceInAttributeValue state handled inline
	AfterAttributeValue_quoted {
		void read(Tokeniser t, CharacterReader r) {
			char c = r.consume();
			switch (c) {
				case '\t', '\n', '\r', '\f', ' ' -> t.transition(BeforeAttributeName);
				case '/' -> t.transition(SelfClosingStartTag);
				case '>' -> {
					t.emitTagPending();
					t.transition(Data);
				}
				case eof -> {
					t.eofError(this);
					t.transition(Data);
				}
				default -> {
					r.unconsume();
					t.error(this);
					t.transition(BeforeAttributeName);
				}
			}

		}
	},
	SelfClosingStartTag {
		void read(Tokeniser t, CharacterReader r) {
			char c = r.consume();
			switch (c) {
				case '>' -> {
					t.tagPending.selfClosing = true;
					t.emitTagPending();
					t.transition(Data);
				}
				case eof -> {
					t.eofError(this);
					t.transition(Data);
				}
				default -> {
					r.unconsume();
					t.error(this);
					t.transition(BeforeAttributeName);
				}
			}
		}
	},
	BogusComment {
		void read(Tokeniser t, CharacterReader r) {
			// todo: handle bogus comment starting from eof. when does that trigger?
			t.commentPending.append(r.consumeTo('>'));
			// todo: replace nullChar with replaceChar
			char next = r.current();
			if (next == '>' || next == eof) {
				r.consume();
				t.emitCommentPending();
				t.transition(Data);
			}
		}
	},
	MarkupDeclarationOpen {
		void read(Tokeniser t, CharacterReader r) {
			if (r.matchConsume("--")) {
				t.createCommentPending();
				t.transition(CommentStart);
			} else if (r.matchConsumeIgnoreCase("DOCTYPE")) {
				t.transition(Doctype);
			} else if (r.matchConsume("[CDATA[")) {
				// todo: should actually check current namespace, and only non-html allows cdata. until namespace
				// is implemented properly, keep handling as cdata
				//} else if (!t.currentNodeInHtmlNS() && r.matchConsume("[CDATA[")) {
				t.createTempBuffer();
				t.transition(CdataSection);
			} else {
				t.error(this);
				t.createBogusCommentPending();
				t.transition(BogusComment);
			}
		}
	},
	CommentStart {
		void read(Tokeniser t, CharacterReader r) {
			char c = r.consume();
			switch (c) {
				case '-' -> t.transition(CommentStartDash);
				case nullChar -> {
					t.error(this);
					t.commentPending.append(replacementChar);
					t.transition(Comment);
				}
				case '>' -> {
					t.error(this);
					t.emitCommentPending();
					t.transition(Data);
				}
				case eof -> {
					t.eofError(this);
					t.emitCommentPending();
					t.transition(Data);
				}
				default -> {
					r.unconsume();
					t.transition(Comment);
				}
			}
		}
	},
	CommentStartDash {
		void read(Tokeniser t, CharacterReader r) {
			char c = r.consume();
			switch (c) {
				case '-' -> t.transition(CommentEnd);
				case nullChar -> {
					t.error(this);
					t.commentPending.append(replacementChar);
					t.transition(Comment);
				}
				case '>' -> {
					t.error(this);
					t.emitCommentPending();
					t.transition(Data);
				}
				case eof -> {
					t.eofError(this);
					t.emitCommentPending();
					t.transition(Data);
				}
				default -> {
					t.commentPending.append(c);
					t.transition(Comment);
				}
			}
		}
	},
	Comment {
		void read(Tokeniser t, CharacterReader r) {
			char c = r.current();
			switch (c) {
				case '-' -> t.advanceTransition(CommentEndDash);
				case nullChar -> {
					t.error(this);
					r.advance();
					t.commentPending.append(replacementChar);
				}
				case eof -> {
					t.eofError(this);
					t.emitCommentPending();
					t.transition(Data);
				}
				default -> t.commentPending.append(r.consumeToAny('-', nullChar));
			}
		}
	},
	CommentEndDash {
		void read(Tokeniser t, CharacterReader r) {
			char c = r.consume();
			switch (c) {
				case '-' -> t.transition(CommentEnd);
				case nullChar -> {
					t.error(this);
					t.commentPending.append('-').append(replacementChar);
					t.transition(Comment);
				}
				case eof -> {
					t.eofError(this);
					t.emitCommentPending();
					t.transition(Data);
				}
				default -> {
					t.commentPending.append('-').append(c);
					t.transition(Comment);
				}
			}
		}
	},
	CommentEnd {
		void read(Tokeniser t, CharacterReader r) {
			char c = r.consume();
			switch (c) {
				case '>' -> {
					t.emitCommentPending();
					t.transition(Data);
				}
				case nullChar -> {
					t.error(this);
					t.commentPending.append("--").append(replacementChar);
					t.transition(Comment);
				}
				case '!' -> t.transition(CommentEndBang);
				case '-' -> t.commentPending.append('-');
				case eof -> {
					t.eofError(this);
					t.emitCommentPending();
					t.transition(Data);
				}
				default -> {
					t.commentPending.append("--").append(c);
					t.transition(Comment);
				}
			}
		}
	},
	CommentEndBang {
		void read(Tokeniser t, CharacterReader r) {
			char c = r.consume();
			switch (c) {
				case '-' -> {
					t.commentPending.append("--!");
					t.transition(CommentEndDash);
				}
				case '>' -> {
					t.emitCommentPending();
					t.transition(Data);
				}
				case nullChar -> {
					t.error(this);
					t.commentPending.append("--!").append(replacementChar);
					t.transition(Comment);
				}
				case eof -> {
					t.eofError(this);
					t.emitCommentPending();
					t.transition(Data);
				}
				default -> {
					t.commentPending.append("--!").append(c);
					t.transition(Comment);
				}
			}
		}
	},
	Doctype {
		void read(Tokeniser t, CharacterReader r) {
			char c = r.consume();
			switch (c) {
				case '\t':
				case '\n':
				case '\r':
				case '\f':
				case ' ':
					t.transition(BeforeDoctypeName);
					break;
				case eof:
					t.eofError(this);
					// note: fall through to > case
				case '>': // catch invalid <!DOCTYPE>
					t.error(this);
					t.createDoctypePending();
					t.doctypePending.forceQuirks = true;
					t.emitDoctypePending();
					t.transition(Data);
					break;
				default:
					t.error(this);
					t.transition(BeforeDoctypeName);
			}
		}
	},
	BeforeDoctypeName {
		void read(Tokeniser t, CharacterReader r) {
			if (r.matchesAsciiAlpha()) {
				t.createDoctypePending();
				t.transition(DoctypeName);
				return;
			}
			char c = r.consume();
			switch (c) {
				case '\t':
				case '\n':
				case '\r':
				case '\f':
				case ' ':
					break; // ignore whitespace
				case nullChar:
					t.error(this);
					t.createDoctypePending();
					t.doctypePending.name.append(replacementChar);
					t.transition(DoctypeName);
					break;
				case eof:
					t.eofError(this);
					t.createDoctypePending();
					t.doctypePending.forceQuirks = true;
					t.emitDoctypePending();
					t.transition(Data);
					break;
				default:
					t.createDoctypePending();
					t.doctypePending.name.append(c);
					t.transition(DoctypeName);
			}
		}
	},
	DoctypeName {
		void read(Tokeniser t, CharacterReader r) {
			if (r.matchesLetter()) {
				String name = r.consumeLetterSequence();
				t.doctypePending.name.append(name);
				return;
			}
			char c = r.consume();
			switch (c) {
				case '>' -> {
					t.emitDoctypePending();
					t.transition(Data);
				}
				case '\t', '\n', '\r', '\f', ' ' -> t.transition(AfterDoctypeName);
				case nullChar -> {
					t.error(this);
					t.doctypePending.name.append(replacementChar);
				}
				case eof -> {
					t.eofError(this);
					t.doctypePending.forceQuirks = true;
					t.emitDoctypePending();
					t.transition(Data);
				}
				default -> t.doctypePending.name.append(c);
			}
		}
	},
	AfterDoctypeName {
		void read(Tokeniser t, CharacterReader r) {
			if (r.isEmpty()) {
				t.eofError(this);
				t.doctypePending.forceQuirks = true;
				t.emitDoctypePending();
				t.transition(Data);
				return;
			}
			if (r.matchesAny('\t', '\n', '\r', '\f', ' ')) r.advance(); // ignore whitespace
			else if (r.matches('>')) {
				t.emitDoctypePending();
				t.advanceTransition(Data);
			} else if (r.matchConsumeIgnoreCase(DocumentType.PUBLIC_KEY)) {
				t.doctypePending.pubSysKey = DocumentType.PUBLIC_KEY;
				t.transition(AfterDoctypePublicKeyword);
			} else if (r.matchConsumeIgnoreCase(DocumentType.SYSTEM_KEY)) {
				t.doctypePending.pubSysKey = DocumentType.SYSTEM_KEY;
				t.transition(AfterDoctypeSystemKeyword);
			} else {
				t.error(this);
				t.doctypePending.forceQuirks = true;
				t.advanceTransition(BogusDoctype);
			}

		}
	},
	AfterDoctypePublicKeyword {
		void read(Tokeniser t, CharacterReader r) {
			char c = r.consume();
			switch (c) {
				case '\t', '\n', '\r', '\f', ' ' -> t.transition(BeforeDoctypePublicIdentifier);
				case '"' -> {
					t.error(this);
					// set public id to empty string
					t.transition(DoctypePublicIdentifier_doubleQuoted);
				}
				case '\'' -> {
					t.error(this);
					// set public id to empty string
					t.transition(DoctypePublicIdentifier_singleQuoted);
				}
				case '>' -> {
					t.error(this);
					t.doctypePending.forceQuirks = true;
					t.emitDoctypePending();
					t.transition(Data);
				}
				case eof -> {
					t.eofError(this);
					t.doctypePending.forceQuirks = true;
					t.emitDoctypePending();
					t.transition(Data);
				}
				default -> {
					t.error(this);
					t.doctypePending.forceQuirks = true;
					t.transition(BogusDoctype);
				}
			}
		}
	},
	BeforeDoctypePublicIdentifier {
		void read(Tokeniser t, CharacterReader r) {
			char c = r.consume();
			switch (c) {
				case '\t':
				case '\n':
				case '\r':
				case '\f':
				case ' ':
					break;
				case '"':
					// set public id to empty string
					t.transition(DoctypePublicIdentifier_doubleQuoted);
					break;
				case '\'':
					// set public id to empty string
					t.transition(DoctypePublicIdentifier_singleQuoted);
					break;
				case '>':
					t.error(this);
					t.doctypePending.forceQuirks = true;
					t.emitDoctypePending();
					t.transition(Data);
					break;
				case eof:
					t.eofError(this);
					t.doctypePending.forceQuirks = true;
					t.emitDoctypePending();
					t.transition(Data);
					break;
				default:
					t.error(this);
					t.doctypePending.forceQuirks = true;
					t.transition(BogusDoctype);
			}
		}
	},
	DoctypePublicIdentifier_doubleQuoted {
		void read(Tokeniser t, CharacterReader r) {
			char c = r.consume();
			switch (c) {
				case '"' -> t.transition(AfterDoctypePublicIdentifier);
				case nullChar -> {
					t.error(this);
					t.doctypePending.publicIdentifier.append(replacementChar);
				}
				case '>' -> {
					t.error(this);
					t.doctypePending.forceQuirks = true;
					t.emitDoctypePending();
					t.transition(Data);
				}
				case eof -> {
					t.eofError(this);
					t.doctypePending.forceQuirks = true;
					t.emitDoctypePending();
					t.transition(Data);
				}
				default -> t.doctypePending.publicIdentifier.append(c);
			}
		}
	},
	DoctypePublicIdentifier_singleQuoted {
		void read(Tokeniser t, CharacterReader r) {
			char c = r.consume();
			switch (c) {
				case '\'' -> t.transition(AfterDoctypePublicIdentifier);
				case nullChar -> {
					t.error(this);
					t.doctypePending.publicIdentifier.append(replacementChar);
				}
				case '>' -> {
					t.error(this);
					t.doctypePending.forceQuirks = true;
					t.emitDoctypePending();
					t.transition(Data);
				}
				case eof -> {
					t.eofError(this);
					t.doctypePending.forceQuirks = true;
					t.emitDoctypePending();
					t.transition(Data);
				}
				default -> t.doctypePending.publicIdentifier.append(c);
			}
		}
	},
	AfterDoctypePublicIdentifier {
		void read(Tokeniser t, CharacterReader r) {
			char c = r.consume();
			switch (c) {
				case '\t', '\n', '\r', '\f', ' ' -> t.transition(BetweenDoctypePublicAndSystemIdentifiers);
				case '>' -> {
					t.emitDoctypePending();
					t.transition(Data);
				}
				case '"' -> {
					t.error(this);
					// system id empty
					t.transition(DoctypeSystemIdentifier_doubleQuoted);
				}
				case '\'' -> {
					t.error(this);
					// system id empty
					t.transition(DoctypeSystemIdentifier_singleQuoted);
				}
				case eof -> {
					t.eofError(this);
					t.doctypePending.forceQuirks = true;
					t.emitDoctypePending();
					t.transition(Data);
				}
				default -> {
					t.error(this);
					t.doctypePending.forceQuirks = true;
					t.transition(BogusDoctype);
				}
			}
		}
	},
	BetweenDoctypePublicAndSystemIdentifiers {
		void read(Tokeniser t, CharacterReader r) {
			char c = r.consume();
			switch (c) {
				case '\t':
				case '\n':
				case '\r':
				case '\f':
				case ' ':
					break;
				case '>':
					t.emitDoctypePending();
					t.transition(Data);
					break;
				case '"':
					t.error(this);
					// system id empty
					t.transition(DoctypeSystemIdentifier_doubleQuoted);
					break;
				case '\'':
					t.error(this);
					// system id empty
					t.transition(DoctypeSystemIdentifier_singleQuoted);
					break;
				case eof:
					t.eofError(this);
					t.doctypePending.forceQuirks = true;
					t.emitDoctypePending();
					t.transition(Data);
					break;
				default:
					t.error(this);
					t.doctypePending.forceQuirks = true;
					t.transition(BogusDoctype);
			}
		}
	},
	AfterDoctypeSystemKeyword {
		void read(Tokeniser t, CharacterReader r) {
			char c = r.consume();
			switch (c) {
				case '\t', '\n', '\r', '\f', ' ' -> t.transition(BeforeDoctypeSystemIdentifier);
				case '>' -> {
					t.error(this);
					t.doctypePending.forceQuirks = true;
					t.emitDoctypePending();
					t.transition(Data);
				}
				case '"' -> {
					t.error(this);
					// system id empty
					t.transition(DoctypeSystemIdentifier_doubleQuoted);
				}
				case '\'' -> {
					t.error(this);
					// system id empty
					t.transition(DoctypeSystemIdentifier_singleQuoted);
				}
				case eof -> {
					t.eofError(this);
					t.doctypePending.forceQuirks = true;
					t.emitDoctypePending();
					t.transition(Data);
				}
				default -> {
					t.error(this);
					t.doctypePending.forceQuirks = true;
					t.emitDoctypePending();
				}
			}
		}
	},
	BeforeDoctypeSystemIdentifier {
		void read(Tokeniser t, CharacterReader r) {
			char c = r.consume();
			switch (c) {
				case '\t':
				case '\n':
				case '\r':
				case '\f':
				case ' ':
					break;
				case '"':
					// set system id to empty string
					t.transition(DoctypeSystemIdentifier_doubleQuoted);
					break;
				case '\'':
					// set public id to empty string
					t.transition(DoctypeSystemIdentifier_singleQuoted);
					break;
				case '>':
					t.error(this);
					t.doctypePending.forceQuirks = true;
					t.emitDoctypePending();
					t.transition(Data);
					break;
				case eof:
					t.eofError(this);
					t.doctypePending.forceQuirks = true;
					t.emitDoctypePending();
					t.transition(Data);
					break;
				default:
					t.error(this);
					t.doctypePending.forceQuirks = true;
					t.transition(BogusDoctype);
			}
		}
	},
	DoctypeSystemIdentifier_doubleQuoted {
		void read(Tokeniser t, CharacterReader r) {
			char c = r.consume();
			switch (c) {
				case '"' -> t.transition(AfterDoctypeSystemIdentifier);
				case nullChar -> {
					t.error(this);
					t.doctypePending.systemIdentifier.append(replacementChar);
				}
				case '>' -> {
					t.error(this);
					t.doctypePending.forceQuirks = true;
					t.emitDoctypePending();
					t.transition(Data);
				}
				case eof -> {
					t.eofError(this);
					t.doctypePending.forceQuirks = true;
					t.emitDoctypePending();
					t.transition(Data);
				}
				default -> t.doctypePending.systemIdentifier.append(c);
			}
		}
	},
	DoctypeSystemIdentifier_singleQuoted {
		void read(Tokeniser t, CharacterReader r) {
			char c = r.consume();
			switch (c) {
				case '\'' -> t.transition(AfterDoctypeSystemIdentifier);
				case nullChar -> {
					t.error(this);
					t.doctypePending.systemIdentifier.append(replacementChar);
				}
				case '>' -> {
					t.error(this);
					t.doctypePending.forceQuirks = true;
					t.emitDoctypePending();
					t.transition(Data);
				}
				case eof -> {
					t.eofError(this);
					t.doctypePending.forceQuirks = true;
					t.emitDoctypePending();
					t.transition(Data);
				}
				default -> t.doctypePending.systemIdentifier.append(c);
			}
		}
	},
	AfterDoctypeSystemIdentifier {
		void read(Tokeniser t, CharacterReader r) {
			char c = r.consume();
			switch (c) {
				case '\t':
				case '\n':
				case '\r':
				case '\f':
				case ' ':
					break;
				case '>':
					t.emitDoctypePending();
					t.transition(Data);
					break;
				case eof:
					t.eofError(this);
					t.doctypePending.forceQuirks = true;
					t.emitDoctypePending();
					t.transition(Data);
					break;
				default:
					t.error(this);
					t.transition(BogusDoctype);
					// NOT force quirks
			}
		}
	},
	BogusDoctype {
		void read(Tokeniser t, CharacterReader r) {
			char c = r.consume();
			switch (c) {
				case '>', eof -> {
					t.emitDoctypePending();
					t.transition(Data);
				}
				default -> {}
			}
		}
	},
	CdataSection {
		void read(Tokeniser t, CharacterReader r) {
			String data = r.consumeTo("]]>");
			t.dataBuffer.append(data);
			if (r.matchConsume("]]>") || r.isEmpty()) {
				t.emit(new Token.CData(t.dataBuffer.toString()));
				t.transition(Data);
			}// otherwise, buffer underrun, stay in data section
		}
	};

	abstract void read(Tokeniser t, CharacterReader r);

	static final char nullChar = '\u0000';
	// char searches. must be sorted, used in inSorted. MUST update TokenisetStateTest if more arrays are added.
	static final char[] attributeNameCharsSorted = new char[] { '\t', '\n', '\f', '\r', ' ', '"', '\'', '/', '<', '=', '>' };
	static final char[] attributeValueUnquoted = new char[] { nullChar, '\t', '\n', '\f', '\r', ' ', '"', '&', '\'', '<', '=', '>', '`' };

	private static final char replacementChar = Tokeniser.replacementChar;
	private static final String replacementStr = String.valueOf(Tokeniser.replacementChar);
	private static final char eof = CharacterReader.EOF;

	/**
	 * Handles RawtextEndTagName, ScriptDataEndTagName, and ScriptDataEscapedEndTagName. Same body impl, just
	 * different else exit transitions.
	 */
	private static void handleDataEndTag(Tokeniser t, CharacterReader r, TokeniserState elseTransition) {
		if (r.matchesLetter()) {
			String name = r.consumeLetterSequence();
			t.tagPending.appendTagName(name);
			t.dataBuffer.append(name);
			return;
		}

		boolean needsExitTransition = false;
		if (t.isAppropriateEndTagToken() && !r.isEmpty()) {
			char c = r.consume();
			switch (c) {
				case '\t', '\n', '\r', '\f', ' ' -> t.transition(BeforeAttributeName);
				case '/' -> t.transition(SelfClosingStartTag);
				case '>' -> {
					t.emitTagPending();
					t.transition(Data);
				}
				default -> {
					t.dataBuffer.append(c);
					needsExitTransition = true;
				}
			}
		} else {
			needsExitTransition = true;
		}

		if (needsExitTransition) {
			t.emit("</");
			t.emit(t.dataBuffer);
			t.transition(elseTransition);
		}
	}

	private static void readRawData(Tokeniser t, CharacterReader r, TokeniserState current, TokeniserState advance) {
		switch (r.current()) {
			case '<' -> t.advanceTransition(advance);
			case nullChar -> {
				t.error(current);
				r.advance();
				t.emit(replacementChar);
			}
			case eof -> t.emit(new Token.EOF());
			default -> {
				String data = r.consumeRawData();
				t.emit(data);
			}
		}
	}

	private static void readCharRef(Tokeniser t, TokeniserState advance) {
		int[] c = t.consumeCharacterReference(null, false);
		if (c == null) t.emit('&');
		else t.emit(c);
		t.transition(advance);
	}

	private static void readEndTag(Tokeniser t, CharacterReader r, TokeniserState a, TokeniserState b) {
		if (r.matchesAsciiAlpha()) {
			t.createTagPending(false);
			t.transition(a);
		} else {
			t.emit("</");
			t.transition(b);
		}
	}

	private static void handleDataDoubleEscapeTag(Tokeniser t, CharacterReader r, TokeniserState primary, TokeniserState fallback) {
		if (r.matchesLetter()) {
			String name = r.consumeLetterSequence();
			t.dataBuffer.append(name);
			t.emit(name);
			return;
		}

		char c = r.consume();
		switch (c) {
			case '\t', '\n', '\r', '\f', ' ', '/', '>' -> {
				if (t.dataBuffer.toString().equals("script")) t.transition(primary);
				else t.transition(fallback);
				t.emit(c);
			}
			default -> {
				r.unconsume();
				t.transition(fallback);
			}
		}
	}
}
