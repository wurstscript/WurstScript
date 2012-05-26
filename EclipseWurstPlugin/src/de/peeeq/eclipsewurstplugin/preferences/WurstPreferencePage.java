package de.peeeq.eclipsewurstplugin.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.peeeq.eclipsewurstplugin.util.UtilityFunctions;

public class WurstPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage{
	
	@Override
	public void init(IWorkbench workbench) {
		setDescription("Please select a subentry to modify global preferences");
		setPreferenceStore(UtilityFunctions.getDefaultPreferenceStore());
		noDefaultAndApplyButton();
	}

	@Override
	protected void createFieldEditors() {
		//reserved for future use
	}
}
