
package de.peeeq;

/**
 * Initialization support for running Xtext languages 
 * without equinox extension registry
 */
public class JassToPscriptStandaloneSetup extends JassToPscriptStandaloneSetupGenerated{

	public static void doSetup() {
		new JassToPscriptStandaloneSetup().createInjectorAndDoEMFRegistration();
	}
}

