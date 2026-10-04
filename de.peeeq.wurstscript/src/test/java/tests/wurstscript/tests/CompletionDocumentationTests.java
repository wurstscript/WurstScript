package tests.wurstscript.tests;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.JassDocService;
import de.peeeq.wurstio.languageserver.ModelManagerImpl;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.languageserver.requests.CompletionDocumentation;
import de.peeeq.wurstio.languageserver.requests.GetCompletions;
import de.peeeq.wurstscript.antlr.WurstLexer;
import de.peeeq.wurstscript.ast.AstElementWithSource;
import de.peeeq.wurstscript.parser.TriviaIndex;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.*;

public class CompletionDocumentationTests extends WurstLanguageServerTest {
    @Test
    public void completionDoesNotReadDocumentationUntilResolve() throws IOException {
        CompletionTestData input = input("package test", "/** docs for foo */", "function fooBar()",
                "init", "    foo|");
        try (Context context = new Context(input)) {
            AtomicInteger reads = new AtomicInteger();
            List<Token> tokens = new ArrayList<>();
            for (Token token : new WurstLexer(CharStreams.fromString(input.buffer)).getAllTokens()) {
                tokens.add(new CommonToken(token) {
                    @Override
                    public int getStartIndex() {
                        reads.incrementAndGet();
                        return super.getStartIndex();
                    }
                });
            }
            context.model.getCompilationUnit(context.file).getCuInfo().setTriviaIndex(TriviaIndex.fromTokens(tokens));
            reads.set(0);

            CompletionItem item = context.complete("fooBar");
            assertNull(item.getDocumentation());
            assertNotNull(item.getData());
            assertEquals(reads.get(), 0, "Collecting candidates must not look up their comments");
            // Exercise the JSON data clients return over the protocol, without retaining Java data objects.
            item.setData(JsonParser.parseString(item.getData().toString()));
            JsonObject before = new Gson().toJsonTree(item).getAsJsonObject();
            assertSame(context.docs.new Resolve(item).execute(context.model), item);
            assertTrue(item.getDocumentation().getLeft().contains("docs for foo"));
            assertTrue(reads.get() > 0, "Resolve must actually fetch the documentation");
            JsonObject after = new Gson().toJsonTree(item).getAsJsonObject();
            after.remove("documentation");
            assertEquals(after, before, "Resolve must preserve ranking, snippets, edits and all other properties");
        }
    }

    @Test
    public void resolvesDocumentationBeyondTheFormerTop24Limit() throws IOException {
        StringBuilder source = new StringBuilder("package test\n");
        for (int i = 0; i < 40; i++) {
            source.append("/** documentation ").append(i).append(" */\nfunction foo")
                    .append(String.format("%02d", i)).append("()\n");
        }
        source.append("init\n    foo|\n");
        try (Context context = new Context(input(source.toString().split("\n")))) {
            CompletionItem item = context.complete("foo39");
            assertNull(item.getDocumentation());
            context.docs.new Resolve(item).execute(context.model);
            assertTrue(item.getDocumentation().getLeft().contains("documentation 39"));
        }
    }

    @Test
    public void resolvesVariableAndConstructorDocumentation() throws IOException {
        try (Context context = new Context(input("package test", "/** variable docs */", "int fooValue = 1",
                "init", "    foo|"))) {
            CompletionItem item = context.complete("fooValue");
            context.docs.new Resolve(item).execute(context.model);
            assertTrue(item.getDocumentation().getLeft().contains("variable docs"));
        }
        try (Context context = new Context(input("package test", "class Foo", "    /** constructor docs */",
                "    construct()", "init", "    new Fo|"))) {
            CompletionItem item = context.complete("Foo");
            context.docs.new Resolve(item).execute(context.model);
            assertTrue(item.getDocumentation().getLeft().contains("constructor docs"));
        }
    }

    @Test
    public void jassDocLookupOnlyRunsForTheSelectedCompletion() throws IOException {
        AtomicInteger lookups = new AtomicInteger();
        JassDocService.setTestLookup(key -> {
            lookups.incrementAndGet();
            return "native documentation";
        });
        JassDocService.getInstance().clearCacheForTests();
        try (Context context = new Context(input("package test", "init", "    DisplayTextToPl|"))) {
            CompletionItem item = context.complete("DisplayTextToPlayer");
            assertNull(item.getDocumentation());
            assertEquals(lookups.get(), 0);
            context.docs.new Resolve(item).execute(context.model);
            assertEquals(item.getDocumentation().getRight().getKind(), "markdown");
            assertTrue(item.getDocumentation().getRight().getValue().contains("native documentation"));
            assertEquals(lookups.get(), 1);
        } finally {
            JassDocService.setTestLookup(null);
            JassDocService.getInstance().clearCacheForTests();
        }
    }

    @Test
    public void oldCompletionDoesNotResolveAfterAnUnsavedDeclarationEdit() throws IOException {
        CompletionTestData original = input("package test", "/** original */", "function fooBar()",
                "init", "    foo|");
        try (Context context = new Context(original)) {
            CompletionItem old = context.complete("fooBar");
            String updated = original.buffer.replace("original", "updated");
            context.buffers.updateFile(context.file, updated);
            context.model.syncCompilationUnitContent(context.file, updated);
            context.docs.new Resolve(old).execute(context.model);
            assertNull(old.getDocumentation(), "A replaced declaration must invalidate its old handle");
            CompletionItem current = context.complete("fooBar");
            context.docs.new Resolve(current).execute(context.model);
            assertTrue(current.getDocumentation().getLeft().contains("updated"));
        }
    }

    @Test
    public void removedDeclarationAndForeignOrMalformedDataAreIgnored() throws IOException {
        try (Context context = new Context(input("package test", "/** docs */", "function fooBar()",
                "init", "    foo|"))) {
            CompletionItem item = context.complete("fooBar");
            CompletionDocumentation otherSession = new CompletionDocumentation();
            otherSession.new Resolve(item).execute(context.model);
            assertNull(item.getDocumentation());
            context.model.removeCompilationUnit(context.file);
            context.docs.new Resolve(item).execute(context.model);
            assertNull(item.getDocumentation());
            for (Object data : List.of("garbage", Map.of("wurstCompletion", 5),
                    JsonParser.parseString("{\"wurstCompletion\":{}}"), Map.of("wurstCompletion", "missing"))) {
                item.setData(data);
                assertSame(context.docs.new Resolve(item).execute(context.model), item);
                assertNull(item.getDocumentation());
            }
            item.setData(null);
            assertSame(context.docs.new Resolve(item).execute(context.model), item);
        }
    }

    @Test
    public void resolveHandlesAreBoundedAndAcceptMapData() throws IOException {
        try (Context context = new Context(input("package test", "/** docs */", "function fooBar()",
                "init", "    foo|"))) {
            CompletionItem first = context.complete("fooBar");
            AstElementWithSource declaration = context.model.getCompilationUnit(context.file).getPackages().get(0)
                    .getElements().get(0);
            CompletionItem last = null;
            for (int i = 0; i < 1000; i++) {
                last = new CompletionItem("fooBar");
                context.docs.attach(last, declaration);
            }
            context.docs.new Resolve(first).execute(context.model);
            assertNull(first.getDocumentation(), "Old handles must be evicted");
            last.setData(new Gson().fromJson(last.getData().toString(), Map.class));
            context.docs.new Resolve(last).execute(context.model);
            assertTrue(last.getDocumentation().getLeft().contains("docs"));
        }
    }

    private static final class Context implements AutoCloseable {
        private final Path root;
        private final WFile file;
        private final BufferManager buffers = new BufferManager();
        private final ModelManagerImpl model;
        private final CompletionDocumentation docs = new CompletionDocumentation();
        private final CompletionParams params;

        Context(CompletionTestData input) throws IOException {
            root = Files.createTempDirectory("wurst-completion-documentation");
            Path wurst = Files.createDirectories(root.resolve("wurst"));
            Path source = wurst.resolve("test.wurst");
            Files.writeString(source, input.buffer);
            Files.writeString(wurst.resolve("Wurst.wurst"), "package Wurst\n");
            file = WFile.create(source);
            model = new ModelManagerImpl(root.toFile(), buffers);
            model.buildProject();
            params = new CompletionParams(new TextDocumentIdentifier(file.getUriString()),
                    new Position(input.line, input.column));
        }

        CompletionItem complete(String label) {
            CompletionList result = new GetCompletions(params, buffers, docs).execute(model);
            return result.getItems().stream().filter(item -> label.equals(item.getLabel())).findFirst().orElseThrow();
        }

        @Override
        public void close() throws IOException {
            model.clean();
            try (var paths = Files.walk(root)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(path);
                }
            }
        }
    }
}
