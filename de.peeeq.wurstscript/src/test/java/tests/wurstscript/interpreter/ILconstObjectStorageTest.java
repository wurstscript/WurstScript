package tests.wurstscript.interpreter;

import com.google.common.collect.Table;
import com.sun.management.ThreadMXBean;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstObject;
import de.peeeq.wurstscript.jassIm.*;
import org.testng.annotations.Test;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.*;

public class ILconstObjectStorageTest {
    private final Element trace = Ast.NoExpr();
    private final ImClass clazz = JassIm.ImClass(trace, "Payload", JassIm.ImTypeVars(),
        JassIm.ImVars(), JassIm.ImMethods(), JassIm.ImFunctions(), Collections.emptyList());
    private final ImClassType type = JassIm.ImClassType(clazz, JassIm.ImTypeArguments());

    private ImVar field(String name) {
        return JassIm.ImVar(trace, JassIm.ImSimpleType("integer"), name, false);
    }

    @Test
    public void scalarAndIndexedFieldsRoundTripAndExport() {
        ILconstObject object = new ILconstObject(type, 1, trace);
        ImVar scalar = field("scalar");
        ImVar indexed = field("indexed");
        assertTrue(object.get(scalar, List.of()).isEmpty());
        assertTrue(object.get(indexed, List.of(2, 3)).isEmpty());
        object.set(scalar, new ArrayList<>(), ILconstInt.create(10));
        object.set(indexed, List.of(2, 3), ILconstInt.create(20));
        object.set(indexed, List.of(3, 2), ILconstInt.create(30));
        object.set(scalar, List.of(), ILconstInt.create(11));
        object.set(indexed, List.of(2, 3), ILconstInt.create(21));
        assertEquals(object.get(scalar, List.of()).orElseThrow(), ILconstInt.create(11));
        assertEquals(object.get(indexed, List.of(2, 3)).orElseThrow(), ILconstInt.create(21));
        assertEquals(object.get(indexed, List.of(3, 2)).orElseThrow(), ILconstInt.create(30));
        Table<ImVar, List<Integer>, ILconst> exported = object.getAttributes();
        assertEquals(exported.size(), 3);
        assertEquals(exported.row(scalar).get(List.of()), ILconstInt.create(11));
        assertEquals(exported.row(indexed).get(List.of(2, 3)), ILconstInt.create(21));
        assertEquals(exported.row(indexed).get(List.of(3, 2)), ILconstInt.create(30));
    }

    @Test
    public void scalarStorageDoesNotAllocateATableRowPerField() {
        ThreadMXBean allocations = (ThreadMXBean) ManagementFactory.getThreadMXBean();
        allocations.setThreadAllocatedMemoryEnabled(true);
        ImVar[] fields = new ImVar[64];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = field("field" + i);
        }
        ILconstObject[] objects = new ILconstObject[10000];
        ILconstInt value = ILconstInt.create(42);
        long before = allocations.getCurrentThreadAllocatedBytes();
        for (int i = 0; i < objects.length; i++) {
            ILconstObject object = new ILconstObject(type, i + 1, trace);
            objects[i] = object;
            for (ImVar field : fields) {
                object.set(field, new ArrayList<>(), value);
            }
        }
        long allocated = allocations.getCurrentThreadAllocatedBytes() - before;
        assertSame(objects[9999].get(fields[63], List.of()).orElseThrow(), value);
        // 640,000 scalar writes must fit comfortably below the old nested-table representation.
        assertTrue(allocated < 64L * 1024 * 1024, "Scalar storage allocated " + allocated + " bytes");
    }
}
