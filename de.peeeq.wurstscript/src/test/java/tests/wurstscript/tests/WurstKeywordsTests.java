package tests.wurstscript.tests;

import de.peeeq.wurstscript.WurstKeywords;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.testng.Assert.assertTrue;

public class WurstKeywordsTests {
    @Test
    public void keywordTablesContainLanguageAndJassWords() {
        assertTrue(Arrays.asList(WurstKeywords.KEYWORDS).containsAll(Arrays.asList(
            "class", "tuple", "compiletime", "isLua", "function", "returns")));
        assertTrue(Arrays.asList(WurstKeywords.JASS_PRIMITIVE_TYPES).containsAll(Arrays.asList(
            "int", "real", "boolean", "string", "handle")));
        assertTrue(Arrays.asList(WurstKeywords.JASSTYPES).containsAll(Arrays.asList(
            "unit", "trigger", "framehandle", "hashtable")));
    }
}
