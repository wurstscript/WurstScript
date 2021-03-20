package de.peeeq.wurstscript.attributes;

import com.google.common.collect.ImmutableList;
import de.peeeq.wurstscript.ast.TypeParamDef;
import de.peeeq.wurstscript.ast.TypeParamDefs;
import de.peeeq.wurstscript.types.WurstType;

public class TypeParamTypes {

  public static ImmutableList<WurstType> calculte(TypeParamDefs defs) {
    if (defs.size() == 0) {
      return ImmutableList.of();
    } else if (defs.size() == 1) {
      return ImmutableList.of(defs.get(0).attrTyp());
    }
    ImmutableList.Builder<WurstType> result = ImmutableList.builder();
    for (TypeParamDef def : defs) {
      result.add(def.attrTyp());
    }
    return result.build();
  }
}
