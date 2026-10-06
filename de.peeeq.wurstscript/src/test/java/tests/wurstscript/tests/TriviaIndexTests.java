package tests.wurstscript.tests;

import de.peeeq.wurstscript.antlr.WurstParser;
import de.peeeq.wurstscript.parser.TriviaIndex;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.testng.annotations.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TriviaIndexTests {
    @Test
    public void documentationLookupDoesNotScanTheFilePrefix() {
        AtomicInteger offsetReads = new AtomicInteger();
        List<Token> tokens = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            tokens.add(new CountingToken(i * 10, offsetReads));
        }
        tokens.add(token(WurstParser.HOTDOC_COMMENT, "/** last declaration */", 100000));
        tokens.add(new CountingToken(100100, offsetReads));
        TriviaIndex index = TriviaIndex.fromTokens(tokens);
        offsetReads.set(0);

        assertEquals(index.findLeadingHotdoc(100100), " last declaration ");
        assertTrue(offsetReads.get() < 40, "Offset reads must grow logarithmically, actual = " + offsetReads.get());
    }

    @Test
    public void leadingDocumentationPreservesTriviaAndDeclarationBoundaries() {
        TriviaIndex index = TriviaIndex.fromTokens(List.of(
                token(WurstParser.HOTDOC_COMMENT, "/** first */", 0),
                token(WurstParser.NL, "\n", 20),
                token(WurstParser.LINE_COMMENT, "// ordinary comment", 21),
                token(WurstParser.SPACETAB, "    ", 50),
                token(WurstParser.ID, "first", 54),
                token(WurstParser.ID, "second", 100)));
        assertEquals(index.findLeadingHotdoc(54), " first ");
        assertEquals(index.findLeadingHotdoc(51), " first ");
        assertEquals(index.findLeadingHotdoc(100), "");
        assertEquals(index.findLeadingHotdoc(101), "");
        assertEquals(TriviaIndex.empty().findLeadingHotdoc(0), "");
    }

    @Test
    public void mergedHiddenCommentsAreIndexedInSourceOrder() {
        ArrayDeque<Token> hidden = new ArrayDeque<>();
        hidden.add(token(WurstParser.HOTDOC_COMMENT, "/** second */", 50));
        hidden.add(token(WurstParser.HOTDOC_COMMENT, "/** first */", 0));
        List<Token> visible = new ArrayList<>(List.of(token(WurstParser.ID, "first", 30),
                token(WurstParser.ID, "second", 80)));
        TriviaIndex index = TriviaIndex.fromTokens(visible, hidden);
        visible.clear();
        hidden.clear();
        assertEquals(index.findLeadingHotdoc(30), " first ");
        assertEquals(index.findLeadingHotdoc(80), " second ");
    }

    private static CommonToken token(int type, String text, int start) {
        CommonToken token = new CommonToken(type, text);
        token.setStartIndex(start);
        token.setStopIndex(start + text.length() - 1);
        return token;
    }

    private static class CountingToken extends CommonToken {
        private final AtomicInteger offsetReads;

        CountingToken(int start, AtomicInteger offsetReads) {
            super(WurstParser.ID, "declaration");
            setStartIndex(start);
            setStopIndex(start + 1);
            this.offsetReads = offsetReads;
        }

        @Override
        public int getStartIndex() {
            offsetReads.incrementAndGet();
            return super.getStartIndex();
        }
    }
}
