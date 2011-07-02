
package de.peeeq.pscript;

/**
 * Initialization support for running Xtext languages 
 * without equinox extension registry
 */
public class PscriptStandaloneSetup extends PscriptStandaloneSetupGenerated{

	public static void doSetup() {
		new PscriptStandaloneSetup().createInjectorAndDoEMFRegistration();
	}
}

