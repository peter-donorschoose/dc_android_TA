package org.psk.kea;

/**
 * @author Pete
 * An implementation of the "parameter object" idiom i.e. a group of related fields.
 * Encapsulates the key elements of an email - subject, addressee, body
 */
public class HappyEmail {
	private String _to;
	private String _subject;
	private String _body;

	public HappyEmail(final String to, final String subject, final String body) {
		_to = to;
		_subject = subject;
		_body = body;
	}

	String getTo() {
		return _to;
	}

	String getSubject() {
		return _subject;
	}

	String getBody() {
		return _body;
	}
}
