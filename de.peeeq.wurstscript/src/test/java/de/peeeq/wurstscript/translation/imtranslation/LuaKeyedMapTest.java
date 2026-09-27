package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.jassIm.JassIm;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class LuaKeyedMapTest {

    @Test
    public void integerSpecializationUsesTypedDefaultStub() {
        assertEquals(LuaKeyedMap.nativeGetStub(JassIm.ImSimpleType("integer")), LuaKeyedMap.NATIVE_GET_INT);
    }
}
