package de.peeeq.wurstscript.intermediateLang.translator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.InitBlock;
import de.peeeq.wurstscript.ast.NativeFunc;
import de.peeeq.wurstscript.ast.PackageOrGlobal;
import de.peeeq.wurstscript.ast.SortPos;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.intermediateLang.ILfunction;
import de.peeeq.wurstscript.intermediateLang.ILvar;
import de.peeeq.wurstscript.types.PScriptTypeArray;
import de.peeeq.wurstscript.types.PScriptTypeInt;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.PscriptTypeClass;
import de.peeeq.wurstscript.utils.Utils;

public class NameManagement {

	
	
	// stores the name for every element
		private final Map<SortPos, String> elementNames = new HashMap<SortPos, String>();
		private final Set<String> usedNames = new HashSet<String>();
		private final Map<FunctionDefinition, ILfunction> functions = new HashMap<FunctionDefinition, ILfunction>();
		private final Map<ConstructorDef, ILfunction> constructors = new HashMap<ConstructorDef, ILfunction>();
		private final Map<InitBlock, ILfunction> initBlockFunctions = new HashMap<InitBlock, ILfunction>();
		private final Map<ClassDef, ILfunction> destroyFunctions = new HashMap<ClassDef, ILfunction>();
		private final Map<VarDef, ILvar> vars = new HashMap<VarDef, ILvar>();
		private long varUniqueNameCounter = 0;
		private ILfunction globalInitFunction;
		
		public NameManagement() {
		}
		
		/**
		 * get the ILfunction for a given function
		 */
		ILfunction getFunction(final FunctionDefinition calledFunc) {
			if (functions.containsKey(calledFunc)) {
				return functions.get(calledFunc);
			}
			
			final PackageOrGlobal p = calledFunc.attrNearestPackage();
			
			String funcName = calledFunc.match(new FunctionDefinition.Matcher<String>() {

				@Override
				public String case_FuncDef(FuncDef term)  {
					String name = calledFunc.getSignature().getName();
					if (p instanceof WPackage) {
						name = ((WPackage) p).getName() + "_" + name;
					}
					return getNameFor(calledFunc, name);
				}

				@Override
				public String case_NativeFunc(NativeFunc term)  {
					return calledFunc.getSignature().getName();
				}
			});
			
			String name = getNameFor(calledFunc, funcName);
			
			ILfunction func = new ILfunction(name, calledFunc.getSource());
			functions.put(calledFunc, func);
			return func;
		}
		
		
		/**
		 * get the ILfunction for a given constructor
		 */
		ILfunction getConstructorFunction(final ConstructorDef constr) {
			if (constr == null) throw new IllegalArgumentException("constr must not be null");
			if (constructors.containsKey(constr)) {
				return constructors.get(constr);
			}
			
			final PackageOrGlobal p = constr.attrNearestPackage();
			final ClassDef c = constr.attrNearestClassDef();
			
			String name = c.getName() + "_new"; 
			if (p instanceof WPackage) {
				name = ((WPackage) p).getName() + "_" + name;
			}
			String funcName = getNameFor(constr, name);
			
			
			ILfunction func = new ILfunction(funcName, constr.getSource());
			constructors.put(constr, func);
			return func;
		}
		
		
		ILvar getILvarForVarDef(VarDef varDef) {
			if (vars.containsKey(varDef)) {
				return vars.get(varDef);
			}
			PscriptType typ = varDef.attrTyp();
			typ = translateType(typ);
			String name = varDef.getName();
			if (varDef instanceof GlobalVarDef) {
				PackageOrGlobal pack = varDef.attrNearestPackage();
				if (pack instanceof WPackage) {
					name = ((WPackage) pack).getName() + "_" + name;
				}
			}
			name = getNameFor(varDef, name);
			ILvar v = new ILvar(name, typ);
			vars.put(varDef, v);
			return v;
		}
		
		private PscriptType translateType(PscriptType typ) {
			if (typ instanceof PscriptTypeClass) {
				typ = PScriptTypeInt.instance();
			} else if (typ instanceof PScriptTypeArray) {
				PScriptTypeArray pScriptTypeArray = (PScriptTypeArray) typ;
				return new PScriptTypeArray(translateType(pScriptTypeArray.getBaseType()));
			}
				
				
			return typ;
		}

		public ILvar getILvarForClassMemberDef(GlobalVarDef varDef) {
			if (vars.containsKey(varDef)) {
				return vars.get(varDef);
			}
			PscriptType typ = varDef.attrTyp();
			typ = new PScriptTypeArray(typ, Utils.array(0)); // because this is a class we need an array of this type
			WPackage pack = (WPackage) varDef.attrNearestPackage();
			ClassDef classDef = varDef.attrNearestClassDef();
			String name = getNameFor(varDef, pack.getName() + "_" + classDef .getName() + "_" + varDef.getName());
			ILvar v = new ILvar(name, typ);
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
			type = translateType(type);
			ILvar var = new ILvar(varName, type);
			func.addLocalVar(var);
			return var;
		}
		
		private String getNameFor(SortPos term, String name) {
			if (elementNames.containsKey(term)) {
				return elementNames.get(term);
			}
			String result = getNewName(name);
			elementNames.put(term, result);
			return result;
		}
		
		public String getNewName(String name) {
			String result = name;
			if (usedNames.contains(name)) {
				// try to find unique name by appending random numbers:
				do {
					varUniqueNameCounter++;
					result = name + varUniqueNameCounter;
				} while (usedNames.contains(result));
			}
			usedNames.add(result);
			return result;
		}


		public ILfunction getInitBlockFunction(WPackage p, InitBlock term) {
			if (initBlockFunctions.containsKey(term)) {
				return initBlockFunctions.get(term);
			}
			String name = getNameFor(term, p.getName() + "_init");
			ILfunction result = new ILfunction(name, term.getSource());
			initBlockFunctions.put(term, result);
			return result;
		}


		public ILfunction getDestroyFunction(ClassDef classDef) {
			if (destroyFunctions.containsKey(classDef)) {
				return destroyFunctions.get(classDef);
			}
			
			WPackage pack = (WPackage) classDef.attrNearestPackage();
			String name = getNameFor(classDef, pack.getName() + "_" + classDef.getName() + "_destroy");
			ILfunction result = new ILfunction(name, classDef.getSource());
			destroyFunctions.put(classDef, result);
			return result;
		}

		public ILfunction getGlobalInitFunction() {
			if (globalInitFunction == null) {
				globalInitFunction = new ILfunction("wurst_globalsinit", Ast.WPos("generated", 0, 0));
			}
			return globalInitFunction;
		}

		public ILvar getThis(FuncDef term) {
			return new ILvar("this", PScriptTypeInt.instance());
		}

		

		
}
