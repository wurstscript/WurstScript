package de.peeeq.wurstscript.types;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.ModuleDef;

public class TypesHelper {

	public static PscriptType typeOf(ClassOrModule classOrModule, final boolean isStatic) {
		return classOrModule.match(new ClassOrModule.Matcher<PscriptType>() {

			@Override
			public PscriptType case_ClassDef(ClassDef classDef) {
				return new PscriptTypeClass(classDef, isStatic);
			}

			@Override
			public PscriptType case_ModuleDef(ModuleDef moduleDef) {
				return new PscriptTypeModule(moduleDef, isStatic);
			}
		});
	}

}
