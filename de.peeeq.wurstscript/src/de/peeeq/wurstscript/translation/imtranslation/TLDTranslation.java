package de.peeeq.wurstscript.translation.imtranslation;

import static de.peeeq.wurstscript.jassAst.JassAst.JassExprBinary;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprIntVal;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprVarAccess;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtCall;
import static de.peeeq.wurstscript.jassAst.JassAst.JassStmtReturn;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.ClassSlot;
import de.peeeq.wurstscript.ast.CompilationUnit;
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
import de.peeeq.wurstscript.ast.PackageOrGlobal;
import de.peeeq.wurstscript.ast.TupleDef;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WImport;
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
			translateVar(g, translator);
		}
	}

	static ImVar translateVar(GlobalVarDef g, ImTranslator translator) {
		ImVar v = translator.getVarFor(g); 
		translator.addGlobalInitalizer(v, g.attrNearestPackage(), g.getInitialExpr());
		translator.addGlobal(v);
		return v;
	}
	
	public static void translate(GlobalVarDef globalVarDef, ImTranslator translator) {
		translateVar(globalVarDef, translator);
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
		if (translator.isTranslated(pack)) {
			return;
		}
		translator.setTranslated(pack);
		
		// first translate all packages used by this package
		for (WImport imp : pack.getImports()) {
			imp.attrImportedPackage().imTranslateTLD(translator);
		}
		
		// translate the package itself
		for (WEntity e : pack.getElements()) {
			e.imTranslateEntity(translator);
		}
	}

	public static void translate(ClassDef classDef, ImTranslator translator) {
		if (translator.isTranslated(classDef)) {
			return;
		}
		if (classDef.attrExtendedClass() != null) {
			// first translate super classes:
			translate(classDef.attrExtendedClass(), translator);
		}
		ClassTranslator.translate(classDef, translator);
		translator.setTranslated(classDef);
	}

	
	public static void translate(FuncDef funcDef, ImTranslator translator) {
		ImFunction f = translator.getFuncFor(funcDef);
		
		// body
		List<ImStmt> stmts = translator.translateStatements(f, funcDef.getBody());
		f.getBody().addAll(stmts);
		
		if (funcDef.attrNearestPackage() instanceof CompilationUnit) {
			if (funcDef.getName().equals("main")) {
				translator.setMainFunc(f);
			} else if (funcDef.getName().equals("config")) {
				translator.setConfigFunc(f);
			}
		}
	}
	
	public static void translate(ExtensionFuncDef funcDef, ImTranslator translator) {
		ImFunction f = translator.getFuncFor(funcDef);
		// body
		List<ImStmt> stmts = translator.translateStatements(f, funcDef.getBody());
		f.getBody().addAll(stmts);
	}
	
	public static void translate(InitBlock initBlock, ImTranslator translator) {
		ImFunction f = translator.getInitFuncFor((WPackage) initBlock.attrNearestPackage());
		f.getBody().addAll(translator.translateStatements(f, initBlock.getBody()));
	}
	
	
	public static void translate(InterfaceDef interfaceDef, ImTranslator translator) {
		new InterfaceTranslator(interfaceDef, translator).translate();
		
	}

	
	public static void translate(ModuleDef moduleDef, ImTranslator translator) {
		// nothing to do, only translate module instanciations
	}

	public static void translate(TypeParamDef typeParamDef, ImTranslator translator) {
		// not possible
		throw new Error("invalid AST");
	}

}
