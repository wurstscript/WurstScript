package de.peeeq.wurstscript.intermediateLang.translator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import katja.common.NE;

import de.peeeq.wurstscript.ast.ClassDefPos;
import de.peeeq.wurstscript.ast.ConstructorDefPos;
import de.peeeq.wurstscript.ast.FuncDefPos;
import de.peeeq.wurstscript.ast.FunctionDefinitionPos;
import de.peeeq.wurstscript.ast.GlobalVarDefPos;
import de.peeeq.wurstscript.ast.InitBlockPos;
import de.peeeq.wurstscript.ast.NativeFuncPos;
import de.peeeq.wurstscript.ast.PackageOrGlobalPos;
import de.peeeq.wurstscript.ast.VarDefPos;
import de.peeeq.wurstscript.ast.WPackagePos;
import de.peeeq.wurstscript.ast.AST.SortPos;
import de.peeeq.wurstscript.attributes.Attributes;
import de.peeeq.wurstscript.intermediateLang.ILfunction;
import de.peeeq.wurstscript.intermediateLang.ILvar;
import de.peeeq.wurstscript.types.PscriptType;

public class NameManagement {

	
	
	// stores the name for every element
		private final Map<SortPos, String> elementNames = new HashMap<SortPos, String>();
		private final Set<String> usedNames = new HashSet<String>();
		private final Attributes attr;
		private final Map<FunctionDefinitionPos, ILfunction> functions = new HashMap<FunctionDefinitionPos, ILfunction>();
		private final Map<ConstructorDefPos, ILfunction> constructors = new HashMap<ConstructorDefPos, ILfunction>();
		private final Map<InitBlockPos, ILfunction> initBlockFunctions = new HashMap<InitBlockPos, ILfunction>();
		private final Map<ClassDefPos, ILfunction> destroyFunctions = new HashMap<ClassDefPos, ILfunction>();
		private final Map<VarDefPos, ILvar> vars = new HashMap<VarDefPos, ILvar>();
		private long varUniqueNameCounter = 0;
		
		public NameManagement(Attributes attr) {
			this.attr = attr;
		}
		
		/**
		 * get the ILfunction for a given function
		 */
		ILfunction getFunction(final FunctionDefinitionPos calledFunc) {
			if (functions.containsKey(calledFunc)) {
				return functions.get(calledFunc);
			}
			
			final PackageOrGlobalPos p = attr.nearestPackage.get(calledFunc);
			
			String funcName = calledFunc.Switch(new FunctionDefinitionPos.Switch<String, NE>() {

				@Override
				public String CaseFuncDefPos(FuncDefPos term) throws NE {
					String name = calledFunc.signature().name().term();
					if (p instanceof WPackagePos) {
						name = ((WPackagePos) p).name().term() + "_" + name;
					}
					return getNameFor(calledFunc, name);
				}

				@Override
				public String CaseNativeFuncPos(NativeFuncPos term) throws NE {
					return calledFunc.signature().name().term();
				}
			});
			
			String name = getNameFor(calledFunc, funcName);
			
			ILfunction func = new ILfunction(name);
			functions.put(calledFunc, func);
			return func;
		}
		
		
		/**
		 * get the ILfunction for a given constructor
		 */
		ILfunction getConstructorFunction(final ConstructorDefPos constr) {
			if (constructors.containsKey(constr)) {
				return constructors.get(constr);
			}
			
			final PackageOrGlobalPos p = attr.nearestPackage.get(constr);
			final ClassDefPos c = attr.nearestClassDef.get(constr);
			
			String name = c.name().term(); 
			if (p instanceof WPackagePos) {
				name = ((WPackagePos) p).name().term() + "_" + name;
			}
			String funcName = getNameFor(constr, name);
			
			
			ILfunction func = new ILfunction(funcName);
			constructors.put(constr, func);
			return func;
		}
		
		
		ILvar getILvarForVarDef(VarDefPos varDef) {
			if (vars.containsKey(varDef)) {
				return vars.get(varDef);
			}
			PscriptType typ = attr.varDefType.get(varDef);
			String name = varDef.name().term();
			ILvar v = new ILvar(name, typ);
			if (varDef instanceof GlobalVarDefPos) {
				PackageOrGlobalPos pack = attr.nearestPackage.get(varDef);
				if (pack instanceof WPackagePos) {
					name = ((WPackagePos) pack).name().term() + "_" + name;
				}
			}
			vars.put(varDef, v);
			return v;
		}
		
		ILvar getNewLocalVar(ILfunction func, PscriptType type, String name) {
			// find unique name:
			String varName = name;
			if (func.getLocalNames().contains(varName)) {
				do {
					varUniqueNameCounter ++;
					varName = name + varUniqueNameCounter;
				} while (func.getLocalNames().contains(varName));
			}
			ILvar var = new ILvar(varName, type);
			func.addLocalVar(var);
			return var;
		}
		
		private String getNameFor(SortPos term, String name) {
			if (elementNames.containsKey(term)) {
				return elementNames.get(term);
			}
			String result = name;
			if (usedNames.contains(name)) {
				// try to find unique name by appending random numbers:
				do {
					varUniqueNameCounter++;
					result = name + varUniqueNameCounter;
				} while (usedNames.contains(result));
			}
			usedNames.add(result);
			elementNames.put(term, result);
			return result;
		}


		public ILfunction getInitBlockFunction(WPackagePos p, InitBlockPos term) {
			if (initBlockFunctions.containsKey(term)) {
				return initBlockFunctions.get(term);
			}
			String name = getNameFor(term, p.name().term() + "_init");
			ILfunction result = new ILfunction(name);
			initBlockFunctions.put(term, result);
			return result;
		}


		public ILfunction getDestroyFunction(ClassDefPos classDef) {
			if (destroyFunctions.containsKey(classDef)) {
				return destroyFunctions.get(classDef);
			}
			
			WPackagePos pack = (WPackagePos) attr.nearestPackage.get(classDef);
			String name = getNameFor(classDef, pack.name().term() + "_" + classDef.name().term() + "_destroy");
			ILfunction result = new ILfunction(name);
			destroyFunctions.put(classDef, result);
			return result;
		}
}
