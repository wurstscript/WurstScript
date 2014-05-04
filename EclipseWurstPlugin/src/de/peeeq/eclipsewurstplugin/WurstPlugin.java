package de.peeeq.eclipsewurstplugin;

import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_BOLD;
import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_COLOR;
import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_COMMENT;
import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_CONSTRUCTOR;
import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_DATATYPE;
import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_FIELD;
import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_FUNCTION;
import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_INTERFACE;
import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_ITALIC;
import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_JASSTYPE;
import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_KEYWORD;
import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_PARAM;
import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_RGB_COMMENT;
import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_RGB_CONSTRUCTOR;
import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_RGB_DATATYPE;
import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_RGB_FIELD;
import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_RGB_FUNCTION;
import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_RGB_INTERFACE;
import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_RGB_JASSTYPE;
import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_RGB_KEYWORD;
import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_RGB_PARAM;
import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_RGB_STRING;
import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_RGB_TEXT;
import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_RGB_VAR;
import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_STRIKETHROUGH;
import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_STRING;
import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_ANNOTATION;
import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_RGB_ANNOTATION;
import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_TEXT;
import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_UNDERLINE;
import static de.peeeq.eclipsewurstplugin.WurstConstants.SYNTAXCOLOR_VAR;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

import de.peeeq.eclipsewurstplugin.editor.WurstEditor;
import de.peeeq.eclipsewurstplugin.editor.highlighting.ScannerFactory;
import de.peeeq.eclipsewurstplugin.ui.WurstPerspective;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.utils.WinRegistry;

/**
 * The activator class controls the plug-in life cycle
 */
public class WurstPlugin extends AbstractUIPlugin {

	private final class PluginLogHandler extends Handler {
		private final ILog log;

		private PluginLogHandler(ILog log) {
			this.log = log;
		}

		@Override
		public void publish(LogRecord record) {
			int level = Status.WARNING;
			if (record.getLevel() == Level.SEVERE) {
				level = Status.ERROR;
			} else if (record.getLevel() == Level.INFO) {
				level = Status.INFO;
			}
			log.log(new Status(level, PLUGIN_ID, Status.OK, record.getMessage(), record.getThrown()));
		}

		@Override
		public void flush() {
		}

		@Override
		public void close() throws SecurityException {
		}
	}

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
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		WLogger.setHandler(new PluginLogHandler(getLog()));
		WLogger.setLevel(Level.INFO);
		plugin = this;
		
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				initializePreferenceStore();
				scanners = new ScannerFactory();
				try {
					WurstPerspective.findConsole();
				} catch (Throwable t) {
					// ignore error
					t.printStackTrace();
				}
			}
		});
		
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
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
		setDefaultValue(SYNTAXCOLOR_COLOR + SYNTAXCOLOR_TEXT,    	 SYNTAXCOLOR_RGB_TEXT);
		setDefaultValue(SYNTAXCOLOR_COLOR + SYNTAXCOLOR_KEYWORD,     SYNTAXCOLOR_RGB_KEYWORD);
		setDefaultValue(SYNTAXCOLOR_COLOR + SYNTAXCOLOR_JASSTYPE,    SYNTAXCOLOR_RGB_JASSTYPE);
		setDefaultValue(SYNTAXCOLOR_COLOR + SYNTAXCOLOR_STRING,      SYNTAXCOLOR_RGB_STRING);
		setDefaultValue(SYNTAXCOLOR_COLOR + SYNTAXCOLOR_ANNOTATION,  SYNTAXCOLOR_RGB_ANNOTATION);
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
		setDefaultValue(SYNTAXCOLOR_BOLD + SYNTAXCOLOR_ANNOTATION, false);
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
		setDefaultValue(SYNTAXCOLOR_ITALIC + SYNTAXCOLOR_ANNOTATION, false);
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
		setDefaultValue(SYNTAXCOLOR_UNDERLINE + SYNTAXCOLOR_ANNOTATION, false);
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
		setDefaultValue(SYNTAXCOLOR_STRIKETHROUGH + SYNTAXCOLOR_ANNOTATION, false);
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
		setDefaultValue(WurstConstants.WURST_MPQEDIT_PATH, "C:\\mpqedit\\MPQEditor.exe");
		setDefaultValue(WurstConstants.WURST_WC3_PATH, "C:\\Warcraft III\\");
		try {
			// try to use the registry to find wc3 path
			WinRegistry reg = new WinRegistry();
			String installPath = reg.readString(WinRegistry.HKEY_CURRENT_USER, "Software\\Blizzard Entertainment\\Warcraft III", "InstallPath");
			if (installPath != null) {
				setDefaultValue(WurstConstants.WURST_WC3_PATH, installPath);
			}
		} catch (NoSuchMethodException e) {
			// ignore, registry not supported
			WLogger.info("Registry not supported");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
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

	public static void refreshEditors() {
		IWorkbenchWindow wb = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		for (IEditorReference ref:wb.getActivePage().getEditorReferences()) {
			if (ref.getEditor(false) instanceof WurstEditor) {
				WurstEditor ed = (WurstEditor) ref.getEditor(false);
				ed.refresh();
			}
		}
	}
	
}
