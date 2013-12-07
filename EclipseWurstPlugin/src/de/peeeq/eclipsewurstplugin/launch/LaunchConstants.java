package de.peeeq.eclipsewurstplugin.launch;

public class LaunchConstants {

	public static final String MAP_FILE = c("MAP_FILE");
	public static final String PROJECT_NAME = c("PROJECT_NAME");
	public static final String LAUNCH_CONFIG_ID = "de.peeeq.eclipsewurstplugin.wustlauchconfig";

	
	private static String c(String key) {
		return LaunchConstants.class.getCanonicalName() + "_" + key;
	}
}
