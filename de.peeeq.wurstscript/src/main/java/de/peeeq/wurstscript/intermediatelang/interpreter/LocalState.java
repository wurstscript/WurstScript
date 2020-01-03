package de.peeeq.wurstscript.intermediatelang.interpreter;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.jassIm.*;
import io.vavr.control.Either;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;
import java.util.Map;


public class LocalState extends State {

    private @Nullable ILconst returnVal = null;
    private Table<ImTypeVar, ImTypeClassFunc, Either<ImMethod, ImFunction>> typeClassImplementations = HashBasedTable.create();

    public LocalState(ILconst returnVal) {
        this.setReturnVal(returnVal);
    }

    public LocalState() {
    }

    public @Nullable ILconst getReturnVal() {
        return returnVal;
    }

    public LocalState setReturnVal(@Nullable ILconst returnVal) {
        this.returnVal = returnVal;
        return this;
    }


    public Either<ImMethod, ImFunction> getImplementation(ImTypeVar typeVariable, ImTypeClassFunc typeClassFunc) {
        return typeClassImplementations.get(typeVariable, typeClassFunc);
    }

    public void setTypeArguments(ImTypeVars typeVariables, List<ImTypeArgument> typeArguments) {
        for (int i = 0; i < typeVariables.size() && i < typeArguments.size(); i++) {
            ImTypeVar typeVariable = typeVariables.get(i);
            ImTypeArgument typeArgument = typeArguments.get(i);
            for (Map.Entry<ImTypeClassFunc, Either<ImMethod, ImFunction>> e : typeArgument.getTypeClassBinding().entrySet()) {
                typeClassImplementations.put(typeVariable, e.getKey(), e.getValue());
            }
        }
    }
}
