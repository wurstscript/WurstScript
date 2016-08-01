package de.peeeq.wurstio.deps;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class WurstProjectFile {
	
	
	private JsonObject json;


	public WurstProjectFile(String json) {
		this(new Gson().fromJson(json, JsonObject.class));
	}
	
	public WurstProjectFile(Reader json) {
		this(new Gson().fromJson(json, JsonObject.class));
	}
	
	public WurstProjectFile(JsonObject json) {
		this.json = json;
	}

	
	public String getLibraryName() {
		return json.get("name").getAsString();
	}
	
	public DepVersion getLibraryVersion() {
		try {
			return new DepVersion(json.get("version").getAsString());
		} catch (InvalidVersionStringException e) {
			return null;
		}
	}
	
	public List<DependencyInfo> getDependencies() {
		JsonArray depArray = json.get("dependencies").getAsJsonArray();
		List<DependencyInfo> result = new ArrayList<>();
		for (JsonElement jsonElement : depArray) {
			result.add(DependencyInfo.fromJson(jsonElement));
		}
		return result;
	}
	
	
	
	public DepVersion getMinWurstVersion() {
		try {
			return new DepVersion(json.get("minWurstVersion").getAsString());
		} catch (InvalidVersionStringException e) {
			return null;
		}
	}
	
	
	

}
