package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeClass;
import de.peeeq.wurstscript.types.WurstTypeUnknown;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SubClasses {


    public static @Nullable ClassDef getExtendedClass(ClassDef classDef) {
        assertNonCyclicClassHierarchy(classDef, new ArrayList<>());

        if (classDef.getExtendedClass().attrTyp() instanceof WurstTypeClass) {
            WurstTypeClass c = (WurstTypeClass) classDef.getExtendedClass().attrTyp();
            if (classDef == c.getClassDef()) {
                // using an exception, because cyclic dependencies can cause stack overflow later
                throw new CompileError(classDef.getSource(), "Classes must not extend themselves");
            }
            return c.getClassDef();
        } else if (classDef.getExtendedClass() instanceof TypeExpr) {
            classDef.getExtendedClass().addError("Cannot extend " + classDef.getExtendedClass().attrTyp() + ", because it is not a class.");
        }
        return null;
    }

    private static void assertNonCyclicClassHierarchy(ClassDef c, List<ClassDef> hierarchy) {
        if (hierarchy.contains(c)) {
            ClassDef start = hierarchy.get(0);
            throw new CompileError(start.getSource(), "Class " + start.getName() + " has a cyclic class hierarchy: " +
                    hierarchy.stream().map(ClassDef::getName).collect(Collectors.joining(" < ")));
        }
        OptTypeExpr ext = c.getExtendedClass();
        if (ext instanceof TypeExpr) {
            TypeExpr extT = (TypeExpr) ext;
            if (extT.attrTypeDef() instanceof ClassDef) {
                ClassDef extC = (ClassDef) extT.attrTypeDef();
                hierarchy.add(c);
                assertNonCyclicClassHierarchy(extC, hierarchy);
            }
        }
    }

    public static @Nullable ConstructorDef getSuperConstructor(ConstructorDef constr) {
        ClassDef c = constr.attrNearestClassDef();
        if (c == null) {
            return null;
        }
        ClassDef superClass = c.attrExtendedClass();
        if (superClass == null) {
            return null;
        }
        // call super constructor
        ConstructorDefs constructors = superClass.getConstructors();
        ConstructorDef superConstr = OverloadingResolver.resolveSuperCall(constructors, constr);
        return superConstr;
    }

    public static WurstType getExtendedClassType(ClassDef c) {
        ClassDef ec = c.attrExtendedClass();
        if (ec != null) {
            return ec.attrTyp().dynamic();
        }
        return WurstTypeUnknown.instance();
    }

}
