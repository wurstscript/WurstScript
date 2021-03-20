package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstio.languageserver.ModelManager;

/** Created by peter on 05.05.16. */
public class CleanProject extends UserRequest<String> {

  @Override
  public String execute(ModelManager modelManager) {
    modelManager.clean();
    modelManager.buildProject();
    return "done";
  }
}
