package de.peeeq.wurstscript.translation.imtranslation;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.StructureDef;
import de.peeeq.wurstscript.ast.TypeExpr;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImMethod;

public class InterfaceTranslator {

	private InterfaceDef interfaceDef;
	private ImTranslator translator;
	private ImClass imClass;

	public InterfaceTranslator(InterfaceDef interfaceDef, ImTranslator translator) {
		this.interfaceDef = interfaceDef;
		this.translator = translator;
		imClass = translator.getClassFor(interfaceDef);
	}

	public void translate() {
		translator.getImProg().getClasses().add(imClass);
		
		// set super-classes
		for (TypeExpr ext : interfaceDef.getExtendsList()) {
			if (ext.attrTypeDef() instanceof StructureDef) {
				StructureDef sup = (StructureDef) ext.attrTypeDef();
				imClass.getSuperClasses().add(translator.getClassFor(sup));
			}
		}
		
		// create dispatch methods
		for (FuncDef f: interfaceDef.getMethods()) {
			translateInterfaceFuncDef(f);
		}
		
		// add destroy method
		addDestroyMethod();
	}

	public void addDestroyMethod() {
		ImMethod m = translator.destroyMethod.getFor(interfaceDef);
		imClass.getMethods().add(m);
		
		List<ClassDef> subClasses = Lists.newArrayList(translator.getInterfaceInstances(interfaceDef));
		
		// set sub methods
		for (ClassDef sc : subClasses) {
			ImMethod dm = translator.destroyMethod.getFor(sc);
			m.getSubMethods().add(dm);
		}
	}

	private void translateInterfaceFuncDef(FuncDef f) {
		ImMethod imMeth = translator.getMethodFor(f);
		ImFunction imFunc = translator.getFuncFor(f);
		imClass.getMethods().add(imMeth);
		
		// translate implementation
		if (f.attrHasEmptyBody()) {
			imMeth.setIsAbstract(true);
		} else {
			// there is a default implementation
			imFunc.getBody().addAll(translator.translateStatements(imFunc, f.getBody()));
		}
		
		
		List<ClassDef> subClasses = Lists.newArrayList(translator.getInterfaceInstances(interfaceDef));
		// TODO also add extended interfaces
		
		// set sub methods
		Map<ClassDef, FuncDef> subClasses2 = translator.getClassesWithImplementation(subClasses, f);
		for (FuncDef subM : subClasses2.values()) {
			imMeth.getSubMethods().add(translator.getMethodFor(subM));
		}
		
	}

}
