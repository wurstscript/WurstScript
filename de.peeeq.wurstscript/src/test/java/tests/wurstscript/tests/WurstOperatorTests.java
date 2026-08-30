package tests.wurstscript.tests;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.intermediatelang.ILconst;
import de.peeeq.wurstscript.intermediatelang.ILconstBool;
import de.peeeq.wurstscript.intermediatelang.ILconstInt;
import de.peeeq.wurstscript.intermediatelang.ILconstReal;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

public class WurstOperatorTests {
    @Test
    public void moduloMatchesWurstAndJassSemantics() {
        assertEquals(WurstOperator.moduloInteger(-7, 3), 2);
        assertEquals(WurstOperator.moduloInteger(7, -3), 1);
        assertEquals(WurstOperator.jassModuloInteger(-7, 3), -1);
        assertEquals(WurstOperator.moduloReal(-7.5f, 3f), 1.5f, 0.0001f);
    }

    @Test
    public void binaryEvaluationPreservesLazyAndArithmeticOperators() {
        ILconst falseValue = WurstOperator.AND.evaluateBinaryOperator(ILconstBool.instance(false),
            () -> { throw new AssertionError("AND evaluated its lazy right operand"); });
        ILconst trueValue = WurstOperator.OR.evaluateBinaryOperator(ILconstBool.instance(true),
            () -> { throw new AssertionError("OR evaluated its lazy right operand"); });
        assertFalse(((ILconstBool) falseValue).getVal());
        assertTrue(((ILconstBool) trueValue).getVal());

        ILconst sum = WurstOperator.PLUS.evaluateBinaryOperator(new ILconstInt(2), () -> new ILconstInt(3));
        ILconst quotient = WurstOperator.DIV_INT.evaluateBinaryOperator(new ILconstInt(7), () -> new ILconstInt(2));
        ILconst remainder = WurstOperator.MOD_INT.evaluateBinaryOperator(new ILconstInt(-7), () -> new ILconstInt(3));
        ILconst realRemainder = WurstOperator.MOD_REAL.evaluateBinaryOperator(new ILconstReal(-7.5f),
            () -> new ILconstReal(3f));
        assertEquals(((ILconstInt) sum).getVal(), 5);
        assertEquals(((ILconstInt) quotient).getVal(), 3);
        assertEquals(((ILconstInt) remainder).getVal(), 2);
        assertEquals(((ILconstReal) realRemainder).getVal(), 1.5f, 0.0001f);
    }

    @Test
    public void unaryEvaluationAndLazyClassificationAreConsistent() {
        assertTrue(WurstOperator.AND.isLazy());
        assertTrue(WurstOperator.OR.isLazy());
        assertFalse(WurstOperator.PLUS.isLazy());
        assertFalse(((ILconstBool) WurstOperator.NOT.evaluateUnaryOperator(ILconstBool.instance(true))).getVal());
        assertEquals(((ILconstInt) WurstOperator.UNARY_MINUS.evaluateUnaryOperator(new ILconstInt(4))).getVal(), -4);
    }

    @Test
    public void operatorMetadataAndBackendMappingsAreExplicit() {
        assertTrue(WurstOperator.PLUS.isBinaryOp());
        assertFalse(WurstOperator.PLUS.isUnaryOp());
        assertTrue(WurstOperator.NOT.isUnaryOp());
        assertFalse(WurstOperator.NOT.isBinaryOp());
        assertEquals(WurstOperator.DIV_REAL.toString(), "/");
        assertEquals(WurstOperator.PLUS.getOverloadingFuncName(), "op_plus");
        assertNotNull(WurstOperator.DIV_INT.jassTranslateBinary());
        assertNotNull(WurstOperator.MOD_REAL.luaTranslateBinary());
        assertNotNull(WurstOperator.UNARY_MINUS.jassTranslateUnary());
        assertNotNull(WurstOperator.NOT.jassTranslateUnary());
        expectThrows(Error.class, () -> WurstOperator.MOD_INT.jassTranslateBinary());
        expectThrows(Error.class, () -> WurstOperator.MOD_INT.luaTranslateBinary());
        expectThrows(Error.class, () -> WurstOperator.PLUS.jassTranslateUnary());
    }
}
