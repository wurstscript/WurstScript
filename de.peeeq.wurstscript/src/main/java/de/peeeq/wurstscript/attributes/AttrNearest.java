package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;
import org.eclipse.jdt.annotation.Nullable;

public class AttrNearest {


    public static @Nullable PackageOrGlobal nearestPackage(@Nullable Element node) {
        if (node == null) {
            return null;
        }
        if (node instanceof ModuleInstanciation) {
            ModuleInstanciation mi = (ModuleInstanciation) node;
            ModuleDef m = mi.attrModuleOrigin();
            if (m == null) {
                return null;
            }
            return m.attrNearestPackage();
        }
        if (node instanceof PackageOrGlobal) {
            return (PackageOrGlobal) node;
        }
        if (node.getParent() == null) {
            return null;
        }
        return node.getParent().attrNearestPackage();
    }

    public static @Nullable ClassDef nearestClassDef(@Nullable Element node) {
        if (node == null) {
            return null;
        }
        if (node instanceof ClassDef) {
            return (ClassDef) node;
        }
        if (node.getParent() == null) {
            return null;
        }
        return node.getParent().attrNearestClassDef();
    }


    public static @Nullable FunctionImplementation nearestFuncDef(@Nullable Element node) {
        if (node == null) {
            return null;
        }
        if (node instanceof FunctionImplementation) {
            return (FunctionImplementation) node;
        }
        if (node.getParent() == null) {
            return null;
        }
        return node.getParent().attrNearestFuncDef();
    }

    public static @Nullable ClassOrModule nearestClassOrModule(@Nullable Element node) {
        if (node == null) {
            return null;
        }
        if (node instanceof ClassOrModule) {
            return (ClassOrModule) node;
        }
        if (node.getParent() == null) {
            return null;
        }
        return node.getParent().attrNearestClassOrModule();
    }

    public static ClassOrInterface nearestClassOrInterface(Element node) {
        while (node != null) {
            if (node instanceof ClassOrInterface) {
                return ((ClassOrInterface) node);
            }
            node = node.getParent();
        }
        return null;
    }


    public static @Nullable NamedScope nearestNamedScope(@Nullable Element node) {
        if (node == null) {
            return null;
        }
        if (node instanceof NamedScope) {
            return (NamedScope) node;
        }
        if (node.getParent() == null) {
            return null;
        }
        return node.getParent().attrNearestNamedScope();
    }

    public static @Nullable WScope nearestScope(Element e) {
        if (e instanceof WScope) {
            return (WScope) e;
        }
        if (e.getParent() == null) {
            return null;
        }
        return e.getParent().attrNearestScope();
    }

    public static @Nullable WScope nextScope(WScope scope) {
        if (scope instanceof ModuleInstanciation) {
            ModuleInstanciation mi = (ModuleInstanciation) scope;
            ModuleDef m = mi.attrModuleOrigin();
            if (m == null) {
                return null;
            }
            return m.attrNextScope();
        } else {
            if (scope.getParent() != null) {
                return scope.getParent().attrNearestScope();
            } else {
                return null;
            }
        }
    }

    public static @Nullable StructureDef nearestStructureDef(Element e) {
        if (e instanceof StructureDef) {
            return (StructureDef) e;
        } else if (e.getParent() != null) {
            return e.getParent().attrNearestStructureDef();
        } else {
            return null;
        }
    }

    public static @Nullable CompilationUnit nearestCompilationUnit(Element e) {
        if (e instanceof CompilationUnit) {
            return (CompilationUnit) e;
        } else if (e.getParent() != null) {
            return e.getParent().attrCompilationUnit();
        } else {
            return null;
        }
    }

    public static @Nullable ExprClosure nearestExprClosure(@Nullable Element e) {
        while (e != null) {
            if (e instanceof ExprClosure) {
                return (ExprClosure) e;
            }
            e = e.getParent();
        }
        return null;
    }

    public static @Nullable ExprStatementsBlock nearestExprStatementsBlock(@Nullable Element e) {
        while (e != null) {
            if (e instanceof ExprStatementsBlock) {
                return (ExprStatementsBlock) e;
            }
            e = e.getParent();
        }
        return null;
    }


}
