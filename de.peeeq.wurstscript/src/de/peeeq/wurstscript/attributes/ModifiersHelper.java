package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.AstElementWithModifier;
import de.peeeq.wurstscript.ast.ModOverride;
import de.peeeq.wurstscript.ast.ModStatic;
import de.peeeq.wurstscript.ast.Modifier;
import de.peeeq.wurstscript.ast.VisibilityPrivate;
import de.peeeq.wurstscript.ast.VisibilityPublic;
import de.peeeq.wurstscript.ast.VisibilityPublicread;

public class ModifiersHelper {

	public static boolean isPublic(AstElementWithModifier e) {
		return containsType(e.getModifiers(), VisibilityPublic.class);
	}

	

	public static boolean isPublicRead(AstElementWithModifier e) {
		return containsType(e.getModifiers(), VisibilityPublicread.class);
	}

	public static boolean isPrivate(AstElementWithModifier e) {
		return containsType(e.getModifiers(), VisibilityPrivate.class);
	}

	public static boolean isStatic(AstElementWithModifier e) {
		return containsType(e.getModifiers(), ModStatic.class);
	}

	
	public static boolean isOverride(AstElementWithModifier e) {
		return containsType(e.getModifiers(), ModOverride.class);
	}
	
	
	
	static boolean containsType(de.peeeq.wurstscript.ast.Modifiers modifiers, Class<? extends Modifier> class1) {
		for (Modifier m : modifiers) {
			if (class1.isInstance(m)) {
				return true;
			}
		}
		return false;
	}


}
