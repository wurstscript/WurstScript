package de.peeeq.eclipsewurstplugin.preferences;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.peeeq.eclipsewurstplugin.WurstConstants;
import de.peeeq.eclipsewurstplugin.util.UtilityFunctions;

public class WurstPreferencePage extends PreferencePage implements IWorkbenchPreferencePage{
	
	

	private FieldEditor enableReconciling;
	private FieldEditor recDelay;
	private FieldEditor enableAutocomplete;
	private FieldEditor autocompleteDelay;
	private FieldEditor wc3Path;
	private BooleanFieldEditor ignoreErrors;

	@Override
	public void init(IWorkbench workbench) {
		setDescription("Please select a subentry to modify global preferences");
		setPreferenceStore(UtilityFunctions.getDefaultPreferenceStore());
	}
	


	@Override
	protected Control createContents(Composite parent) {
//		new StringFieldEditor(WurstConstants.WURST_WC3_PATH, "Warcraft installation path: ", comp1);
		final Composite rootComposite = new Composite(parent, SWT.NONE);
		rootComposite.setLayout(GridLayoutFactory.fillDefaults().create());
		
		final ScrolledComposite sc = new ScrolledComposite(rootComposite, SWT.BORDER | SWT.V_SCROLL);
		sc.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, 200).create());
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		
		final Composite comp = new Composite(sc, SWT.NONE);
		sc.setContent(comp);
		comp.setLayout(new GridLayout(1, false));
		
		createReconcilationControls(comp);
		createAutocompleteControls(comp);
		createPathControls(comp);
		createAdditionalOptions(comp);
		
		loadSettings();
		
		sc.setMinWidth(0);
		
		comp.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event event) {
				sc.setMinHeight(comp.computeSize(comp.getSize().x, SWT.DEFAULT).y);
			}
		});
		sc.setMinHeight(comp.computeSize(comp.getSize().x, SWT.DEFAULT).y);
		sc.setContent(comp);
		return rootComposite;
	}



	private void createAdditionalOptions(Composite comp) {
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		
		Group g = new Group(comp, SWT.NONE);
		g.setLayoutData(gridData);
		GridLayout layout = new GridLayout(2, false);
		g.setLayout(layout);
		
		g.setText("Other options:");
		
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = SWT.FILL;
		
		Composite c1 = new Composite(g, SWT.NONE);
		ignoreErrors = new BooleanFieldEditor(WurstConstants.WURST_IGNORE_ERRORS, "Ignore all errors", c1 );
		ignoreErrors.setPreferenceStore(getPreferenceStore());
		c1.setLayoutData(gridData);
		
	}



	private void createPathControls(Composite comp) {
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		
		Group g = new Group(comp, SWT.NONE);
		g.setLayoutData(gridData);
		GridLayout layout = new GridLayout(2, false);
		g.setLayout(layout);
		
		g.setText("External Tool Paths");
		
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = SWT.FILL;
		
		
//		Label t = new Label(g, SWT.WRAP);
//		t.setText("Reconcilation is the ability of the editor to check your code while typing. " +
//				"When you stop typing the editor will wait for the given delay " +
//				"and then trigger the checking procedure in the background.");
//		t.setLayoutData(gridData);
		
		
		Composite c1 = new Composite(g, SWT.NONE);
		wc3Path = new StringFieldEditor(WurstConstants.WURST_WC3_PATH, "Warcraft installation path: ", c1);
		wc3Path.setPreferenceStore(getPreferenceStore());
		c1.setLayoutData(gridData);
		
//		Composite c2 = new Composite(g, SWT.NONE);
//		mpqeditPath = new StringFieldEditor(WurstConstants.WURST_MPQEDIT_PATH, "Path to MPQEditor.exe", c2);
//		mpqeditPath.setPreferenceStore(getPreferenceStore());
//		c1.setLayoutData(gridData);
		
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
		
	}



	private void loadSettings() {
		enableAutocomplete.load();
		enableReconciling.load();
		autocompleteDelay.load();
		recDelay.load();
		wc3Path.load();
		ignoreErrors.load();
	}
	
	
	@Override
	protected void performDefaults() {		
		enableAutocomplete.loadDefault();
		enableReconciling.loadDefault();
		autocompleteDelay.loadDefault();
		recDelay.loadDefault();
		wc3Path.loadDefault();
		ignoreErrors.loadDefault();
		super.performDefaults();
	}
	
	@Override
	public boolean performOk() {
		enableAutocomplete.store();
		enableReconciling.store();
		autocompleteDelay.store();
		recDelay.store();
		wc3Path.store();
		ignoreErrors.store();
		return super.performOk();
	}
	
	
}
