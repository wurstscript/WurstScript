package de.peeeq.wurstscript.attributes.symbols;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.types.WurstType;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class FunctionSymbol extends ExportedSymbol {
    private final @Nullable WurstType receiverType;
    private final String name;
    private final List<WurstType> paramTypes;

    public FunctionSymbol(ExportingScopeSymbol scope, @Nullable WurstType receiverType, String name, List<WurstType> paramTypes) {
        super(scope);
        this.receiverType = receiverType;
        this.name = name;
        this.paramTypes = paramTypes;
    }

    @Override
    public String getName() {
        return scope + "/" + receiverType + "." + name + "(" + paramTypes.stream().map(Object::toString).collect(Collectors.joining(", ")) + ")";
    }

    @Override
    public Element resolve(WurstModel model) {
        return scope.resolve(model).attrLookupFunction(receiverType, name, paramTypes).orElseGet(() -> {
            throw new CouldNotResolveException();
        });
    }
}
