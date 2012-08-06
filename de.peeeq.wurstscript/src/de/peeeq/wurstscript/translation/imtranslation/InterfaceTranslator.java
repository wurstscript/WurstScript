package de.peeeq.wurstscript.translation.imtranslation;

import static de.peeeq.wurstscript.jassIm.JassIm.ImExprs;
import static de.peeeq.wurstscript.jassIm.JassIm.ImStringVal;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.types.PScriptTypeVoid;
import de.peeeq.wurstscript.utils.Pair;

public class InterfaceTranslator {

	private InterfaceDef interfaceDef;
	private ImTranslator translator;
	private ClassManagementVars m;

	public InterfaceTranslator(InterfaceDef interfaceDef, ImTranslator translator) {
		this.interfaceDef = interfaceDef;
		this.translator = translator;
		m = translator.getClassManagementVarsFor(interfaceDef);
	}

	public void translate() {
		// TODO implement @castable

		// get classes implementing this interface
		List<ClassDef> instances = Lists.newArrayList(translator.getInterfaceInstances(interfaceDef));

		// sort instances by typeid
		Collections.sort(instances, new TypeIdComparator(translator));

		// create dispatch methods
		for (FuncDef f: interfaceDef.getMethods()) {
			translateInterfaceFuncDef(interfaceDef, instances, f);
		}
	}

	private void translateInterfaceFuncDef(InterfaceDef interfaceDef, List<ClassDef> instances, FuncDef funcDef) {
		AstElement trace = funcDef;
		ImFunction f = translator.getFuncFor(funcDef);
		Map<ClassDef, FuncDef> instances2 = translator.getClassedWithImplementation(instances, funcDef);
		if (instances2.size() > 0) {
			int maxTypeId = translator.getMaxTypeId(instances);
			f.getBody().addAll(translator.createDispatch(instances2, funcDef, f, maxTypeId, new TypeIdGetterImpl()));
		}
		if (funcDef.getBody().size() > 0) {
			// TODO add default implementation
			f.getBody().addAll(translator.translateStatements(f, funcDef.getBody()));
		} else {
			// create dynamic message when not matched:
			String msg = "ERROR: invalid type for interface dispatch when calling " + interfaceDef.getName() + "." + funcDef.getName();
			
			f.getBody().add(JassIm.ImFunctionCall(trace, translator.getDebugPrintFunc(), ImExprs(ImStringVal(msg))));
			if (!(funcDef.attrTyp() instanceof PScriptTypeVoid)) {
				// add return statement
				ImType type = f.getReturnType();
				ImExpr def = translator.getDefaultValueForJassType(type);
				f.getBody().add(JassIm.ImReturn(trace, def));
			}
		}
	}

	
	private class TypeIdGetterImpl implements TypeIdGetter {
		@Override
		public ImExpr get(ImVar thisVar) {
			return JassIm.ImVarArrayAccess(m.typeId, JassIm.ImVarAccess(thisVar));
		}
	}
	

}
