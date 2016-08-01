package de.peeeq.wurstio.deps;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class DependencyInfo {
	
	private String name;
	private String version;
	private String gitRepository;
	private String gitRevision;
	private String gitTag;
	private String gitBranch;
	

	public static DependencyInfo fromJson(JsonElement jsonElement) {
		return new Gson().fromJson(jsonElement, DependencyInfo.class);
	}


	public String getName() {
		return name;
	}


	public String getVersion() {
		return version;
	}


	public String getGitRepository() {
		return gitRepository;
	}


	public String getGitRevision() {
		return gitRevision;
	}


	public String getGitTag() {
		return gitTag;
	}


	public String getGitBranch() {
		return gitBranch;
	}
	
	
	
	

}
