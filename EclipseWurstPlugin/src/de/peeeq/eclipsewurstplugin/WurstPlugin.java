package de.peeeq.eclipsewurstplugin;

import static de.peeeq.eclipsewurstplugin.WurstConstants.*;


import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.peeeq.eclipsewurstplugin.editor.highlighting.ScannerFactory;
import de.peeeq.eclipsewurstplugin.ui.WurstPerspective;

/**
 * The activator class controls the plug-in life cycle
 */
public class WurstPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "EclipseWurstPlugin"; //$NON-NLS-1$

	public static final String WURST_PERSPECTIVE_ID = "de.peeeq.eclipsewurstplugin.wurstperspective";

	// The shared instance
	private static WurstPlugin plugin;

	private ScannerFactory scanners;
	
	/**
	 * The constructor
	 */
	public WurstPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		initializePreferenceStore();
		scanners = new ScannerFactory();
		WurstPerspective.findConsole();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static WurstPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	private void initializePreferenceStore(){
		//Initialize default values of preferenceStore
		
		//Colors for syntax highlighting
		setDefaultValue(SYNTAXCOLOR_COLOR + SYNTAXCOLOR_KEYWORD,     SYNTAXCOLOR_RGB_KEYWORD);
		setDefaultValue(SYNTAXCOLOR_COLOR + SYNTAXCOLOR_JASSTYPE,    SYNTAXCOLOR_RGB_JASSTYPE);
		setDefaultValue(SYNTAXCOLOR_COLOR + SYNTAXCOLOR_STRING,      SYNTAXCOLOR_RGB_STRING);
		setDefaultValue(SYNTAXCOLOR_COLOR + SYNTAXCOLOR_COMMENT,     SYNTAXCOLOR_RGB_COMMENT);
		setDefaultValue(SYNTAXCOLOR_COLOR + SYNTAXCOLOR_FUNCTION,    SYNTAXCOLOR_RGB_FUNCTION);
		setDefaultValue(SYNTAXCOLOR_COLOR + SYNTAXCOLOR_DATATYPE,    SYNTAXCOLOR_RGB_DATATYPE);
		setDefaultValue(SYNTAXCOLOR_COLOR + SYNTAXCOLOR_VAR,         SYNTAXCOLOR_RGB_VAR        );
		setDefaultValue(SYNTAXCOLOR_COLOR + SYNTAXCOLOR_PARAM,       SYNTAXCOLOR_RGB_PARAM      );
		setDefaultValue(SYNTAXCOLOR_COLOR + SYNTAXCOLOR_FIELD,       SYNTAXCOLOR_RGB_FIELD      );
		setDefaultValue(SYNTAXCOLOR_COLOR + SYNTAXCOLOR_INTERFACE,   SYNTAXCOLOR_RGB_INTERFACE  );
		setDefaultValue(SYNTAXCOLOR_COLOR + SYNTAXCOLOR_CONSTRUCTOR, SYNTAXCOLOR_RGB_CONSTRUCTOR);

		//Style for syntax highlighting
		setDefaultValue(SYNTAXCOLOR_BOLD + SYNTAXCOLOR_KEYWORD,    true);
		setDefaultValue(SYNTAXCOLOR_BOLD + SYNTAXCOLOR_JASSTYPE,   false);
		setDefaultValue(SYNTAXCOLOR_BOLD + SYNTAXCOLOR_STRING,     false);
		setDefaultValue(SYNTAXCOLOR_BOLD + SYNTAXCOLOR_COMMENT,    false);
		setDefaultValue(SYNTAXCOLOR_BOLD + SYNTAXCOLOR_FUNCTION,   false);
		setDefaultValue(SYNTAXCOLOR_BOLD + SYNTAXCOLOR_DATATYPE,   false);
		setDefaultValue(SYNTAXCOLOR_BOLD + SYNTAXCOLOR_VAR,        false);
		setDefaultValue(SYNTAXCOLOR_BOLD + SYNTAXCOLOR_PARAM,      false);
		setDefaultValue(SYNTAXCOLOR_BOLD + SYNTAXCOLOR_FIELD,      false);
		setDefaultValue(SYNTAXCOLOR_BOLD + SYNTAXCOLOR_INTERFACE,  false);
		setDefaultValue(SYNTAXCOLOR_BOLD + SYNTAXCOLOR_CONSTRUCTOR,false);
		
		setDefaultValue(SYNTAXCOLOR_ITALIC + SYNTAXCOLOR_KEYWORD,    false);
		setDefaultValue(SYNTAXCOLOR_ITALIC + SYNTAXCOLOR_JASSTYPE,   false);
		setDefaultValue(SYNTAXCOLOR_ITALIC + SYNTAXCOLOR_STRING,     false);
		setDefaultValue(SYNTAXCOLOR_ITALIC + SYNTAXCOLOR_COMMENT,    false);
		setDefaultValue(SYNTAXCOLOR_ITALIC + SYNTAXCOLOR_FUNCTION,   true);
		setDefaultValue(SYNTAXCOLOR_ITALIC + SYNTAXCOLOR_DATATYPE,   false);
		setDefaultValue(SYNTAXCOLOR_ITALIC + SYNTAXCOLOR_VAR,        false);
		setDefaultValue(SYNTAXCOLOR_ITALIC + SYNTAXCOLOR_PARAM,      false);
		setDefaultValue(SYNTAXCOLOR_ITALIC + SYNTAXCOLOR_FIELD,      false);
		setDefaultValue(SYNTAXCOLOR_ITALIC + SYNTAXCOLOR_INTERFACE,  false);
		setDefaultValue(SYNTAXCOLOR_ITALIC + SYNTAXCOLOR_CONSTRUCTOR,true);
		
		setDefaultValue(SYNTAXCOLOR_UNDERLINE + SYNTAXCOLOR_KEYWORD,    false);
		setDefaultValue(SYNTAXCOLOR_UNDERLINE + SYNTAXCOLOR_JASSTYPE,   false);
		setDefaultValue(SYNTAXCOLOR_UNDERLINE + SYNTAXCOLOR_STRING,     false);
		setDefaultValue(SYNTAXCOLOR_UNDERLINE + SYNTAXCOLOR_COMMENT,    false);
		setDefaultValue(SYNTAXCOLOR_UNDERLINE + SYNTAXCOLOR_FUNCTION,   false);
		setDefaultValue(SYNTAXCOLOR_UNDERLINE + SYNTAXCOLOR_DATATYPE,   false);
		setDefaultValue(SYNTAXCOLOR_UNDERLINE + SYNTAXCOLOR_VAR,        false);
		setDefaultValue(SYNTAXCOLOR_UNDERLINE + SYNTAXCOLOR_PARAM,      false);
		setDefaultValue(SYNTAXCOLOR_UNDERLINE + SYNTAXCOLOR_FIELD,      false);
		setDefaultValue(SYNTAXCOLOR_UNDERLINE + SYNTAXCOLOR_INTERFACE,  false);
		setDefaultValue(SYNTAXCOLOR_UNDERLINE + SYNTAXCOLOR_CONSTRUCTOR,false);
		
		setDefaultValue(SYNTAXCOLOR_STRIKETHROUGH + SYNTAXCOLOR_KEYWORD,    false);
		setDefaultValue(SYNTAXCOLOR_STRIKETHROUGH + SYNTAXCOLOR_JASSTYPE,   false);
		setDefaultValue(SYNTAXCOLOR_STRIKETHROUGH + SYNTAXCOLOR_STRING,     false);
		setDefaultValue(SYNTAXCOLOR_STRIKETHROUGH + SYNTAXCOLOR_COMMENT,    false);
		setDefaultValue(SYNTAXCOLOR_STRIKETHROUGH + SYNTAXCOLOR_FUNCTION,   false);
		setDefaultValue(SYNTAXCOLOR_STRIKETHROUGH + SYNTAXCOLOR_DATATYPE,   false);
		setDefaultValue(SYNTAXCOLOR_STRIKETHROUGH + SYNTAXCOLOR_VAR,        false);
		setDefaultValue(SYNTAXCOLOR_STRIKETHROUGH + SYNTAXCOLOR_PARAM,      false);
		setDefaultValue(SYNTAXCOLOR_STRIKETHROUGH + SYNTAXCOLOR_FIELD,      false);
		setDefaultValue(SYNTAXCOLOR_STRIKETHROUGH + SYNTAXCOLOR_INTERFACE,  false);
		setDefaultValue(SYNTAXCOLOR_STRIKETHROUGH + SYNTAXCOLOR_CONSTRUCTOR,false);
		
		
		setDefaultValue(WurstConstants.WURST_ENABLE_AUTOCOMPLETE, true);
		setDefaultValue(WurstConstants.WURST_AUTOCOMPLETION_DELAY, "0.5");
		setDefaultValue(WurstConstants.WURST_ENABLE_RECONCILING, true);
		setDefaultValue(WurstConstants.WURST_RECONCILATION_DELAY, "0.5");
		
	}
	
	private void setDefaultValue(String name, boolean value){
		getDefaultPreferenceStore().setDefault(name, value);
	}
	
	private void setDefaultValue(String name, String value){
		getDefaultPreferenceStore().setDefault(name, value);
	}
	
	private void setDefaultValue(String name, RGB value){
		PreferenceConverter.setDefault(getDefaultPreferenceStore(), name, value);
	}
	
	public static IPreferenceStore getDefaultPreferenceStore(){
		return WurstPlugin.getDefault().getPreferenceStore();
	}

	public ScannerFactory scanners() {
		return scanners;
	}

	public static WurstEclipseConfig config() {
		return new WurstEclipseConfig(getDefaultPreferenceStore());
		
	}
	
}
