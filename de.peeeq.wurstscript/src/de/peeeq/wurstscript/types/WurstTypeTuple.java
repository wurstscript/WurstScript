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
	public boolean isSubtypeOfIntern(WurstType other, AstElement location) {
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
	public ImType imTranslateType() {
		List<ImType> types = Lists.newArrayList();
		List<String> names = Lists.newArrayList();
		for (WParameter p : tupleDef.getParameters()) {
			ImType pt = p.attrTyp().imTranslateType();
			types.add(pt);
			names.add(p.getName());
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
