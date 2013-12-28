package de.peeeq.wurstscript.translation.imtranslation;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.ExprClosure;
import de.peeeq.wurstscript.ast.FuncDef;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImMethod;
import de.peeeq.wurstscript.jassIm.ImMethods;
import de.peeeq.wurstscript.jassIm.ImSet;
import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImTupleType;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVarAccess;
import de.peeeq.wurstscript.jassIm.ImVars;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeBool;
import de.peeeq.wurstscript.types.WurstTypeClass;
import de.peeeq.wurstscript.types.WurstTypeCode;
import de.peeeq.wurstscript.types.WurstTypeInt;
import de.peeeq.wurstscript.types.WurstTypeInterface;
import de.peeeq.wurstscript.types.WurstTypeVoid;

public class ClosureTranslator {
	private final ExprClosure e;
	private final ImTranslator tr;
	private final ImFunction f;
	
	private final Map<ImVar, ImVar> closureVars = Maps.newLinkedHashMap();
	private ImFunction impl;
	
	public ClosureTranslator(ExprClosure e, ImTranslator tr, ImFunction f) {
		super();
		this.e = e;
		this.tr = tr;
		this.f = f;
	}



	public ImExpr translate() {
		if (e.attrExpectedTyp() instanceof WurstTypeCode) {
			return translateAnonFunc();
		} else {
			ImClass c = createClass();
			ImVar clVar = JassIm.ImVar(WurstTypeInt.instance().imTranslateType(), "clVar", false);
			f.getLocals().add(clVar);
			ImStmts stmts = JassIm.ImStmts();
			// allocate closure
			stmts.add(JassIm.ImSet(e, clVar, JassIm.ImAlloc(c)));
			// set closure vars
			for (Entry<ImVar, ImVar> entry : closureVars.entrySet()) {
				ImVar orig = entry.getKey();
				ImVar v = entry.getValue();
				WLogger.info(orig + " ---> " + v);
				stmts.add(JassIm.ImSetArray(e, v, JassIm.ImVarAccess(clVar), JassIm.ImVarAccess(orig)));		
			}
			return JassIm.ImStatementExpr(stmts, JassIm.ImVarAccess(clVar));
		}
	}



	private ImExpr translateAnonFunc() {
		impl = tr.getFuncFor(e);
		impl.getParameters().clear();
		ImExpr translated = e.getImplementation().imTranslateExpr(tr, impl);
		
		verifyTranslatedAnonfunc(translated);
		
		if (e.getImplementation().attrTyp() instanceof WurstTypeBool) {
			impl.getBody().add(JassIm.ImReturn(e, translated));
			impl.setReturnType(WurstTypeBool.instance().imTranslateType());
		} else {
			impl.getBody().add(translated);
			impl.setReturnType(WurstTypeVoid.instance().imTranslateType());
		}
		return JassIm.ImFuncRef(impl);
	}


	/** checks that there are  no captured variables */
	private void verifyTranslatedAnonfunc(ImExpr translated) {
		translated.accept(new ImExpr.DefaultVisitor() {
			@Override
			public void visit(ImVarAccess va) {
				if (isLocalToOtherFunc(va.getVar())) {
					throw new CompileError(va.attrTrace().attrSource(), "Anonymous functions used as 'code' cannot capture variables. Captured " + va.getVar().getName());
				}
			}
			
			@Override
			public void visit(ImSet s) {
				if (isLocalToOtherFunc(s.getLeft())) {
					throw new CompileError(s.attrTrace().attrSource(), "Anonymous functions used as 'code' cannot capture variables. Captured " + s.getLeft().getName());
				}
			}
		});
	}



	private ImClass createClass() {
		ImClass superClass = getSuperClass();
		FuncDef superMethod = getSuperMethod();
		ImMethod superMethodIm = tr.getMethodFor(superMethod);
		
		
		ImVars fields = JassIm.ImVars();
		ImMethods methods = JassIm.ImMethods();
		List<ImClass> superClasses = java.util.Collections.singletonList(superClass);
		ImClass c = JassIm.ImClass(e, "Closure", fields, methods, superClasses);
		tr.imProg().getClasses().add(c);
		
//		ImVars parameters = JassIm.ImVars();
//		parameters.add(tr.getThisVar(e));
//		for (WParameter p : e.getParameters()) {
//			parameters.add(tr.getVarFor(p));
//		}
//		ImType returnType = e.getImplementation().attrTyp().imTranslateType();
//		if (returnType == null) {
//			WLogger.info(e.attrTyp());
//			returnType = JassIm.ImVoid();
//		}
//		ImVars locals = JassIm.ImVars();
//		ImStmts body = JassIm.ImStmts();
//		List<FunctionFlag> flags = Collections.emptyList();
//		ImFunction impl JassIm.ImFunction(e, superMethod.getName(), parameters, returnType, locals, body, flags);
		impl = tr.getFuncFor(e);
		ImMethod m = JassIm.ImMethod(e, superMethod.getName(), impl, JassIm.ImMethods(), false);
		c.getMethods().add(m);
		superMethodIm.getSubMethods().add(m);
		
		
		ImExpr translated = e.getImplementation().imTranslateExpr(tr, impl);
		
		transformTranslated(translated);
		
		if (e.getImplementation().attrTyp().isVoid()) {
			impl.getBody().add(translated);
		} else {
			impl.getBody().add(JassIm.ImReturn(e, translated));
		}
		return c;
	}



	private void transformTranslated(ImExpr t) {
		final List<ImVarAccess> vas = Lists.newArrayList();
		final List<ImSet> sets = Lists.newArrayList();
		t.accept(new ImExpr.DefaultVisitor() {
			@Override
			public void visit(ImVarAccess va) {
				if (isLocalToOtherFunc(va.getVar())) {
					vas.add(va);
				}
			}
			
			@Override
			public void visit(ImSet s) {
				if (isLocalToOtherFunc(s.getLeft())) {
					sets.add(s);
				}
			}
		});
		
		for (ImVarAccess va : vas) {
			ImVar v = getClosureVarFor(va.getVar());			
			va.replaceWith(JassIm.ImVarArrayAccess(v, closureThis()));
		}
		for (ImSet s : sets) {
			ImVar v = getClosureVarFor(s.getLeft());
			ImExpr right = s.getRight();
			right.setParent(null);
			s.replaceWith(JassIm.ImSetArray(e, v, closureThis(), right));
		}
	}

	private ImExpr closureThis() {
		return JassIm.ImVarAccess(tr.getThisVar(e));
	}



	private ImVar getClosureVarFor(ImVar var) {
		ImVar v = closureVars.get(var);
		if (v == null) {
			v = JassIm.ImVar(arrayType(var.getType()), var.getName(), false);
			tr.imProg().getGlobals().add(v);
			closureVars.put(var, v);
		}
		return v;
	}



	private ImType arrayType(ImType type) {
		if (type instanceof ImSimpleType) {
			ImSimpleType t = (ImSimpleType) type;
			return JassIm.ImArrayType(t.getTypename());
		} else if (type instanceof ImTupleType) {
			ImTupleType t = (ImTupleType) type;
			return JassIm.ImTupleArrayType(t.getTypes(), t.getNames());
		}
		throw new CompileError(e.getSource(), "Closure references array variable.");
	}



	private boolean isLocalToOtherFunc(ImVar imVar) {
		if (imVar.getParent() == null
				|| imVar.getParent().getParent() == null) {
			return false;
		}
		if (imVar.getParent().getParent() instanceof ImFunction) {
			boolean r = imVar.getParent().getParent() != impl;
			return r;
		}
		return false;
	}



	private FuncDef getSuperMethod() {
		NameLink nl = e.attrClosureAbstractMethod();
		return (FuncDef) nl.getNameDef();
	}



	private ImClass getSuperClass() {
		WurstType t = e.attrExpectedTyp();
		if (t instanceof WurstTypeInterface) {
			WurstTypeInterface it = (WurstTypeInterface) t;
			return tr.getClassFor(it.getDef());
		} else if (t instanceof WurstTypeClass) {
			WurstTypeClass ct = (WurstTypeClass) t;
			return tr.getClassFor(ct.getDef());
		}
		throw new CompileError(e.getSource(), "Could not get super class for closure");
	}




}
