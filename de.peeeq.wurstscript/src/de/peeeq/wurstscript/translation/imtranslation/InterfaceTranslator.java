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

		// create dispatch methods
		for (FuncDef f: interfaceDef.getMethods()) {
			translateInterfaceFuncDef(interfaceDef, instances, f);
		}
	}

	private void translateInterfaceFuncDef(InterfaceDef interfaceDef, List<ClassDef> instances, FuncDef funcDef) {
		ImFunction f = translator.getFuncFor(funcDef);
		List<Pair<ClassDef, FuncDef>> instances2 = getClassedWithImplementation(instances, funcDef);
		f.getBody().addAll(createDispatch(instances2, 0, instances2.size()-1, funcDef, f, false));
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

	private List<ImStmt> createDispatch(List<Pair<ClassDef, FuncDef>> instances, int start, int end
			, FuncDef funcDef, ImFunction f, boolean equalityKnown) {
		
		
		List<ImStmt> result = Lists.newArrayList();
		ImVar thisVar = f.getParameters().get(0);
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
			ClassDef instance = instances.get(start).getA();
			FuncDef calledFunc = instances.get(start).getB();
			ImFunction calledJassFunc = translator.getFuncFor(calledFunc);
			translator.addCallRelation(f, calledJassFunc);
			ImExprs arguments = JassIm.ImExprs();
			for (int i=0; i<f.getParameters().size(); i++) {
				ImExpr arg;
				ImVar p = f.getParameters().get(i);
				ImVar expected = calledJassFunc.getParameters().get(i);
				if (expected.getType() instanceof ImSimpleType
						&& p.getType() instanceof ImTupleType) {
					// class type expected but got interface type 
					// ==> select only first part 
					arg = JassIm.ImTupleSelection(JassIm.ImVarAccess(p), 0);
				} else {
					arg = JassIm.ImVarAccess(p);
					// TODO subtyping differences interface vs class?
				}
				arguments.add(arg);
			}
			ImCall call = JassIm.ImFunctionCall(calledJassFunc, arguments);
			if (returnsVoid) {
				result.add(call);
			} else {
				result.add(JassIm.ImReturn(call));
			}
			if (!equalityKnown) {
				// check for equality
				ImExpr condition = JassIm.ImOperatorCall(Ast.OpEquals(), 
						JassIm.ImExprs(
								JassIm.ImTupleSelection(JassIm.ImVarAccess(thisVar), 1),
								JassIm.ImIntVal(translator.getTypeId(instances.get(start).getA()))));;
				
				return Collections.<ImStmt>singletonList(
						JassIm.ImIf(condition, ImStmts(result), ImStmts())
					);
			} else {
				return result;
			}
		} else {
			int splitAt = start + (end-start) / 2;
			
			boolean eq = false;
			System.out.println("splitAt - start = " + (splitAt - start));
			if (splitAt - start == 0) {
				// if we only have one element at the left side, we can already check for equality
				eq = true;
			}
			
			List<ImStmt> case1 = createDispatch(instances, start, splitAt, funcDef, f, eq);
			List<ImStmt> case2 = createDispatch(instances, splitAt+1, end, funcDef, f, false);

			// if (thistype <= instances[splitAt].typeId)
			ImExpr cond = 
					JassIm.ImOperatorCall(eq ? Ast.OpEquals() : Ast.OpLessEq(), 
							JassIm.ImExprs(
									JassIm.ImTupleSelection(JassIm.ImVarAccess(thisVar), 1),
									JassIm.ImIntVal(translator.getTypeId(instances.get(splitAt).getA()))));
			ImStmts thenBlock = JassIm.ImStmts(case1);
			ImStmts elseBlock = JassIm.ImStmts(case2);
			result.add(JassIm.ImIf(cond, thenBlock, elseBlock));
			return result;
		}
	}

	/**
	 * returns a list of classes and functions implementing funcDef
	 */
	private List<Pair<ClassDef, FuncDef>> getClassedWithImplementation(
			List<ClassDef> instances, FuncDef funcDef) {
		List<Pair<ClassDef, FuncDef>> result = Lists.newArrayListWithCapacity(instances.size());
		for (ClassDef c : instances) {
			for (NameDef nameDef : c.attrVisibleNamesPrivate().get(funcDef.getName())) {
				if (nameDef instanceof FuncDef) {
					FuncDef calledFunc = (FuncDef) nameDef;
					result.add(Pair.create(c, calledFunc));
				}
			}
		}
		return result;
	}

}
