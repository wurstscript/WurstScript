package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstio.languageserver.ModelManager;

/**
 * Created by peter on 05.05.16.
 */
public class CleanProject extends UserRequest {
    public CleanProject(int sequenceNr) {
        super(sequenceNr);
    }

    @Override
    public Object execute(ModelManager modelManager) {
        modelManager.clean();
        modelManager.buildProject();
        return null;
    }
}
