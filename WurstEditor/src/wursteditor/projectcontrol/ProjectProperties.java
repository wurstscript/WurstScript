package wursteditor.projectcontrol;

import java.util.List;

import com.google.common.collect.Lists;

public class ProjectProperties {
	private String name = "New Project";
	private List<String> files = Lists.newArrayList();
	
		
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getFiles() {
		return files;
	}
	public void setFiles(List<String> files) {
		this.files = files;
	}
	
	
}
