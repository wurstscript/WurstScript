package de.peeeq.pscript.intermediateLang;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

public interface IntermediateCodeGenerator {
	ILprog translateProg(Resource resource);
	ILprog translatePrograms(Iterable<? extends EObject> programs);
}
