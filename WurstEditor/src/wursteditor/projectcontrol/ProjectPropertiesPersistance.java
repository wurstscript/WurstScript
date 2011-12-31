package wursteditor.projectcontrol;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;

public class ProjectPropertiesPersistance {

	static public void saveProperties(ProjectProperties p, String fileName) throws IOException {
		String json = new Gson().toJson(p);
		Files.write(json, new File(fileName), Charsets.UTF_8);
	}
	
	static public ProjectProperties loadProperties(String fileName) throws IOException {
		String json = Files.toString(new File(fileName), Charsets.UTF_8);
		return new Gson().fromJson(json, ProjectProperties.class);
	}
	
}
