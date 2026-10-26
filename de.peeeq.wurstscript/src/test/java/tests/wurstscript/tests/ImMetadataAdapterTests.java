package tests.wurstscript.tests;

import de.peeeq.wurstscript.jassIm.Element;
import de.peeeq.wurstscript.jassIm.JassIm;
import de.peeeq.wurstscript.jassIm.NodeGraphs;
import io.vavr.control.Either;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.testng.annotations.Test;

import java.util.*;
import static org.testng.Assert.*;

public class ImMetadataAdapterTests {
    @Test public void insertionOrderedFastutilMapsKeepNonComparableImKeysAndRepairTheirReferences() {
        var first=JassIm.ImSimpleType("first");var second=JassIm.ImSimpleType("second");
        var firstCopy=JassIm.ImSimpleType("first");var secondCopy=JassIm.ImSimpleType("second");
        Map<Element,Element> references=new IdentityHashMap<>();references.put(first,firstCopy);references.put(second,secondCopy);
        var source=new Object2ObjectLinkedOpenHashMap<Element,Element>();source.put(first,second);source.put(second,first);
        var mapper=new NodeGraphs.MetadataMapper(node->references.getOrDefault(node,node),NodeGraphs.Adapter.DEFAULT);
        var copy=(Map<?,?>)mapper.map(source);
        assertEquals(new ArrayList<>(copy.keySet()),List.of(firstCopy,secondCopy));
        assertSame(copy.get(firstCopy),secondCopy);assertSame(copy.get(secondCopy),firstCopy);
        assertSame(source.get(first),second);
    }

    @Test public void foreignAstListNodesRemainReferenceLeaves() {
        var mapper=new NodeGraphs.MetadataMapper(node->node,NodeGraphs.Adapter.DEFAULT);
        for(Object node:List.of(de.peeeq.wurstscript.ast.Ast.WurstModel(),
                de.peeeq.wurstscript.jassAst.JassAst.JassProgs(),de.peeeq.wurstscript.luaAst.LuaAst.LuaModel())) {
            assertFalse(NodeGraphs.needsMapping(node,NodeGraphs.Adapter.DEFAULT));assertSame(mapper.map(node),node);
        }
    }

    @Test public void immutableEitherArmsRepairNestedImReferences() {
        var first=JassIm.ImSimpleType("first");var second=JassIm.ImSimpleType("second");
        var firstCopy=JassIm.ImSimpleType("first");var secondCopy=JassIm.ImSimpleType("second");
        Map<Element,Element> references=new IdentityHashMap<>();references.put(first,firstCopy);references.put(second,secondCopy);
        var source=List.of(Either.left(first),Either.right(second));
        var mapper=new NodeGraphs.MetadataMapper(node->references.getOrDefault(node,node),NodeGraphs.Adapter.DEFAULT);
        var copy=(List<?>)mapper.map(source);
        assertSame(((Either<?,?>)copy.get(0)).getLeft(),firstCopy);
        assertSame(((Either<?,?>)copy.get(1)).get(),secondCopy);
        assertSame(source.get(0).getLeft(),first);assertSame(source.get(1).get(),second);
    }
}
