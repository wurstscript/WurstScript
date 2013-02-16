package de.peeeq.eclipsewurstplugin;

import org.eclipse.jface.preference.IPreferenceStore;

public class WurstEclipseConfig {

	private IPreferenceStore pref;

	public WurstEclipseConfig(IPreferenceStore pref) {
		this.pref = pref;
	}

	public boolean reconilationEnabled() {
		return pref.getBoolean(WurstConstants.WURST_ENABLE_RECONCILING);
	}

	
	public boolean autocompleteEnabled() {
		return pref.getBoolean(WurstConstants.WURST_ENABLE_AUTOCOMPLETE);
	}
	
	public float reconilationDelay() {
		return pref.getFloat(WurstConstants.WURST_RECONCILATION_DELAY);
	}
	
	public float autocompleteDelay() {
		return pref.getFloat(WurstConstants.WURST_AUTOCOMPLETION_DELAY);
	}
	
}
