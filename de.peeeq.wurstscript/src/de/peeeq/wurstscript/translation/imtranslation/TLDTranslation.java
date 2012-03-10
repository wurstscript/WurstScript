package de.peeeq.wurstscript.translation.imtranslation;

import static de.peeeq.wurstscript.jassAst.JassAst.JassExprBinary;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprIntVal;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprVarAccess;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtCall;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtReturn;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassSlot;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.InitBlock;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.JassGlobalBlock;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NativeFunc;
import de.peeeq.wurstscript.ast.NativeType;
import de.peeeq.wurstscript.ast.OpLessEq;
import de.peeeq.wurstscript.ast.TupleDef;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassAst.JassAst;
import de.peeeq.wurstscript.jassAst.JassExpr;
import de.peeeq.wurstscript.jassAst.JassExprlist;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassStatement;
import de.peeeq.wurstscript.jassAst.JassStatements;
import de.peeeq.wurstscript.jassIm.ImCall;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImExprs;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.types.PScriptTypeVoid;
import de.peeeq.wurstscript.types.PscriptType;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.utils.Utils;

public class TLDTranslation {

	

	

	public static void translate(JassGlobalBlock jassGlobalBlock, ImTranslator translator) {
		for (GlobalVarDef g : jassGlobalBlock) {
			ImVar v = translateVar(g);
			translator.addGlobal(v);
		}
	}

	static ImVar translateVar(GlobalVarDef g) {
		// TODO initializer
		return JassIm.ImVar(g.attrTyp().imTranslateType(), g.getName());
	}
	
	public static void translate(GlobalVarDef globalVarDef, ImTranslator translator) {
		translateVar(globalVarDef);
	}

	public static void translate(NativeFunc nativeFunc, ImTranslator translator) {
		// nothing to do
	}

	public static void translate(NativeType nativeType, ImTranslator translator) {
		// nothing to do
	}

	public static void translate(TupleDef tupleDef, ImTranslator translator) {
		// nothing to do
	}

	public static void translate(WPackage pack, ImTranslator translator) {
		for (WEntity e : pack.getElements()) {
			e.imTranslateEntity(translator);
		}
	}

	public static void translate(ClassDef classDef, ImTranslator translator) {
		ClassTranslator.translate(classDef, translator);
	}

	
	public static void translate(FuncDef funcDef, ImTranslator translator) {
		ImFunction f = translator.getFuncFor(funcDef);
		// return type:
		f.setReturnType(funcDef.getReturnTyp().attrTyp().imTranslateType());
		// parameters
		ImHelper.translateParameters(funcDef.getParameters(), f.getParameters());
		// body
		List<ImStmt> stmts = translator.translateStatements(f, funcDef.getBody());
		f.getBody().addAll(stmts);
		
		translator.addFunction(f);
	}
	
	public static void translate(ExtensionFuncDef funcDef, ImTranslator translator) {
		ImFunction f = translator.getFuncFor(funcDef);
		// return type:
		f.setReturnType(funcDef.getReturnTyp().attrTyp().imTranslateType());
		// parameters
		f.getParameters().add(JassIm.ImVar(funcDef.getExtendedType().attrTyp().imTranslateType(), "this"));
		ImHelper.translateParameters(funcDef.getParameters(), f.getParameters());
		// body
		List<ImStmt> stmts = translator.translateStatements(f, funcDef.getBody());
		f.getBody().addAll(stmts);
		
		translator.addFunction(f);
	}
	
	public static void translate(InitBlock initBlock, ImTranslator translator) {
		ImFunction f = translator.getFuncFor(initBlock);
		f.getBody().addAll(translator.translateStatements(f, initBlock.getBody()));
	}
	
	
	private final static class TypeIdComparator implements Comparator<ClassDef> {
		
		private ImTranslator translator;

		public TypeIdComparator(ImTranslator translator) {
			this.translator = translator;
		}
		
		@Override
		public int compare(ClassDef o1, ClassDef o2) {
			int i1 = translator.getTypeId(o1);
			int i2 = translator.getTypeId(o2);
			if (i1 > i2) { 
				return 1;
			} else if (i1 < i2) { 
				return -1;
			}
			return 0;
		}
	}
	

	public static void translate(InterfaceDef interfaceDef, ImTranslator translator) {
		// TODO implement @castable
		
		// get classes implementing this interface
		List<ClassDef> instances = Lists.newArrayList(translator.getWurstProg().attrInterfaceInstances().get(interfaceDef));
		
		// sort instances by typeid
		Collections.sort(instances, new TypeIdComparator(translator));
		
		// create dispatch methods
		for (ClassSlot s: interfaceDef.getSlots()) {
			if (s instanceof FuncDef) {
				translateInterfaceFuncDef(interfaceDef, instances, (FuncDef) s, translator);
			} else {
				throw new Error("not implemented for " + Utils.printElement(s));
			}
		}
	}

	private static void translateInterfaceFuncDef(InterfaceDef interfaceDef, List<ClassDef> instances, FuncDef funcDef, ImTranslator translator) {
		ImFunction f = translator.getFuncFor(funcDef);
		
//		prog.attrComments().put(f, "interface dispatch function " + funcDef.getName() + " for interface " + interfaceDef.getName());
		
//		prog.getFunctions().add(f);
		
		f.setReturnType(funcDef.getReturnTyp().attrTyp().imTranslateType());
		
		f.getParameters().add(JassIm.ImVar(TypesHelper.imIntPair(), "this"));
		
		ImHelper.translateParameters(funcDef.getParameters(), f.getParameters());
		
		f.getBody().addAll(createDispatch(instances, 0, instances.size()-1, funcDef, f, translator));
		
	}

	private static List<ImStmt> createDispatch(List<ClassDef> instances, int start, int end, FuncDef funcDef, ImFunction f, ImTranslator translator) {
		List<ImStmt> result = Lists.newArrayList();
		boolean returnsVoid = funcDef.attrTyp() instanceof PScriptTypeVoid;
		if (start > end) {
			// there seem to be no instances
			assert instances.size() == 0;
			// just create an dummy return
			if (funcDef.getReturnTyp().attrTyp() instanceof PScriptTypeVoid) {
				// empty function
			} else {
				ImType type = f.getReturnType();
				ImExpr def = translator.getDefaultValueForJassType(type);
				result.add(JassIm.ImReturn(def));
			}
			return result;
		} else if (start == end) {
			ClassDef instance = instances.get(start);
			for (NameDef nameDef : instance.attrVisibleNamesPrivate().get(funcDef.getName())) {
				if (nameDef instanceof FuncDef) {
					FuncDef calledFunc = (FuncDef) nameDef;
					ImFunction calledJassFunc = translator.getFuncFor(calledFunc);
					translator.addCallRelation(f, calledJassFunc);
					ImExprs arguments = JassIm.ImExprs();
					for (int i=0; i<f.getParameters().size(); i++) {
						ImExpr arg;
						ImVar p = f.getParameters().get(i);
						if (i == 0) {
							arg = JassIm.ImTupleSelection(JassIm.ImVarAccess(p), 0);
						} else {
							arg = JassIm.ImVarAccess(p);
							// TODO subtyping differences interface vs class?
						}
						arguments.add(arg);
					}
					ImCall call = JassIm.ImCall(calledJassFunc, arguments);
					if (returnsVoid) {
						result.add(call);
					} else {
						result.add(JassIm.ImReturn(call));
					}
					return result;
				}
			}
			throw new CompileError(instance.getSource(), "not really an instance...");
		} else {
			int splitAt = start + (end-start) / 2;
			List<ImStmt> case1 = createDispatch(instances, start, splitAt, funcDef, f, translator);
			List<ImStmt> case2 = createDispatch(instances, splitAt+1, end, funcDef, f, translator);
			
			// if (thistype < instances[splitAt].typeId)
			ImVar thisVar = f.getParameters().get(0);
			ImExpr cond = 
					JassIm.ImCall(translator.getFuncFor(Ast.OpLess()), 
							JassIm.ImExprs(
									JassIm.ImTupleSelection(JassIm.ImVarAccess(thisVar), 1),
									JassIm.ImIntVal(translator.getTypeId(instances.get(splitAt)))));
			ImStmts thenBlock = JassIm.ImStmts(case1);
			ImStmts elseBlock = JassIm.ImStmts(case2);
			result.add(JassIm.ImIf(cond, thenBlock, elseBlock));
			return result;
		}
	}

	
	public static void translate(ModuleDef moduleDef, ImTranslator translator) {
		// nothing to do, only translate module instanciations
	}

	public static void translate(TypeParamDef typeParamDef, ImTranslator translator) {
		// not possible
		throw new Error("invalid AST");
	}

}
