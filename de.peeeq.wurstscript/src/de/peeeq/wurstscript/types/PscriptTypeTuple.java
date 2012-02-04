package de.peeeq.wurstscript.types;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.NamedScope;
import de.peeeq.wurstscript.ast.TupleDef;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.jassAst.JassExpr;
import de.peeeq.wurstscript.jassAst.JassVar;
import de.peeeq.wurstscript.utils.Utils;


public class PscriptTypeTuple extends PscriptType {

	TupleDef tupleDef;


	public PscriptTypeTuple(TupleDef tupleDef) {
		if (tupleDef == null) throw new IllegalArgumentException();
		this.tupleDef = tupleDef;
	}

	@Override
	public boolean isSubtypeOf(PscriptType other, AstElement location) {
		if (other instanceof PscriptTypeTuple) {
			PscriptTypeTuple otherTuple = (PscriptTypeTuple) other;
			return tupleDef == otherTuple.tupleDef;
		}
		return false;
	}
	

	public TupleDef getTupleDef() {
		return tupleDef;
	}
	
	@Override
	public String getName() {
		return tupleDef.getName() + " (tuple)";
	}

	@Override
	public String getFullName() {
		return getName();
	}

	@Override
	public String[] jassTranslateType() {
		List<String> result = Lists.newArrayList();
		for (WParameter p : tupleDef.getParameters()) {
			for (String t : p.attrTyp().jassTranslateType()) {
				result.add(t);
			}
		}
		return result.toArray(new String[result.size()]);
	}

	public List<JassVar> getJassVars(List<JassVar> jassVars, VarDef param) {
		int i = 0;
		for (WParameter p : tupleDef.getParameters()) {
			int varCount = p.attrTyp().jassTranslateType().length;
			if (p == param) {
				return Utils.slice(jassVars, i, varCount);
			}
			i+= varCount;
		}
		throw new Error("Could not find parameter " + param.getName() + " for tuple " + tupleDef.getName());
	}
	
	public List<JassExpr> getJassExprs(List<JassExpr> jassExprs, NameDef param) {
		int i = 0;
		for (WParameter p : tupleDef.getParameters()) {
			int varCount = p.attrTyp().jassTranslateType().length;
			if (p == param) {
				return Utils.slice(jassExprs, i, varCount);
			}
			i+= varCount;
		}
		throw new Error("Could not find parameter " + param.getName() + " for tuple " + tupleDef.getName());
	}
}
