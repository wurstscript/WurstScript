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
    @Test public void functionFlagsAreOwnedAndTrackedSeparatelyFromSourceOrigins() {
        var flags = new ArrayList<de.peeeq.wurstscript.translation.imtranslation.FunctionFlag>();
        var function = JassIm.ImFunction(de.peeeq.wurstscript.ast.Ast.NoExpr(), "f", JassIm.ImTypeVars(),
            JassIm.ImVars(), JassIm.ImVoid(), JassIm.ImVars(), JassIm.ImStmts(), flags);
        int semantic = function.semanticModificationCount();
        function.getFlags().add(de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum.IS_NATIVE);
        assertNotEquals(function.semanticModificationCount(), semantic);
        assertTrue(flags.isEmpty(), "caller-owned flags must not be mutated");
        semantic = function.semanticModificationCount(); int source = function.sourceModificationCount();
        function.setTrace(de.peeeq.wurstscript.ast.Ast.NoExpr());
        assertEquals(function.semanticModificationCount(), semantic);
        assertNotEquals(function.sourceModificationCount(), source);
    }

    @Test public void flattenKeepsInitializerMetadataPointingAtTheOwnedReplacement() {
        var trace = de.peeeq.wurstscript.ast.Ast.NoExpr();
        var translator = new de.peeeq.wurstscript.translation.imtranslation.ImTranslator(
            de.peeeq.wurstscript.ast.Ast.WurstModel(), false, new de.peeeq.wurstscript.RunArgs("-lua"));
        var variable = JassIm.ImVar(trace, JassIm.ImSimpleType("int"), "global", false);
        var initializer = JassIm.ImSet(trace, JassIm.ImVarAccess(variable), JassIm.ImIntVal(7));
        var function = JassIm.ImFunction(trace, "initialize", JassIm.ImTypeVars(), JassIm.ImVars(),
            JassIm.ImVoid(), JassIm.ImVars(), JassIm.ImStmts(initializer), new ArrayList<>());
        translator.imProg().getGlobals().add(variable);
        translator.imProg().getFunctions().add(function);
        translator.imProg().getGlobalInits().put(variable, List.of(initializer));
        function.flatten(translator);
        var current = translator.imProg().getGlobalInits().get(variable).getFirst();
        assertSame(current, function.getBody().getFirst());
        NodeGraphs.validate(current);
    }

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
