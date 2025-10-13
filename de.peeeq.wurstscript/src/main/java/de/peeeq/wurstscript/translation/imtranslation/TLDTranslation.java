package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.types.WurstTypeClass;

import java.util.List;
import java.util.ArrayList;

public class TLDTranslation {

    public static void translate(JassGlobalBlock jassGlobalBlock, ImTranslator translator) {
        // Batch process all globals to reduce overhead
        List<GlobalVarDef> globals = new ArrayList<>();
        for (GlobalVarDef g : jassGlobalBlock) {
            globals.add(g);
        }

        // Process in batch to improve cache locality
        for (GlobalVarDef g : globals) {
            translateVar(g, translator);
        }
    }

    static ImVar translateVar(GlobalVarDef g, ImTranslator translator) {
        ImVar v = translator.getVarFor(g);
        translator.addGlobal(v);
        translator.addGlobalInitalizer(v, g.attrNearestPackage(), g.getInitialExpr());
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

        // Batch process imports
        List<WPackage> packagesToTranslate = new ArrayList<>();
        for (WImport imp : pack.getImports()) {
            WPackage p = imp.attrImportedPackage();
            if (p != null) {
                packagesToTranslate.add(p);
            }
        }

        for (WPackage p : packagesToTranslate) {
            p.imTranslateTLD(translator);
        }

        // Batch process elements by type for better cache locality
        List<WEntity> elements = new ArrayList<>();
        for (WEntity e : pack.getElements()) {
            elements.add(e);
        }

        // Process elements
        for (WEntity e : elements) {
            translator.lasttranslatedThing = e;
            e.imTranslateEntity(translator);
        }
    }

    public static void translate(ClassDef classDef, ImTranslator translator) {
        if (translator.isTranslated(classDef)) {
            return;
        }

        // Cache type lookup
        WurstTypeClass ct = classDef.attrTypC();
        WurstTypeClass extendedClass = ct.extendedClass();
        if (extendedClass != null) {
            translate(extendedClass.getClassDef(), translator);
        }

        ClassTranslator.translate(classDef, translator);
        translator.setTranslated(classDef);
    }

    public static void translate(FuncDef funcDef, ImTranslator translator) {
        // Cache function lookup - this is a hotspot
        ImFunction f = translator.getFuncFor(funcDef);

        // Pre-allocate body list with estimated capacity if possible
        List<ImStmt> stmts = translator.translateStatements(f, funcDef.getBody());

        // Use addAll for batch addition
        if (!stmts.isEmpty()) {
            f.getBody().addAll(stmts);
        }

        // Cache package lookup
        if (funcDef.attrNearestPackage() instanceof CompilationUnit) {
            String funcName = funcDef.getName();
            if ("main".equals(funcName)) {
                translator.setMainFunc(f);
            } else if ("config".equals(funcName)) {
                translator.setConfigFunc(f);
            }
        }
    }

    public static void translate(ExtensionFuncDef funcDef, ImTranslator translator) {
        // Cache function lookup - this is a hotspot
        ImFunction f = translator.getFuncFor(funcDef);

        // Translate statements once and add in batch
        List<ImStmt> stmts = translator.translateStatements(f, funcDef.getBody());
        if (!stmts.isEmpty()) {
            f.getBody().addAll(stmts);
        }
    }

    public static void translate(InitBlock initBlock, ImTranslator translator) {
        // Cache function lookup
        WPackage nearestPackage = (WPackage) initBlock.attrNearestPackage();
        ImFunction f = translator.getInitFuncFor(nearestPackage);

        // Translate and add statements in batch
        List<ImStmt> stmts = translator.translateStatements(f, initBlock.getBody());
        if (!stmts.isEmpty()) {
            f.getBody().addAll(stmts);
        }
    }

    public static void translate(InterfaceDef interfaceDef, ImTranslator translator) {
        new InterfaceTranslator(interfaceDef, translator).translate();
    }

    public static void translate(ModuleDef moduleDef, ImTranslator translator) {
        // nothing to do, only translate module instantiations
    }

    public static void translate(TypeParamDef typeParamDef, ImTranslator translator) {
        // not possible
        throw new Error("invalid AST");
    }

    public static void translate(EnumDef enumDef, ImTranslator translator) {
        // nothing to do
    }

    public static void translate(ModuleInstanciation moduleInstanciation, ImTranslator translator) {
        // nothing to do?
    }
}
