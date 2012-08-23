package de.peeeq.eclipsewurstplugin.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

public class WurstPerspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		//String editorArea = "eu.hatsproject.abs.Abs";
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
		

	}

}
