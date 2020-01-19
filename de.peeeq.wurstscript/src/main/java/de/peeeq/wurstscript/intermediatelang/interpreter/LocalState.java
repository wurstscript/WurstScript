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
    private Table<ImTypeVar, ImClassType, ImTypeClassImpl> typeClassImplementations = HashBasedTable.create();

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


    public @Nullable ImTypeClassImpl getImplementation(ImTypeVar typeVariable, ImClassType classType) {
        return typeClassImplementations.get(typeVariable, classType);
    }

    public void setTypeArguments(ImTypeVars typeVariables, List<ImTypeArgument> typeArguments) {
        for (int i = 0; i < typeVariables.size() && i < typeArguments.size(); i++) {
            ImTypeVar typeVariable = typeVariables.get(i);
            ImTypeArgument typeArgument = typeArguments.get(i);
            for (ImTypeClassImpl e : typeArgument.getTypeClassImplementations()) {
                typeClassImplementations.put(typeVariable, e.getTypeClass(), e);
            }
        }
    }
}
