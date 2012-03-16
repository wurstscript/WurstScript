package de.peeeq.wurstscript.translation.imtranslation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.jassAst.JassAst;
import de.peeeq.wurstscript.jassAst.JassExpr;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.ImStmt;
import de.peeeq.wurstscript.jassIm.ImTupleType;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVars;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.types.TypesHelper;
import static de.peeeq.wurstscript.jassAst.JassAst.JassExprIntVal;
import static de.peeeq.wurstscript.jassIm.JassIm.*;

public class ImTranslator {

	
	private Map<Class<? extends OpBinary>, ImFunction> binaryOperatorFunctions = Maps.newHashMap();
	private CompilationUnit wurstProg;
	
	
	
	
	public ImTranslator(CompilationUnit wurstProg) {
		this.wurstProg = wurstProg;
	}
	

	private Map<TranslatedToImFunction, ImFunction> functionMap = Maps.newHashMap();
	
	public ImFunction getFuncFor(TranslatedToImFunction funcDef) {
		if (functionMap.containsKey(funcDef)) {
			return functionMap.get(funcDef);
		}
		String name = getNameFor(funcDef);;
		ImFunction f = JassIm.ImFunction(name, ImVars(), ImVoid(), ImVars(), ImStmts(), false);
		
		functionMap.put(funcDef, f);
		return f;
	}
	
	/**
	 * returns a suitable name for the given element
	 * the returned name is a valid jass identifier 
	 */
	public String getNameFor(AstElement e) {
		if (e instanceof AstElementWithName) {
			AstElementWithName wn = (AstElementWithName) e;
			return wn.getName();
		} else if (e instanceof ConstructorDef) {
			return "new_" + e.attrNearestClassDef().getName();
		}
		String r = e.getClass().getSimpleName();
		while (e != null) {
			if (e instanceof AstElementWithName) {
				AstElementWithName wn = (AstElementWithName) e;
				r = wn + "_" + r;
			}
			e = e.getParent();
		}
		return r;
	}

	private Map<WScope, ImVar> thisVarMap = Maps.newHashMap();

	public ImVar getThisVar(WScope scope) {
		if (thisVarMap.containsKey(scope)) {
			return thisVarMap.get(scope);
		}
		ImVar v = ImVar(ImSimpleType("integer"), "this");
		thisVarMap.put(scope, v);
		return v ;
	}

	private Map<VarDef, ImVar> varMap = Maps.newHashMap();
	
	public ImVar getVarFor(VarDef varDef) {
		ImVar v = varMap.get(varDef);
		if (v == null) {
			ImType type = varDef.attrTyp().imTranslateType();
			String name = varDef.getName();
			v = JassIm.ImVar(type, name);
			varMap.put(varDef, v);
			
		}
		return v;
	}

	public int getTupleIndex(TupleDef tupleDef, VarDef parameter) {
		int i = 0;
		for (WParameter p : tupleDef.getParameters()) {
			if (p == parameter) {
				return i;
			}
			i++;
		}
		throw new Error("");
	}
	
	
	Map<ClassDef, ImFunction> destroyFuncMap = Maps.newHashMap();
	private ImProg imProg;

	public ImFunction getDestroyFuncFor(ClassDef classDef) {
		ImFunction f = destroyFuncMap.get(classDef); 
		if (f == null) {
			f = JassIm.ImFunction("destroy" + classDef.getName(), ImVars(ImVar(TypesHelper.imInt(), "this")), TypesHelper.imInt(), ImVars(), ImStmts(), false);
			destroyFuncMap.put(classDef, f);
		}
		return f ;
	}


	public List<ImStmt> translateStatements(ImFunction f, List<WStatement> statements) {
		List<ImStmt> result = Lists.newArrayList();
		for (WStatement s : statements) {
			result.add(s.imTranslateStmt(this, f));
		}
		return result ;
	}

	public ImProg translateProg() {
		imProg = ImProg(ImVars(), ImFunctions());
		for (TopLevelDeclaration tld : wurstProg) {
			tld.imTranslateTLD(this);
		}
		return imProg;
	}

	public ImProg imProg() {
		return imProg;
	}

	public void addFunction(ImFunction f) {
		imProg.getFunctions().add(f);
	}

	public void addGlobal(ImVar v) {
		imProg.getGlobals().add(v);
	}


	private int typeIdCounter = 0;
	Map<ClassDef, Integer> typeIdMap = Maps.newHashMap();
	
	public int getTypeId(ClassDef c) {
		Integer r = typeIdMap.get(c); 
		if (r == null) {   
			typeIdCounter++;
			typeIdMap.put(c, typeIdCounter);
			return typeIdCounter;
		} else {
			return r;
		}
	}

	public CompilationUnit getWurstProg() {
		return wurstProg;
	}

	public ImExpr getDefaultValueForJassType(ImType type) {
		if (type instanceof ImSimpleType) {
			ImSimpleType imSimpleType = (ImSimpleType) type;
			String typeName = imSimpleType.getTypename();
			if (typeName.equals("integer")) {
				return ImIntVal(0);
			} else if (typeName.equals("real")) {
				return ImRealVal("0.");
			} else if (typeName.equals("boolean")) {
				return ImBoolVal(false);
			} else {
				return ImNull();
			}
		} else if (type instanceof ImTupleType) {
			ImTupleType imTupleType = (ImTupleType) type;
			return getDefaultValueForJassType(imTupleType.getTypes().get(0));
		} else {
			throw new IllegalArgumentException("could not get default value for type " + type);
		}
	}

	public void addCallRelation(ImFunction callingFunc, ImFunction calledFunc) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}

	public void addGlobalInitalizer(ImVar v, OptExpr initialExpr) {
		// TODO Auto-generated method stub
		throw new Error("not implemented");
	}
		


}
