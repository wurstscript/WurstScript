package de.peeeq.wurstio.languageserver;

/**
 * Created by peter on 24.04.16.
 */
public class ReplyPackage {
	private int requestId;
	private Object data;

	public ReplyPackage(int requestId, Object data) {
		this.data = data;
		this.requestId = requestId;
	}
}
