package de.peeeq.wurstscript;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import de.peeeq.immutablecollections.ImmutableList;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ClassOrModule;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.ModuleUse;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.utils.Pair;

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
		Preconditions.checkNotNull(m);
		if (m.getModuleInstanciations().size() > 0) {
			return;
		}
		

		for (ModuleUse moduleUse : m.getModuleUses()) {
			ModuleDef usedModule = moduleUse.attrModuleDef();
			if (usedModule == null) {
				moduleUse.addError("not found");
				continue;
			}
			expandModules(usedModule);

			
			int numTypeArgs = moduleUse.getTypeArgs().size();
			if (numTypeArgs < usedModule.getTypeParameters().size()) {
				moduleUse.addError("Missing type arguments for module " 
						 + moduleUse.getModuleName() + ".");
			} else if (numTypeArgs > usedModule.getTypeParameters().size()) {
				moduleUse.addError("Too many type arguments for module " 
						 + moduleUse.getModuleName() + ".");
			}

			List<Pair<WurstType, WurstType>> typeReplacements = Lists.newArrayList(); 
			for (int i=0; i<numTypeArgs; i++) {
				typeReplacements.add(Pair.create(usedModule.getTypeParameters().get(i).attrTyp(), moduleUse.getTypeArgs().get(i).attrTyp()));
			}
			
			m.getModuleInstanciations().add(
					Ast.ModuleInstanciation(moduleUse.getSource(), Ast.Modifiers(), 
							usedModule.getName(), 
							smartCopy(usedModule.getMethods(), typeReplacements), 
							smartCopy(usedModule.getVars(), typeReplacements),  
							smartCopy(usedModule.getConstructors(), typeReplacements), 
							smartCopy(usedModule.getModuleInstanciations(), typeReplacements), 
							smartCopy(usedModule.getModuleUses(), typeReplacements), 
							smartCopy(usedModule.getOnDestroy(), typeReplacements)));
		}
		
	}

	private <T extends AstElement> T smartCopy(T e,	List<Pair<WurstType, WurstType>> typeReplacements) {
		List<Pair<ImmutableList<Integer>, TypeExpr>> replacementsByPath = Lists.newArrayList();
		calcReplacementsByPath(typeReplacements, replacementsByPath, e, ImmutableList.<Integer>emptyList());
		
		
		AstElement copy = e.copy();
		
		// Do the type replacements
		for (Pair<ImmutableList<Integer>, TypeExpr> rep : replacementsByPath) {
			doReplacement(copy, rep.getA(), (TypeExpr) rep.getB().copy());
		}
		
		@SuppressWarnings("unchecked")
		T t = (T) copy;
		return t;
	}
	
	private void doReplacement(AstElement e, ImmutableList<Integer> a, TypeExpr newTypeExpr) {
		if (a.size() == 1) {
			e.set(a.head(), newTypeExpr);
		} else if (a.size() > 1) {
			doReplacement(e.get(a.head()), a.tail(), newTypeExpr);
		}
	}

	private void calcReplacementsByPath(List<Pair<WurstType, WurstType>> typeReplacements, List<Pair<ImmutableList<Integer>, TypeExpr>> replacementsByPath, AstElement e, ImmutableList<Integer> pos) {
		if (e instanceof TypeExpr) {
			TypeExpr typeExpr = (TypeExpr) e;
			for (Pair<WurstType, WurstType> rep : typeReplacements) {
				if (typeExpr.attrTyp().equalsType(rep.getA(), e)) {
					WPos source = typeExpr.getSource();
					replacementsByPath.add(Pair.create(pos, (TypeExpr) Ast.TypeExprResolved(source, rep.getB())));
				}
			}
		}
		// children:
		for (int i=0; i<e.size(); i++) {
			calcReplacementsByPath(typeReplacements, replacementsByPath, e.get(i), pos.appBack(i));
		}
	}

	
}
