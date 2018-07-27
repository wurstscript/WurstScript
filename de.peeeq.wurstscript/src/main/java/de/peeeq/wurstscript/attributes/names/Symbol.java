package de.peeeq.wurstscript.attributes.names;

import com.google.common.collect.ImmutableList;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.ast.NameDef;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.utils.Utils;

/**
 *
 */
public abstract class Symbol<Def extends NameDef> {
    public abstract Def getDef(WurstModel m);

    public abstract String getName();


    public static <D extends NameDef> Symbol<D> fromDef(D def) {
        if (def instanceof LocalVarDef || def instanceof WParameter) {
            //noinspection unchecked
            return (Symbol<D>) new LocalVarSymbol((VarDef) def);
        } else {
            return GlobalSymbol.globalSymbolFromDef(def);
        }
    }
}
