package de.peeeq.wurstscript;

import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleUse;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.attributes.attr;

public class ModuleExpander {


	public void expandModules(CompilationUnit cu) {
		for (WPackage t : cu.getPackages()) {
			expandModules((WPackage) t);
		}
	}

	private void expandModules(WPackage p) {
		for (WEntity e : p.getElements()) {
			if (e instanceof ClassOrModule) {
				expandModules((ClassOrModule) e);
			}
		}
	}

	private void expandModules(ClassOrModule m) {
		System.out.println();
		Preconditions.checkNotNull(m);
		if (m.getModuleInstanciations().size() > 0) {
			return;
		}
		

		for (ModuleUse moduleUse : m.getModuleUses()) {
			ModuleDef usedModule = moduleUse.attrModuleDef();
			if (usedModule == null) {
				attr.addError(moduleUse.getSource(), "not found");
				continue;
			}
			expandModules(usedModule);

			
			
			m.getModuleInstanciations().add(
					Ast.ModuleInstanciation(moduleUse.getSource().copy(), Ast.Modifiers(), 
							usedModule.getName(), usedModule.getMethods().copy(), usedModule.getVars().copy(), usedModule.getConstructors().copy(), 
							usedModule.getModuleInstanciations().copy(), usedModule.getModuleUses().copy(), usedModule.getOnDestroy().copy()));
		}
		
	}

	
}
