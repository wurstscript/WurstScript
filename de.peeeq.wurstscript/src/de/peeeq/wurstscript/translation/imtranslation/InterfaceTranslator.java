package de.peeeq.wurstscript.translation.imtranslation;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.ast.InterfaceDef;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.Op;
import de.peeeq.wurstscript.ast.OpLessEq;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassIm.ImCall;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImExprs;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImTupleType;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.JassIm;
import static de.peeeq.wurstscript.jassIm.JassIm.*;
import de.peeeq.wurstscript.types.PScriptTypeVoid;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.utils.Pair;
import de.peeeq.wurstscript.utils.Utils;

public class InterfaceTranslator {

	private InterfaceDef interfaceDef;
	private ImTranslator translator;

	public InterfaceTranslator(InterfaceDef interfaceDef, ImTranslator translator) {
		this.interfaceDef = interfaceDef;
		this.translator = translator;
	}

	public void translate() {
		// TODO implement @castable

		// get classes implementing this interface
		List<ClassDef> instances = Lists.newArrayList(translator.getWurstProg().attrInterfaceInstances().get(interfaceDef));

		// sort instances by typeid
		Collections.sort(instances, new TypeIdComparator(translator));

		System.out.println("instances = ");
		for (ClassDef i : instances) {
			System.out.println("	 " + i.getName());
		}
		
		// create dispatch methods
		for (FuncDef f: interfaceDef.getMethods()) {
			translateInterfaceFuncDef(interfaceDef, instances, f);
		}
	}

	private void translateInterfaceFuncDef(InterfaceDef interfaceDef, List<ClassDef> instances, FuncDef funcDef) {
		ImFunction f = translator.getFuncFor(funcDef);
		List<Pair<ClassDef, FuncDef>> instances2 = translator.getClassedWithImplementation(instances, funcDef);
		if (instances2.size() > 0) {
			f.getBody().addAll(translator.createDispatch(instances2, 0, instances2.size()-1, funcDef, f, false, new TypeIdGetterImpl()));
		}
		if (funcDef.getBody().size() > 0) {
			// TODO add default implementation
			f.getBody().addAll(translator.translateStatements(f, funcDef.getBody()));
		} else {
			if (!(funcDef.attrTyp() instanceof PScriptTypeVoid)) {
				String msg = "ERROR: invalid type for interface dispatch";
				f.getBody().add(JassIm.ImFunctionCall(translator.getDebugPrintFunc(), ImExprs(ImStringVal(msg))));
				// add return statement
				ImType type = f.getReturnType();
				ImExpr def = translator.getDefaultValueForJassType(type);
				f.getBody().add(JassIm.ImReturn(def));
			}
		}
	}

	
	private class TypeIdGetterImpl implements TypeIdGetter {
		@Override
		public ImExpr get(ImVar thisVar) {
			return JassIm.ImTupleSelection(JassIm.ImVarAccess(thisVar), 1);
		}
	}
	

}
