package de.peeeq.wurstio.languageserver2.requests;

import de.peeeq.wurstio.languageserver2.ModelManager;

/**
 * Created by peter on 05.05.16.
 */
public class CleanProject extends UserRequest<Void> {

	@Override
	public Void execute(ModelManager modelManager) {
		modelManager.clean();
		modelManager.buildProject();
		return null;
	}
}
