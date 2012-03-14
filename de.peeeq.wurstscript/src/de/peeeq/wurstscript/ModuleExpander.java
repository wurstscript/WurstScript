package de.peeeq.wurstscript;

import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.ClassSlot;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleInstanciation;
import de.peeeq.wurstscript.ast.ModuleUse;
import de.peeeq.wurstscript.ast.TopLevelDeclaration;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.attributes.attr;

public class ModuleExpander {

	private Set<ClassOrModule> done = Sets.newHashSet();

	public void expandModules(CompilationUnit root) {
		for (TopLevelDeclaration t : root) {
			if (t instanceof WPackage) {
				expandModules((WPackage) t);
			}
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
		Preconditions.checkNotNull(m);
		if (done.contains(m)) {
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
							moduleUse.getModuleName(), m.getMethods(), m.getVars(), m.getConstructors(), 
							m.getModuleInstanciations(), m.getModuleUses(), m.getOnDestroy()));
		}
		
		
		done.add(m);
	}

	
}
