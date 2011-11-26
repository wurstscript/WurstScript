package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.Modifier;
import de.peeeq.wurstscript.ast.Modifiers;
import de.peeeq.wurstscript.ast.VisibilityPrivate;
import de.peeeq.wurstscript.ast.VisibilityProtected;
import de.peeeq.wurstscript.ast.VisibilityPublic;
import de.peeeq.wurstscript.ast.VisibilityPublicread;

public enum Visibility {
	DEFAULT, PUBLIC, PRIVATE, PROTECTED, PUBLICREAD;
	
	static public Visibility get(Modifiers mods) {
		for (Modifier m : mods) {
			if (m instanceof VisibilityPrivate) {
				return PRIVATE;
			}
			if (m instanceof VisibilityPublic) {
				return PUBLIC;
			}
			if (m instanceof VisibilityProtected) {
				return PROTECTED;
			}
			if (m instanceof VisibilityPublicread) {
				return PUBLICREAD;
			}
		}
		return DEFAULT;
	}
}
