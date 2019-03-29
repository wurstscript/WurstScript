package de.peeeq.wurstscript.intermediatelang;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImClassType;
import de.peeeq.wurstscript.jassIm.ImVar;

import java.util.*;
import java.util.stream.Collectors;

public class ILconstObject extends ILconstAbstract {
    private final ImClassType classType;
    private final int objectId;
    private final Table<ImVar, List<Integer>, ILconst> attributes = HashBasedTable.create();
    private boolean destroyed = false;
    private final Element trace;

    public ILconstObject(ImClassType classType, int objectId, Element trace) {
        this.classType = classType;
        this.objectId = objectId;
        this.trace = trace;
    }

    public int getObjectId() {
        return objectId;
    }

    @Override
    public String print() {
        return classType + "_" + hashCode();
    }



    @Override
    public boolean isEqualTo(ILconst other) {
        return other == this;
    }

    public void set(ImVar attr, List<Integer> indexes, ILconst value) {
        attributes.put(attr, indexes, value);
    }

    public Optional<ILconst> get(ImVar attr, List<Integer> indexes) {
        return Optional.ofNullable(attributes.get(attr, indexes));
    }


    public boolean isDestroyed() {
        return destroyed;
    }

    public void destroy() {
        destroyed = true;
    }

    public ImClass getImClass() {
        return classType.getClassDef();
    }

    public Element getTrace() {
        return trace;
    }

    public ImClassType getType() {
        return classType;
    }

    @Override
    public int hashCode() {
        return objectId;
    }

    public Table<ImVar, List<Integer>, ILconst> getAttributes() {
        return attributes;
    }
}
