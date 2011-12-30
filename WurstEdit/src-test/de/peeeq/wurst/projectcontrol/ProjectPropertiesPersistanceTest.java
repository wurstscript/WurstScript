package de.peeeq.wurst.projectcontrol;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.google.common.collect.Lists;

public class ProjectPropertiesPersistanceTest {

	@Test
	public void test() throws IOException {
		ProjectProperties p1 = new ProjectProperties();
		p1.setName("ABC");
		p1.setFiles(Lists.newArrayList("d", "e", "f"));
		
		ProjectPropertiesPersistance.saveProperties(p1, "temp_properties");
		ProjectProperties p2 = ProjectPropertiesPersistance.loadProperties("temp_properties");
		
		assertEquals(p1.getName(), p2.getName());
		assertEquals(p1.getFiles(), p2.getFiles());
	}

}

