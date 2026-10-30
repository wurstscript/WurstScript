package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.Ast;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;

import java.util.List;
import java.util.function.Supplier;

import static org.testng.Assert.*;

public class ImBackendInputTests {
    private record Input(ImBackendInput input, int function) { }

    private static Input input(int literal, WurstOperator operator) {
        return input(JassIm.ImOperatorCall(operator, JassIm.ImExprs(JassIm.ImIntVal(literal), JassIm.ImIntVal(3))), "integer");
    }

    private static Input input(ImExpr expression, String type) {
        ImTranslator translator = new ImTranslator(Ast.WurstModel(), false, new RunArgs("-lua", "-luaHotReload"));
        ImFunction function = JassIm.ImFunction(Ast.NoExpr(), "value", JassIm.ImTypeVars(), JassIm.ImVars(),
            JassIm.ImSimpleType(type), JassIm.ImVars(),
            JassIm.ImStmts(JassIm.ImReturn(Ast.NoExpr(), expression)), List.of());
        translator.imProg().getFunctions().add(function);
        ImBackendInput input = new ImBackendInput(translator.imProg(), translator);
        return new Input(input, input.ids.get(function));
    }

    @Test public void exactAndShapeReceiptsEncodeEachInputNodeOnce() {
        Input fixture = input(7, WurstOperator.PLUS);
        assertTrue(fixture.input.receipt.shapes().containsKey(fixture.function));
        assertEquals(fixture.input.receiptEncodings, fixture.input.nodes.size(),
            "Exact and shape receipts must share the schema encoding");
    }

    @Test public void literalEditsKeepShapeAndStructuralEditsChangeIt() {
        Input before = input(7, WurstOperator.PLUS), literal = input(8, WurstOperator.PLUS), structural = input(7, WurstOperator.MINUS);
        assertNotEquals(before.input.receipt.bodies().get(before.function), literal.input.receipt.bodies().get(literal.function));
        assertEquals(before.input.receipt.shapes().get(before.function), literal.input.receipt.shapes().get(literal.function));
        assertNotEquals(before.input.receipt.shapes().get(before.function), structural.input.receipt.shapes().get(structural.function));
    }

    @DataProvider public Object[][] literals() {
        return new Object[][] {
            {"integer", (Supplier<ImExpr>) () -> JassIm.ImIntVal(7), (Supplier<ImExpr>) () -> JassIm.ImIntVal(8)},
            {"real", (Supplier<ImExpr>) () -> JassIm.ImRealVal("0.5"), (Supplier<ImExpr>) () -> JassIm.ImRealVal("0.75")},
            {"boolean", (Supplier<ImExpr>) () -> JassIm.ImBoolVal(true), (Supplier<ImExpr>) () -> JassIm.ImBoolVal(false)},
            {"string", (Supplier<ImExpr>) () -> JassIm.ImStringVal("r\u00e9sum\u00e9"), (Supplier<ImExpr>) () -> JassIm.ImStringVal("other")}
        };
    }
    @Test(dataProvider = "literals") public void pairedReceiptsPreserveEveryLiteralKind(String type, Supplier<ImExpr> first, Supplier<ImExpr> second) {
        Input before = input(first.get(), type), after = input(second.get(), type);
        assertNotEquals(before.input.receipt.bodies().get(before.function), after.input.receipt.bodies().get(after.function));
        assertEquals(before.input.receipt.shapes().get(before.function), after.input.receipt.shapes().get(after.function));
        assertEquals(after.input.receiptEncodings, after.input.nodes.size());
    }
}
