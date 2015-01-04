package de.peeeq.eclipsewurstplugin.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;

import de.peeeq.eclipsewurstplugin.console.WurstConsole;

public class WurstPerspective implements IPerspectiveFactory {

	
	
	
	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT, 0.20f, editorArea);
		IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, 0.75f, editorArea);
		IFolderLayout right = layout.createFolder("right", IPageLayout.RIGHT, 0.80f, editorArea);
		left.addView(IPageLayout.ID_PROJECT_EXPLORER);
		left.addPlaceholder(IPageLayout.ID_BOOKMARKS);


		bottom.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		bottom.addView(IPageLayout.ID_TASK_LIST);
		bottom.addView(IPageLayout.ID_PROBLEM_VIEW);

		right.addView(IPageLayout.ID_OUTLINE);

		layout.addNewWizardShortcut("de.peeeq.eclipsewurstplugin.wizards.newproject");

		//add view shortcuts
		layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		layout.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_PROJECT_EXPLORER);
		
		// add wizard shortcuts
		layout.addNewWizardShortcut("de.peeeq.eclipsewurstplugin.wizards.newpackage");
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");
        layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");
		
		
		findConsole();
		
		
		
	}

	public static WurstConsole findConsole() {
	      ConsolePlugin plugin = ConsolePlugin.getDefault();
	      IConsoleManager conMan = plugin.getConsoleManager();
	      IConsole[] existing = conMan.getConsoles();
	      for (int i = 0; i < existing.length; i++)
	         if (existing[i] instanceof WurstConsole)
	            return (WurstConsole) existing[i];
	      //no console found, so create a new one
	      WurstConsole myConsole = new WurstConsole();
	      conMan.addConsoles(new IConsole[]{myConsole});
	      return myConsole;
	   }
	
}
