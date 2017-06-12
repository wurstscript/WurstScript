package de.peeeq.wurstscript.types;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.TupleDef;
import de.peeeq.wurstscript.ast.WParameter;
import de.peeeq.wurstscript.jassIm.ImExpr;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImExprs;
import de.peeeq.wurstscript.jassIm.ImTupleType;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;


public class WurstTypeTuple extends WurstType {

	TupleDef tupleDef;


	public WurstTypeTuple(TupleDef tupleDef) {
		Preconditions.checkNotNull(tupleDef);
		this.tupleDef = tupleDef;
	}

	@Override
	public boolean isSubtypeOfIntern(WurstType other, @Nullable Element location) {
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
			if (pt instanceof ImTupleType) {
                ImTupleType ptt = (ImTupleType) pt;
                // add flattened
                for (int i=0; i<ptt.getTypes().size(); i++) {
                    types.add(ptt.getTypes().get(i));
                    names.add(p.getName() + "_" + ptt.getNames().get(i));
                }
			} else {
    			types.add(pt);
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
