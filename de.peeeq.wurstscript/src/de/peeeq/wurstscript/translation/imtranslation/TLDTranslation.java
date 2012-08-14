package de.peeeq.wurstscript.translation.imtranslation;

import java.util.List;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.EnumDef;
import de.peeeq.wurstscript.ast.ExtensionFuncDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.ast.InitBlock;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.JassGlobalBlock;
import de.peeeq.wurstscript.ast.ModuleDef;
import de.peeeq.wurstscript.ast.NativeFunc;
import de.peeeq.wurstscript.ast.NativeType;
import de.peeeq.wurstscript.ast.TupleDef;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.WEntity;
import de.peeeq.wurstscript.ast.WImport;
import de.peeeq.wurstscript.ast.WPackage;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImVar;

public class TLDTranslation {

	

	public static void translate(JassGlobalBlock jassGlobalBlock, ImTranslator translator) {
		for (GlobalVarDef g : jassGlobalBlock) {
			translateVar(g, translator);
		}
	}

	static ImVar translateVar(GlobalVarDef g, ImTranslator translator) {
		ImVar v = translator.getVarFor(g); 
		translator.addGlobal(v);
		if (!v.getIsBJ()) {
			// add init code for non-BJ variables
			translator.addGlobalInitalizer(v, g.attrNearestPackage(), g.getInitialExpr());
		}
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
		
		if (funcDef.attrIsCompiletime()) {
			translator.addCompiletimeFunc(f);
		}
		
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

	public static void translate(EnumDef enumDef, ImTranslator translator) {
		// nothing to do
		
	}

}
