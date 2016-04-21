package de.peeeq.wurstio.languageserver;

import com.google.gson.JsonElement;

/**
 * @author peter
 *
 */
public class EventPackage {
	private String eventName;
	private JsonElement data;
	
	public EventPackage(String eventName, JsonElement data) {
		this.eventName = eventName;
		this.data = data;
	}
	public JsonElement getData() {
		return data;
	}
	public void setData(JsonElement data) {
		this.data = data;
	}
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	
	
	
}
