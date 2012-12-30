package de.peeeq.wurstscript.types;

import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.ast.TupleDef;
import de.peeeq.wurstscript.ast.VarDef;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.jassAst.JassExpr;
import de.peeeq.wurstscript.jassAst.JassVar;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImExprs;
import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.ImTupleType;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.utils.Utils;


public class WurstTypeTuple extends WurstType {

	TupleDef tupleDef;


	public WurstTypeTuple(TupleDef tupleDef) {
		if (tupleDef == null) throw new IllegalArgumentException();
		this.tupleDef = tupleDef;
	}

	@Override
	public boolean isSubtypeOf(WurstType other, AstElement location) {
		if (other instanceof WurstTypeTuple) {
			WurstTypeTuple otherTuple = (WurstTypeTuple) other;
			return tupleDef == otherTuple.tupleDef;
		}
		return false;
	}
	

	public TupleDef getTupleDef() {
		return tupleDef;
	}
	
	@Override
	public String getName() {
		return tupleDef.getName();
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

	@Override
	public ImType imTranslateType() {
		List<String> types = Lists.newArrayList();
		List<String> names = Lists.newArrayList();
		for (WParameter p : tupleDef.getParameters()) {
			ImType pt = p.attrTyp().imTranslateType();
			if (pt instanceof ImTupleType) {
				ImTupleType ptt = (ImTupleType) pt;
				for (String t : ptt.getTypes()) {
					types.add(t);
				}
				for (String n : ptt.getNames()) {
					names.add(p.getName() + "_" + n);
				}
			} else if (pt instanceof ImSimpleType) {
				ImSimpleType st = (ImSimpleType) pt;
				types.add(st.getTypename());
				names.add(p.getName());
			}
		}
		return JassIm.ImTupleType(types, names);
	}

	@Override
	public ImExprOpt getDefaultValue() {
		ImExprs exprs = JassIm.ImExprs();
		for (WParameter p : tupleDef.getParameters()) {
			exprs.add((ImExpr) p.attrTyp().getDefaultValue());
		}
		return JassIm.ImTupleExpr(exprs);
	}
}
