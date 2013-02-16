package de.peeeq.eclipsewurstplugin.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.peeeq.eclipsewurstplugin.WurstConstants;
import de.peeeq.eclipsewurstplugin.util.UtilityFunctions;

public class WurstPreferencePage extends PreferencePage implements IWorkbenchPreferencePage{
	
	

	private FieldEditor enableReconciling;
	private FieldEditor recDelay;
	private FieldEditor enableAutocomplete;
	private FieldEditor autocompleteDelay;

	@Override
	public void init(IWorkbench workbench) {
		setDescription("Please select a subentry to modify global preferences");
		setPreferenceStore(UtilityFunctions.getDefaultPreferenceStore());
	}
	


	@Override
	protected Control createContents(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		
		comp.setLayout(new GridLayout(1, false));
		
		createReconcilationControls(comp);
		createAutocompleteControls(comp);
		
		return comp;
	}



	private void createReconcilationControls(Composite comp) {
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		
		Group g = new Group(comp, SWT.NONE);
		g.setLayoutData(gridData);
		GridLayout layout = new GridLayout(2, false);
		g.setLayout(layout);
		
		g.setText("Reconcilation");
		
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = SWT.FILL;
		
		
		Label t = new Label(g, SWT.WRAP);
		t.setText("Reconcilation is the ability of the editor to check your code while typing. " +
				"When you stop typing the editor will wait for the given delay " +
				"and then trigger the checking procedure in the background.");
		t.setLayoutData(gridData);
		
		
		Composite c1 = new Composite(g, SWT.NONE);
		enableReconciling = new BooleanFieldEditor(WurstConstants.WURST_ENABLE_RECONCILING, "Enable reconciling", c1 );
		enableReconciling.setPreferenceStore(getPreferenceStore());
		c1.setLayoutData(gridData);
		
		Composite c2 = new Composite(g, SWT.NONE);
		recDelay = new StringFieldEditor(WurstConstants.WURST_RECONCILATION_DELAY, "Reconcilation Delay (ms): ", c2);
		recDelay.setPreferenceStore(getPreferenceStore());
		c1.setLayoutData(gridData);
	}
	
	private void createAutocompleteControls(Composite comp) {
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		
		Group g = new Group(comp, SWT.NONE);
		g.setLayoutData(gridData);
		GridLayout layout = new GridLayout(2, false);
		g.setLayout(layout);
		
		g.setText("Auto Complete");
		
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = SWT.FILL;
		
		
		Label t = new Label(g, SWT.WRAP);
		t.setText("Auto Complete will show you available methods when you type a dot.");
		t.setLayoutData(gridData);
		
		
		Composite c1 = new Composite(g, SWT.NONE);
		enableAutocomplete = new BooleanFieldEditor(WurstConstants.WURST_ENABLE_AUTOCOMPLETE, "Enable autocomplete", c1 );
		enableAutocomplete.setPreferenceStore(getPreferenceStore());
		c1.setLayoutData(gridData);
		
		Composite c2 = new Composite(g, SWT.NONE);
//		autocompleteDelay = new IntegerFieldEditor(WurstConstants.WURST_AUTOCOMPLETION_DELAY, "Autocompletion Delay (ms): ", c2);
		autocompleteDelay = new StringFieldEditor(WurstConstants.WURST_AUTOCOMPLETION_DELAY, "Autocompletion Delay (ms): ", c2);
		autocompleteDelay.setPreferenceStore(getPreferenceStore());
		c1.setLayoutData(gridData);
		
		
		enableAutocomplete.load();
		enableReconciling.load();
		autocompleteDelay.load();
		recDelay.load();
	}
	
	
	@Override
	protected void performDefaults() {		
		enableAutocomplete.loadDefault();
		enableReconciling.loadDefault();
		autocompleteDelay.loadDefault();
		recDelay.loadDefault();
		super.performDefaults();
	}
	
	@Override
	public boolean performOk() {
		enableAutocomplete.store();
		enableReconciling.store();
		autocompleteDelay.store();
		recDelay.store();
		return super.performOk();
	}
	
	
}
