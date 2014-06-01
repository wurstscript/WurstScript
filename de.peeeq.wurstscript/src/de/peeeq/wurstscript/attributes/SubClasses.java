package de.peeeq.wurstscript.attributes;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.ConstructorDefs;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.types.WurstTypeClass;
import de.peeeq.wurstscript.utils.Utils;

public class SubClasses {


	public static Multimap<ClassDef, ClassDef> getSubClasses(CompilationUnit cu) {
		Multimap<ClassDef, ClassDef> result = HashMultimap.create();
		for (ClassDef c : cu.attrGetByType().classes) {
			if (c.attrExtendedClass() != null) {
				result.put(c.attrExtendedClass(), c);
			}
		}
		result = Utils.transientClosure(result);
		return result ;
	}

	public static @Nullable ClassDef getExtendedClass(ClassDef classDef) {
		if (classDef.getExtendedClass().attrTyp() instanceof WurstTypeClass) {
			WurstTypeClass c = (WurstTypeClass) classDef.getExtendedClass().attrTyp();
			if (classDef == c.getClassDef()) {
				classDef.getExtendedClass().addError("Classes must not extend themselves");
				return null;
			}
			return c.getClassDef();
		} else if (classDef.getExtendedClass() instanceof TypeExpr) {
			classDef.getExtendedClass().addError("Cannot extend " + classDef.getExtendedClass().attrTyp() + ", because it is not a class.");
		}
		return null;
	}

	public static @Nullable ConstructorDef getSuperConstructor(ConstructorDef constr) {
		ClassDef c = constr.attrNearestClassDef();
		if (c == null) {
			return null;
		}
		ClassDef superClass = c.attrExtendedClass();
		if (superClass == null) {
			return null;
		}
		// call super constructor
		ConstructorDefs constructors = superClass.getConstructors();
		ConstructorDef superConstr = OverloadingResolver.resolveSuperCall(constructors, constr);
		return superConstr;
	}

}
