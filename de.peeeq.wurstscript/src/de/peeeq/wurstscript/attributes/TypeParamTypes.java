package de.peeeq.wurstscript.attributes;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.TypeParamDefs;
import de.peeeq.wurstscript.types.WurstType;

public class TypeParamTypes {

	public static List<WurstType> calculte(TypeParamDefs defs) {
		if (defs.size() == 0) {
			return Collections.emptyList();
		} else if (defs.size() == 1) {
			return Collections.singletonList(defs.get(0).attrTyp());
		}
		List<WurstType> result = Lists.newArrayList();
		for (TypeParamDef def : defs) {
			result.add(def.attrTyp());
		}
		return result ;
	}

}
