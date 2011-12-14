package de.peeeq.wurstscript.attributes;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;

import de.peeeq.wurstscript.ast.AstElementWithModifier;
import de.peeeq.wurstscript.ast.ModAbstract;
import de.peeeq.wurstscript.ast.ModConstant;
import de.peeeq.wurstscript.ast.ModOverride;
import de.peeeq.wurstscript.ast.ModStatic;
import de.peeeq.wurstscript.ast.Modifier;
import de.peeeq.wurstscript.ast.VisibilityPrivate;
import de.peeeq.wurstscript.ast.VisibilityProtected;
import de.peeeq.wurstscript.ast.VisibilityPublic;
import de.peeeq.wurstscript.ast.VisibilityPublicread;

public class ModifiersHelper {

	public static boolean isPublic(AstElementWithModifier e) {
		return containsType(e.getModifiers(), VisibilityPublic.class);
	}

	public static boolean isProtected(AstElementWithModifier e) {
		return containsType(e.getModifiers(), VisibilityProtected.class);
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

	public static boolean isAbstract(AstElementWithModifier e) {
		return containsType(e.getModifiers(), ModAbstract.class);
	}

	public static boolean isConstant(AstElementWithModifier e) {
		return containsType(e.getModifiers(), ModConstant.class);
	}

	static boolean containsType(de.peeeq.wurstscript.ast.Modifiers modifiers, Class<? extends Modifier> class1) {
		for (Modifier m : modifiers) {
			if (class1.isInstance(m)) {
				return true;
			}
		}
		return false;
	}


	static void checkAllowedModifiers(AstElementWithModifier e) {
		// TODO check allowed modifiers and call this method from checker
	}


}
