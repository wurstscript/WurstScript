package de.peeeq.eclipsewurstplugin.util;

import static de.peeeq.eclipsewurstplugin.WurstPlugin.SYNTAXCOLOR_BOLD;
import static de.peeeq.eclipsewurstplugin.WurstPlugin.SYNTAXCOLOR_ITALIC;
import static de.peeeq.eclipsewurstplugin.WurstPlugin.SYNTAXCOLOR_STRIKETHROUGH;
import static de.peeeq.eclipsewurstplugin.WurstPlugin.SYNTAXCOLOR_UNDERLINE;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import eclipsewurstplugin.Activator;

public class UtilityFunctions {

	public static void showErrorMessage(final String errorMessage){
		MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error", errorMessage);
	}

	public static IPreferenceStore getDefaultPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	
	public static int computeAttributes(IPreferenceStore store, String postfix) {
		int funattr = 0;

		boolean attrbold = store.getBoolean(SYNTAXCOLOR_BOLD + postfix);
		if(attrbold) funattr = funattr | SWT.BOLD;

		boolean attritalic = store.getBoolean(SYNTAXCOLOR_ITALIC + postfix);
		if(attritalic) funattr = funattr | SWT.ITALIC;

		boolean attrunderline = store.getBoolean(SYNTAXCOLOR_UNDERLINE + postfix);
		if(attrunderline) funattr = funattr | TextAttribute.UNDERLINE;

		boolean attrstrikethrough = store.getBoolean(SYNTAXCOLOR_STRIKETHROUGH + postfix);
		if(attrstrikethrough) funattr = funattr | TextAttribute.STRIKETHROUGH;
		
		return funattr;
	}
}
