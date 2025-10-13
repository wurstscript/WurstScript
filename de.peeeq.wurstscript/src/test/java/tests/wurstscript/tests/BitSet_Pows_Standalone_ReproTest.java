package tests.wurstscript.tests;

import org.testng.annotations.Test;

public class BitSet_Pows_Standalone_ReproTest extends WurstScriptTest {

    @Test
    public void highestBit_reset_should_clear_with_buggy_reversePows() {
        test().withStdLib().executeProg().lines(
            "package BitSetStandalone",
            "",
            "constant int BITSET_SIZE = 32",
            "",
            "int array pows",
            "int array reversePows",
            "",
            "@compiletime function initPows()",
            "    pows[0] = 1",
            "    var allPows = 1",
            "    for i = 1 to BITSET_SIZE - 1",
            "        pows[i] = pows[i - 1] * 2",
            "        allPows = BlzBitOr(allPows, pows[i])",
            "    for i = 0 to BITSET_SIZE - 1",
            "        reversePows[i] = BlzBitXor(allPows, pows[i])",
            "",
            "init",
            "    initPows()",
            "    let bit = BITSET_SIZE - 1",
            "    var val = 0",
            "    // set highest bit",
            "    val = BlzBitOr(val, pows[bit])",
            "    // reset that bit using reversePows",
            "    val = BlzBitAnd(val, reversePows[bit])",
            "    // expect bit cleared; if buggy masks, this stays set and testSuccess() won't be called",
            "    if BlzBitAnd(val, pows[bit]) == 0",
            "        testSuccess()",
            "endpackage"
        );
    }
}
