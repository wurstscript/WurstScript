package de.peeeq.wurstscript.attributes;

import java.util.Collection;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.ConstructorDefs;
import de.peeeq.wurstscript.types.PscriptTypeClass;
import de.peeeq.wurstscript.utils.Utils;

public class SubClasses {

	public static Collection<ClassDef> getSubClasses(ClassDef classDef) {
		CompilationUnit cu = classDef.attrCompilationUnit();
		return cu.attrSubClasses().get(classDef);
	}

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

	public static ClassDef getExtendedClass(ClassDef classDef) {
		if (classDef.getExtendedClass().attrTyp() instanceof PscriptTypeClass) {
			PscriptTypeClass c = (PscriptTypeClass) classDef.getExtendedClass().attrTyp();
			return c.getClassDef();
		}
		return null;
	}

	public static ConstructorDef getSuperConstructor(ConstructorDef constr) {
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
