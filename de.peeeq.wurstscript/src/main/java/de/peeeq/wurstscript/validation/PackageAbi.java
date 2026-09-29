package de.peeeq.wurstscript.validation;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.ModifiersHelper;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PackageAbi {

    /**
     * Computes a SHA-256 hash representing the public ABI of the given WPackage.
     * Implementation details (function/constructor bodies, private members, local variables)
     * are excluded. If this hash is identical across edits, downstream dependent packages
     * do not need to be invalidated.
     */
    public static String computeAbiHash(WPackage p) {
        StringBuilder sb = new StringBuilder();
        sb.append("package:").append(p.getName()).append("\n");

        // Imports
        List<WImport> imports = p.getImports().stream()
            .sorted(Comparator.comparing(WImport::getPackagename))
            .collect(Collectors.toList());
        for (WImport imp : imports) {
            sb.append("import:").append(imp.getIsPublic() ? "pub:" : "priv:")
              .append(imp.getPackagename()).append("\n");
        }

        // Public elements in package
        for (WEntity elem : p.getElements()) {
            if (elem instanceof FuncDef) {
                FuncDef f = (FuncDef) elem;
                if (ModifiersHelper.isPublic(f)) {
                    appendFuncDef(sb, f);
                }
            } else if (elem instanceof ExtensionFuncDef) {
                ExtensionFuncDef f = (ExtensionFuncDef) elem;
                if (ModifiersHelper.isPublic(f)) {
                    appendExtensionFuncDef(sb, f);
                }
            } else if (elem instanceof GlobalVarDef) {
                GlobalVarDef v = (GlobalVarDef) elem;
                if (ModifiersHelper.isPublic(v)) {
                    appendGlobalVar(sb, v);
                }
            } else if (elem instanceof ClassDef) {
                ClassDef c = (ClassDef) elem;
                if (ModifiersHelper.isPublic(c)) {
                    appendClassDef(sb, c);
                }
            } else if (elem instanceof InterfaceDef) {
                InterfaceDef inf = (InterfaceDef) elem;
                if (ModifiersHelper.isPublic(inf)) {
                    appendInterfaceDef(sb, inf);
                }
            } else if (elem instanceof TupleDef) {
                TupleDef t = (TupleDef) elem;
                if (ModifiersHelper.isPublic(t)) {
                    appendTupleDef(sb, t);
                }
            } else if (elem instanceof EnumDef) {
                EnumDef ed = (EnumDef) elem;
                if (ModifiersHelper.isPublic(ed)) {
                    appendEnumDef(sb, ed);
                }
            } else if (elem instanceof NativeFunc) {
                NativeFunc nf = (NativeFunc) elem;
                if (ModifiersHelper.isPublic(nf)) {
                    appendNativeFunc(sb, nf);
                }
            } else if (elem instanceof NativeType) {
                NativeType nt = (NativeType) elem;
                if (ModifiersHelper.isPublic(nt)) {
                    appendNativeType(sb, nt);
                }
            } else if (elem instanceof ModuleDef) {
                ModuleDef m = (ModuleDef) elem;
                if (ModifiersHelper.isPublic(m)) {
                    appendModuleDef(sb, m);
                }
            }
        }

        Hasher hasher = Hashing.sha256().newHasher();
        hasher.putString(sb.toString(), StandardCharsets.UTF_8);
        return hasher.hash().toString();
    }

    private static void appendFuncDef(StringBuilder sb, FuncDef f) {
        sb.append("func:").append(f.getName());
        appendModifiers(sb, f.getModifiers());
        for (TypeParamDef tp : f.getTypeParameters()) {
            sb.append(":tp:").append(tp.getName());
        }
        for (WParameter param : f.getParameters()) {
            sb.append(":p:").append(param.getName()).append(":").append(typeExprToString(param.getTyp()));
        }
        sb.append(":ret:").append(typeExprToString(f.getReturnTyp())).append("\n");
    }

    private static void appendExtensionFuncDef(StringBuilder sb, ExtensionFuncDef f) {
        sb.append("extfunc:").append(typeExprToString(f.getExtendedType())).append(":").append(f.getName());
        appendModifiers(sb, f.getModifiers());
        for (TypeParamDef tp : f.getTypeParameters()) {
            sb.append(":tp:").append(tp.getName());
        }
        for (WParameter param : f.getParameters()) {
            sb.append(":p:").append(param.getName()).append(":").append(typeExprToString(param.getTyp()));
        }
        sb.append(":ret:").append(typeExprToString(f.getReturnTyp())).append("\n");
    }

    private static void appendGlobalVar(StringBuilder sb, GlobalVarDef v) {
        sb.append("var:").append(v.getName()).append(":")
          .append(ModifiersHelper.isConstant(v) ? "const:" : "var:")
          .append(typeExprToString(v.getOptTyp())).append("\n");
    }

    private static void appendClassDef(StringBuilder sb, ClassDef c) {
        sb.append("class:").append(c.getName());
        if (ModifiersHelper.isAbstract(c)) {
            sb.append(":abstract");
        }
        for (TypeParamDef tp : c.getTypeParameters()) {
            sb.append(":tp:").append(tp.getName());
        }
        sb.append(":ext:").append(typeExprToString(c.getExtendedClass()));
        for (TypeExpr imp : c.getImplementsList()) {
            sb.append(":imp:").append(typeExprToString(imp));
        }
        sb.append("\n");
        for (ConstructorDef constr : c.getConstructors()) {
            if (ModifiersHelper.isPublic(constr)) {
                sb.append("  construct");
                for (WParameter param : constr.getParameters()) {
                    sb.append(":p:").append(param.getName()).append(":").append(typeExprToString(param.getTyp()));
                }
                sb.append("\n");
            }
        }
        for (FuncDef m : c.getMethods()) {
            if (!ModifiersHelper.isPrivate(m)) {
                sb.append("  ");
                appendFuncDef(sb, m);
            }
        }
        for (GlobalVarDef v : c.getVars()) {
            if (!ModifiersHelper.isPrivate(v)) {
                sb.append("  ");
                appendGlobalVar(sb, v);
            }
        }
    }

    private static void appendInterfaceDef(StringBuilder sb, InterfaceDef inf) {
        sb.append("interface:").append(inf.getName());
        for (TypeParamDef tp : inf.getTypeParameters()) {
            sb.append(":tp:").append(tp.getName());
        }
        for (TypeExpr ext : inf.getExtendsList()) {
            sb.append(":ext:").append(typeExprToString(ext));
        }
        sb.append("\n");
        for (FuncDef m : inf.getMethods()) {
            sb.append("  ");
            appendFuncDef(sb, m);
        }
    }

    private static void appendTupleDef(StringBuilder sb, TupleDef t) {
        sb.append("tuple:").append(t.getName());
        for (WParameter param : t.getParameters()) {
            sb.append(":p:").append(param.getName()).append(":").append(typeExprToString(param.getTyp()));
        }
        sb.append("\n");
    }

    private static void appendEnumDef(StringBuilder sb, EnumDef ed) {
        sb.append("enum:").append(ed.getName());
        for (EnumMember m : ed.getMembers()) {
            sb.append(":").append(m.getName());
        }
        sb.append("\n");
    }

    private static void appendNativeFunc(StringBuilder sb, NativeFunc nf) {
        sb.append("nativefunc:").append(nf.getName());
        for (WParameter param : nf.getParameters()) {
            sb.append(":p:").append(param.getName()).append(":").append(typeExprToString(param.getTyp()));
        }
        sb.append(":ret:").append(typeExprToString(nf.getReturnTyp())).append("\n");
    }

    private static void appendNativeType(StringBuilder sb, NativeType nt) {
        sb.append("nativetype:").append(nt.getName()).append(":").append(typeExprToString(nt.getOptTyp())).append("\n");
    }

    private static void appendModuleDef(StringBuilder sb, ModuleDef m) {
        sb.append("module:").append(m.getName());
        for (TypeParamDef tp : m.getTypeParameters()) {
            sb.append(":tp:").append(tp.getName());
        }
        sb.append("\n");
        // Modules are expanded directly into using classes; any changes to the module AST affect instantiating classes.
        sb.append(de.peeeq.wurstscript.utils.Utils.prettyPrint(m));
        sb.append("\n");
    }

    private static void appendModifiers(StringBuilder sb, Modifiers modifiers) {
        for (Modifier m : modifiers) {
            if (m instanceof ModConstant) {
                sb.append(":const");
            } else if (m instanceof ModOverride) {
                sb.append(":override");
            } else if (m instanceof ModAbstract) {
                sb.append(":abstract");
            } else if (m instanceof ModVararg) {
                sb.append(":vararg");
            } else if (m instanceof Annotation) {
                sb.append(":ann:").append(((Annotation) m).getAnnotationType());
            }
        }
    }

    public static String typeExprToString(OptTypeExpr optType) {
        if (optType == null || optType instanceof NoTypeExpr) {
            return "void";
        }
        if (optType instanceof TypeExprSimple) {
            TypeExprSimple s = (TypeExprSimple) optType;
            StringBuilder sb = new StringBuilder();
            if (s.getScopeType() instanceof TypeExpr) {
                sb.append(typeExprToString(s.getScopeType())).append(".");
            }
            sb.append(s.getTypeName());
            if (!s.getTypeArgs().isEmpty()) {
                sb.append("<");
                for (int i = 0; i < s.getTypeArgs().size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append(typeExprToString(s.getTypeArgs().get(i)));
                }
                sb.append(">");
            }
            return sb.toString();
        }
        if (optType instanceof TypeExprArray) {
            TypeExprArray a = (TypeExprArray) optType;
            return typeExprToString(a.getBase()) + "[]";
        }
        if (optType instanceof TypeExprThis) {
            return "this";
        }
        if (optType instanceof TypeExprResolved) {
            return ((TypeExprResolved) optType).getResolvedType().toString();
        }
        return optType.getClass().getSimpleName();
    }
}
