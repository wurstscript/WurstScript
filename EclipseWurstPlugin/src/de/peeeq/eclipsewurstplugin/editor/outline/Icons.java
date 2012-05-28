package de.peeeq.eclipsewurstplugin.editor.outline;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;

import de.peeeq.eclipsewurstplugin.WurstPlugin;

public class Icons {

	private static final String ICON_PATH = "resources/icons/";

	public static Image file = createIcon("WurstFile16.gif");
	public static Image block = createIcon("block.gif");
	public static Image wclass = createIcon("class.gif");
	public static Image wenum = createIcon("enum.gif");
	public static Image function = createIcon("function.gif");
	public static Image wimports = createIcon("imports.gif");
	public static Image wimport = createIcon("import.gif");
	public static Image winterface = createIcon("interface.gif");
	public static Image var = createIcon("var.gif");
	public static Image wpackage = createIcon("package.gif");
	
	public static Image createIcon(String path){
		return createImage(ICON_PATH+path);
	}
	
	public static Image createImage(String path){
		Bundle bundle = Platform.getBundle(WurstPlugin.PLUGIN_ID);
		IPath imagepath = new Path(path);
		URL imageUrl = FileLocator.find(bundle, imagepath,null);
		ImageDescriptor id = ImageDescriptor.createFromURL(imageUrl);
		Image im = id.createImage();
		return im;
	}
}
