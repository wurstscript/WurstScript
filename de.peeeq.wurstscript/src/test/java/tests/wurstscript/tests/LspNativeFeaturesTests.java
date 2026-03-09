package tests.wurstscript.tests;

import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.JassDocService;
import de.peeeq.wurstio.languageserver.ModelManagerImpl;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.languageserver.requests.CodeActionRequest;
import de.peeeq.wurstio.languageserver.requests.DocumentSymbolRequest;
import de.peeeq.wurstio.languageserver.requests.GetDefinition;
import de.peeeq.wurstio.languageserver.requests.HoverInfo;
import de.peeeq.wurstio.languageserver.requests.InlayHintsRequest;
import de.peeeq.wurstio.languageserver.requests.PrepareRenameRequest;
import de.peeeq.wurstio.languageserver.requests.RenameRequest;
import de.peeeq.wurstio.languageserver.requests.SemanticTokensRequest;
import de.peeeq.wurstio.languageserver.requests.SignatureInfo;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.InlayHintParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PrepareRenameDefaultBehavior;
import org.eclipse.lsp4j.PrepareRenameParams;
import org.eclipse.lsp4j.PrepareRenameResult;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.messages.Either3;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class LspNativeFeaturesTests extends WurstLanguageServerTest {

    @Test
    public void renameIncludesDeclarationAndReference() throws IOException {
        CompletionTestData data = input(
                "package test",
                "function fo|o()",
                "init",
                "    foo()",
                "endpackage"
        );
        TestContext ctx = createContext(data, data.buffer);

        RenameParams params = new RenameParams();
        params.setTextDocument(new TextDocumentIdentifier(ctx.uri));
        params.setPosition(new Position(data.line, data.column));
        params.setNewName("bar");

        WorkspaceEdit edit = new RenameRequest(params, ctx.bufferManager).execute(ctx.modelManager);
        List<TextEdit> edits = allTextEdits(edit);
        assertTrue(edits.size() >= 2, "Expected declaration and usage to be renamed.");
        Set<Integer> startLines = edits.stream().map(t -> t.getRange().getStart().getLine()).collect(Collectors.toSet());
        assertTrue(startLines.contains(1), "Expected declaration line to be included.");
        assertTrue(startLines.contains(3), "Expected call site line to be included.");
    }

    @Test
    public void signatureHelpUsesUnsavedBuffer() throws IOException {
        CompletionTestData data = input(
                "package test",
                "function foo(int a, int b)",
                "init",
                "    fo|o(1, 2)",
                "endpackage"
        );
        String oldDiskContent = String.join("\n",
                "package test",
                "function foo(int a)",
                "init",
                "    foo(1)",
                "endpackage",
                "");
        TestContext ctx = createContext(data, oldDiskContent);

        TextDocumentPositionParams params = new TextDocumentPositionParams(
                new TextDocumentIdentifier(ctx.uri),
                new Position(data.line, data.column)
        );
        SignatureHelp help = new SignatureInfo(params, ctx.bufferManager).execute(ctx.modelManager);
        assertEquals(help.getSignatures().size(), 1);
        assertEquals(help.getSignatures().get(0).getParameters().size(), 2);
    }

    @Test
    public void codeActionsReturnQuickFixWithWorkspaceEdit() throws IOException {
        CompletionTestData data = input(
                "package test",
                "init",
                "    mis|sing()",
                "endpackage"
        );
        TestContext ctx = createContext(data, data.buffer);

        CodeActionParams params = new CodeActionParams();
        params.setTextDocument(new TextDocumentIdentifier(ctx.uri));
        params.setRange(new Range(new Position(data.line, data.column), new Position(data.line, data.column)));
        Diagnostic d = new Diagnostic();
        d.setRange(params.getRange());
        d.setMessage("unresolved");
        params.setContext(new CodeActionContext(Collections.singletonList(d)));

        List<Either<org.eclipse.lsp4j.Command, CodeAction>> actions =
                new CodeActionRequest(params, ctx.bufferManager).execute(ctx.modelManager);
        List<CodeAction> codeActions = actions.stream()
                .filter(Either::isRight)
                .map(Either::getRight)
                .collect(Collectors.toList());

        assertFalse(codeActions.isEmpty(), "Expected at least one code action.");
        assertTrue(codeActions.stream().anyMatch(a -> CodeActionKind.QuickFix.equals(a.getKind())));
        assertTrue(codeActions.stream().anyMatch(a -> a.getEdit() != null));
    }

    @Test
    public void documentSymbolsHaveSelectionInsideRange() throws IOException {
        CompletionTestData data = input(
            "package test",
            "class C",
            "    int x = 1",
            "    function f(int a)",
            "init",
            "    skip",
            "endpackage"
        );
        TestContext ctx = createContext(data, data.buffer);

        DocumentSymbolParams params = new DocumentSymbolParams(new TextDocumentIdentifier(ctx.uri));
        List<Either<org.eclipse.lsp4j.SymbolInformation, DocumentSymbol>> symbols =
            new DocumentSymbolRequest(params).execute(ctx.modelManager);
        List<DocumentSymbol> docs = symbols.stream()
            .filter(Either::isRight)
            .map(Either::getRight)
            .collect(Collectors.toList());
        assertFalse(docs.isEmpty());
        for (DocumentSymbol s : docs) {
            assertSelectionContainedRecursive(s);
        }
    }

    @Test
    public void codeActionCanReplaceVarWithLetForConstantLocal() throws IOException {
        CompletionTestData data = input(
            "package test",
            "init",
            "    va|r value = 1",
            "endpackage"
        );
        TestContext ctx = createContext(data, data.buffer);

        CodeActionParams params = new CodeActionParams();
        params.setTextDocument(new TextDocumentIdentifier(ctx.uri));
        params.setRange(new Range(new Position(data.line, data.column), new Position(data.line, data.column)));
        Diagnostic d = new Diagnostic();
        d.setRange(params.getRange());
        d.setMessage("Constant local variables should be defined using 'let'.");
        params.setContext(new CodeActionContext(Collections.singletonList(d)));

        List<CodeAction> codeActions = new CodeActionRequest(params, ctx.bufferManager).execute(ctx.modelManager).stream()
            .filter(Either::isRight)
            .map(Either::getRight)
            .collect(Collectors.toList());

        CodeAction fix = codeActions.stream()
            .filter(a -> "Use 'let' for constant local".equals(a.getTitle()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("expected let quickfix, got: " +
                codeActions.stream().map(CodeAction::getTitle).collect(Collectors.toList())));

        TextEdit edit = allTextEdits(fix.getEdit()).get(0);
        assertEquals(edit.getRange().getStart().getLine(), 2);
        assertEquals(edit.getRange().getStart().getCharacter(), 4);
        assertEquals(edit.getRange().getEnd().getCharacter(), 7);
        assertEquals(edit.getNewText(), "let");
    }

    @Test
    public void codeActionCanRemoveUnusedImport() throws IOException {
        CompletionTestData data = input(
            "package test",
            "import Wur|st",
            "init",
            "endpackage"
        );
        TestContext ctx = createContext(data, data.buffer);

        CodeActionParams params = new CodeActionParams();
        params.setTextDocument(new TextDocumentIdentifier(ctx.uri));
        params.setRange(new Range(new Position(data.line, data.column), new Position(data.line, data.column)));
        Diagnostic d = new Diagnostic();
        d.setRange(params.getRange());
        d.setMessage("The import Wurst is never used");
        params.setContext(new CodeActionContext(Collections.singletonList(d)));

        List<CodeAction> codeActions = new CodeActionRequest(params, ctx.bufferManager).execute(ctx.modelManager).stream()
            .filter(Either::isRight)
            .map(Either::getRight)
            .collect(Collectors.toList());

        CodeAction fix = codeActions.stream()
            .filter(a -> "Remove unused import Wurst".equals(a.getTitle()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("expected import removal quickfix, got: " +
                codeActions.stream().map(CodeAction::getTitle).collect(Collectors.toList())));

        TextEdit edit = allTextEdits(fix.getEdit()).get(0);
        assertEquals(edit.getRange().getStart().getLine(), 1);
        assertEquals(edit.getRange().getStart().getCharacter(), 0);
        assertEquals(edit.getRange().getEnd().getLine(), 2);
        assertEquals(edit.getRange().getEnd().getCharacter(), 0);
        assertEquals(edit.getNewText(), "");
    }

    @Test
    public void codeActionCanRemoveRedundantImport() throws IOException {
        CompletionTestData data = input(
            "package test",
            "import BuildingCon|stants",
            "init",
            "endpackage"
        );
        TestContext ctx = createContext(data, data.buffer);

        CodeActionParams params = new CodeActionParams();
        params.setTextDocument(new TextDocumentIdentifier(ctx.uri));
        params.setRange(new Range(new Position(data.line, data.column), new Position(data.line, data.column)));
        Diagnostic d = new Diagnostic();
        d.setRange(params.getRange());
        d.setMessage("The import BuildingConstants can be removed, because it is already included in CFBuilding.");
        params.setContext(new CodeActionContext(Collections.singletonList(d)));

        List<CodeAction> codeActions = new CodeActionRequest(params, ctx.bufferManager).execute(ctx.modelManager).stream()
            .filter(Either::isRight)
            .map(Either::getRight)
            .collect(Collectors.toList());

        CodeAction fix = codeActions.stream()
            .filter(a -> "Remove redundant import BuildingConstants".equals(a.getTitle()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("expected redundant import quickfix, got: " +
                codeActions.stream().map(CodeAction::getTitle).collect(Collectors.toList())));

        TextEdit edit = allTextEdits(fix.getEdit()).get(0);
        assertEquals(edit.getRange().getStart().getLine(), 1);
        assertEquals(edit.getRange().getEnd().getLine(), 2);
        assertEquals(edit.getNewText(), "");
    }

    @Test
    public void codeActionCanPrefixUnreadVariableWithUnderscore() throws IOException {
        CompletionTestData data = input(
            "package test",
            "init",
            "    int hu|P = 1",
            "endpackage"
        );
        TestContext ctx = createContext(data, data.buffer);

        CodeActionParams params = new CodeActionParams();
        params.setTextDocument(new TextDocumentIdentifier(ctx.uri));
        params.setRange(new Range(new Position(data.line, data.column), new Position(data.line, data.column)));
        Diagnostic d = new Diagnostic();
        d.setRange(params.getRange());
        d.setMessage("The variable huP is never read. If intentional, prefix with \"_\" to suppress this warning.");
        params.setContext(new CodeActionContext(Collections.singletonList(d)));

        List<CodeAction> codeActions = new CodeActionRequest(params, ctx.bufferManager).execute(ctx.modelManager).stream()
            .filter(Either::isRight)
            .map(Either::getRight)
            .collect(Collectors.toList());

        CodeAction fix = codeActions.stream()
            .filter(a -> "Prefix 'huP' with '_'".equals(a.getTitle()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("expected underscore quickfix, got: " +
                codeActions.stream().map(CodeAction::getTitle).collect(Collectors.toList())));

        TextEdit edit = allTextEdits(fix.getEdit()).get(0);
        assertEquals(edit.getRange().getStart().getLine(), 2);
        assertEquals(edit.getRange().getStart().getCharacter(), data.column);
        assertEquals(edit.getRange().getEnd().getCharacter(), data.column);
        assertEquals(edit.getNewText(), "_");
    }

    @Test
    public void codeActionCanNormalizeMixedIndentation() throws IOException {
        CompletionTestData data = input(
            "package test",
            "init",
            "\t  sk|ip",
            "endpackage"
        );
        TestContext ctx = createContext(data, data.buffer);

        CodeActionParams params = new CodeActionParams();
        params.setTextDocument(new TextDocumentIdentifier(ctx.uri));
        params.setRange(new Range(new Position(data.line, data.column), new Position(data.line, data.column)));
        Diagnostic d = new Diagnostic();
        d.setRange(params.getRange());
        d.setMessage("Mixing tabs and spaces for indentation.");
        params.setContext(new CodeActionContext(Collections.singletonList(d)));

        List<CodeAction> codeActions = new CodeActionRequest(params, ctx.bufferManager).execute(ctx.modelManager).stream()
            .filter(Either::isRight)
            .map(Either::getRight)
            .collect(Collectors.toList());

        CodeAction fix = codeActions.stream()
            .filter(a -> "Normalize indentation on this line".equals(a.getTitle()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("expected indentation quickfix, got: " +
                codeActions.stream().map(CodeAction::getTitle).collect(Collectors.toList())));

        TextEdit edit = allTextEdits(fix.getEdit()).get(0);
        assertEquals(edit.getRange().getStart().getLine(), 2);
        assertEquals(edit.getRange().getStart().getCharacter(), 0);
        assertEquals(edit.getRange().getEnd().getCharacter(), 3);
        assertEquals(edit.getNewText(), "      ");
    }

    @Test
    public void createFunctionQuickfixInClassInsertsUsableMethod() throws IOException {
        CompletionTestData data = input(
            "package test",
            "class C",
            "    function run()",
            "        mis|sing(1)",
            "init",
            "    new C().run()",
            "endpackage"
        );
        TestContext ctx = createContext(data, data.buffer);

        CodeActionParams params = new CodeActionParams();
        params.setTextDocument(new TextDocumentIdentifier(ctx.uri));
        params.setRange(new Range(new Position(data.line, data.column), new Position(data.line, data.column)));
        Diagnostic d = new Diagnostic();
        d.setRange(params.getRange());
        d.setMessage("Could not find function missing.");
        params.setContext(new CodeActionContext(Collections.singletonList(d)));

        List<CodeAction> codeActions = new CodeActionRequest(params, ctx.bufferManager).execute(ctx.modelManager).stream()
            .filter(Either::isRight)
            .map(Either::getRight)
            .collect(Collectors.toList());

        CodeAction fix = codeActions.stream()
            .filter(a -> "Create function missing".equals(a.getTitle()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("expected create-function quickfix, got: " +
                codeActions.stream().map(CodeAction::getTitle).collect(Collectors.toList())));

        TextEdit edit = allTextEdits(fix.getEdit()).get(0);
        assertEquals(edit.getRange().getStart().getLine(), 2);
        assertTrue(edit.getNewText().contains("\n    function missing("), "inserted = " + edit.getNewText());
        assertTrue(edit.getNewText().contains("\n        skip\n"), "inserted = " + edit.getNewText());
    }

    @Test
    public void semanticTokensAreProduced() throws IOException {
        CompletionTestData data = input(
                "package test",
                "class C",
                "function foo(int x)",
                "    string s = \"a\"",
                "    int i = 5",
                "endpackage"
        );
        TestContext ctx = createContext(data, data.buffer);

        SemanticTokensParams params = new SemanticTokensParams(new TextDocumentIdentifier(ctx.uri));
        SemanticTokens tokens = new SemanticTokensRequest(params, ctx.bufferManager).execute(ctx.modelManager);
        assertNotNull(tokens);
        assertFalse(tokens.getData().isEmpty(), "Expected semantic token data.");
        assertEquals(tokens.getData().size() % 5, 0);

        List<Integer> tokenTypes = new ArrayList<>();
        List<Integer> dataValues = tokens.getData();
        for (int i = 3; i < dataValues.size(); i += 5) {
            tokenTypes.add(dataValues.get(i));
        }
        assertTrue(tokenTypes.contains(SemanticTokensRequest.TOKEN_TYPES.indexOf(org.eclipse.lsp4j.SemanticTokenTypes.Function)));
    }

    @Test
    public void inlayHintsShowParameterNames() throws IOException {
        CompletionTestData data = input(
                "package test",
                "function foo(int amount, string name)",
                "init",
                "    foo(1, \"x\")",
                "endpackage"
        );
        TestContext ctx = createContext(data, data.buffer);

        InlayHintParams params = new InlayHintParams(
                new TextDocumentIdentifier(ctx.uri),
                new Range(new Position(0, 0), new Position(100, 0))
        );
        List<InlayHint> hints = new InlayHintsRequest(params, ctx.bufferManager).execute(ctx.modelManager);
        List<String> labels = hints.stream()
                .map(h -> h.getLabel().isLeft() ? h.getLabel().getLeft() : "")
                .collect(Collectors.toList());
        assertTrue(labels.contains("amount:"));
        assertFalse(labels.contains("name:"), "Obvious string parameter hints should be suppressed.");
    }

    @Test
    public void inlayHintsHideSelfDescribingArgumentNames() throws IOException {
        CompletionTestData data = input(
                "package test",
                "function send(string message)",
                "init",
                "    string message = \"x\"",
                "    send(message)",
                "endpackage"
        );
        TestContext ctx = createContext(data, data.buffer);

        InlayHintParams params = new InlayHintParams(
                new TextDocumentIdentifier(ctx.uri),
                new Range(new Position(0, 0), new Position(100, 0))
        );
        List<InlayHint> hints = new InlayHintsRequest(params, ctx.bufferManager).execute(ctx.modelManager);
        List<String> labels = hints.stream()
                .map(h -> h.getLabel().isLeft() ? h.getLabel().getLeft() : "")
                .collect(Collectors.toList());
        assertFalse(labels.contains("message:"));
    }

    @Test
    public void inlayHintsShowWhenParameterTypesAreRepeated() throws IOException {
        CompletionTestData data = input(
                "package test",
                "function setRange(int min, int max)",
                "init",
                "    int first = 1",
                "    int second = 2",
                "    setRange(first, second)",
                "endpackage"
        );
        TestContext ctx = createContext(data, data.buffer);

        InlayHintParams params = new InlayHintParams(
                new TextDocumentIdentifier(ctx.uri),
                new Range(new Position(0, 0), new Position(100, 0))
        );
        List<InlayHint> hints = new InlayHintsRequest(params, ctx.bufferManager).execute(ctx.modelManager);
        List<String> labels = hints.stream()
                .map(h -> h.getLabel().isLeft() ? h.getLabel().getLeft() : "")
                .collect(Collectors.toList());
        assertTrue(labels.contains("min:"));
        assertTrue(labels.contains("max:"));
    }

    @Test
    public void inlayHintForMemberAccessStartsAtReceiver() throws IOException {
        CompletionTestData data = input(
                "package test",
                "class Icons",
                "    static string iconPath = \"x\"",
                "function addPerk(string id)",
                "init",
                "    addPerk(Icons.iconPath)",
                "endpackage"
        );
        TestContext ctx = createContext(data, data.buffer);

        InlayHintParams params = new InlayHintParams(
                new TextDocumentIdentifier(ctx.uri),
                new Range(new Position(0, 0), new Position(100, 0))
        );
        List<InlayHint> hints = new InlayHintsRequest(params, ctx.bufferManager).execute(ctx.modelManager);
        InlayHint idHint = hints.stream()
                .filter(h -> h.getLabel().isLeft() && "id:".equals(h.getLabel().getLeft()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("id: inlay hint not found"));

        String line = data.buffer.split("\\R")[5];
        int expectedStart = line.indexOf("Icons.iconPath");
        assertEquals(idHint.getPosition().getLine(), 5);
        assertEquals(idHint.getPosition().getCharacter(), expectedStart);
    }

    @Test
    public void inlayHintsStayStableWhileTemporarilyUnparsable() throws IOException {
        CompletionTestData valid = input(
                "package test",
                "function foo(int amount)",
                "init",
                "    foo(1)",
                "endpackage"
        );
        TestContext ctx = createContext(valid, valid.buffer);

        InlayHintParams params = new InlayHintParams(
                new TextDocumentIdentifier(ctx.uri),
                new Range(new Position(0, 0), new Position(100, 0))
        );

        List<InlayHint> stableHints = new InlayHintsRequest(params, ctx.bufferManager).execute(ctx.modelManager);
        List<String> stableLabels = stableHints.stream()
                .map(h -> h.getLabel().isLeft() ? h.getLabel().getLeft() : "")
                .collect(Collectors.toList());
        assertTrue(stableLabels.contains("amount:"));

        String invalidBuffer = String.join("\n",
                "package test",
                "function foo(int amount)",
                "init",
                "    if (",
                "endpackage",
                "");
        ctx.bufferManager.updateFile(WFile.create(ctx.uri), invalidBuffer);

        List<InlayHint> duringParseError = new InlayHintsRequest(params, ctx.bufferManager).execute(ctx.modelManager);
        List<String> errorLabels = duringParseError.stream()
                .map(h -> h.getLabel().isLeft() ? h.getLabel().getLeft() : "")
                .collect(Collectors.toList());
        assertTrue(errorLabels.contains("amount:"), "Expected cached hints to be reused during parse errors.");
    }

    @Test
    public void inlayHintsDoNotCrashOnCascadeCodeLambdaWithMissingTarget() throws IOException {
        CompletionTestData data = input(
                "package test",
                "class TriggerWrap",
                "    function addAction(code c)",
                "        skip",
                "init",
                "    let t = new TriggerWrap()",
                "        ..missing() ->",
                "            skip",
                "endpackage"
        );
        TestContext ctx = createContext(data, data.buffer);

        InlayHintParams params = new InlayHintParams(
                new TextDocumentIdentifier(ctx.uri),
                new Range(new Position(0, 0), new Position(100, 0))
        );

        List<InlayHint> hints = new InlayHintsRequest(params, ctx.bufferManager).execute(ctx.modelManager);
        assertNotNull(hints);
    }

    @Test
    public void prepareRenameReturnsSymbolRange() throws IOException {
        CompletionTestData data = input(
                "package test",
                "function foo()",
                "init",
                "    fo|o()",
                "endpackage"
        );
        TestContext ctx = createContext(data, data.buffer);

        PrepareRenameParams params = new PrepareRenameParams();
        params.setTextDocument(new TextDocumentIdentifier(ctx.uri));
        params.setPosition(new Position(data.line, data.column));
        Either3<Range, PrepareRenameResult, PrepareRenameDefaultBehavior> result =
                new PrepareRenameRequest(params, ctx.bufferManager).execute(ctx.modelManager);
        assertTrue(result.isFirst());
        assertEquals(result.getFirst().getStart().getLine(), 3);
    }

    @Test
    public void typeDefinitionRequestReturnsTypeTarget() throws IOException {
        CompletionTestData data = input(
                "package test",
                "class C",
                "init",
                "    C c = null",
                "    c|",
                "endpackage"
        );
        TestContext ctx = createContext(data, data.buffer);

        TextDocumentPositionParams pos = new TextDocumentPositionParams(
                new TextDocumentIdentifier(ctx.uri),
                new Position(data.line, data.column)
        );
        Either<List<? extends org.eclipse.lsp4j.Location>, List<? extends org.eclipse.lsp4j.LocationLink>> result =
                new GetDefinition(pos, ctx.bufferManager, GetDefinition.LookupType.TYPE_DEFINITION).execute(ctx.modelManager);
        assertTrue(result.isLeft());
        assertFalse(result.getLeft().isEmpty());
        assertEquals(result.getLeft().get(0).getRange().getStart().getLine(), 1);
    }

    @Test
    public void hoverUsesJassDocFallbackForBuiltinFunction() throws IOException {
        CompletionTestData data = input(
            "package test",
            "init",
            "    DisplayTextToPl|ayer(null, 0., 0., \"x\")",
            "endpackage"
        );
        TestContext ctx = createContext(data, data.buffer);

        JassDocService.setTestLookup(k ->
            Objects.equals(k.symbolName(), "DisplayTextToPlayer")
                && k.symbolKind() == JassDocService.SymbolKind.FUNCTION
                ? "Shows text to a specific player."
                : null
        );
        JassDocService.getInstance().clearCacheForTests();
        try {
            Hover hover = new HoverInfo(
                new TextDocumentPositionParams(new TextDocumentIdentifier(ctx.uri), new Position(data.line, data.column)),
                ctx.bufferManager
            ).execute(ctx.modelManager);

            List<String> text = hover.getContents().getLeft().stream()
                .map(e -> e.isLeft() ? e.getLeft() : e.getRight().getValue())
                .collect(Collectors.toList());
            assertTrue(text.stream().anyMatch(t -> t.contains("Shows text to a specific player.")), "hover text = " + text);
        } finally {
            JassDocService.setTestLookup(null);
            JassDocService.getInstance().clearCacheForTests();
        }
    }

    @Test
    public void hoverUsesJassDocFromSingleNativeWrapperCall() throws IOException {
        CompletionTestData data = input(
            "package test",
            "function mySin(real x) returns real",
            "    return Sin(x)",
            "init",
            "    myS|in(1.0)",
            "endpackage"
        );
        TestContext ctx = createContext(data, data.buffer);

        JassDocService.setTestLookup(k ->
            Objects.equals(k.symbolName(), "Sin")
                && k.symbolKind() == JassDocService.SymbolKind.FUNCTION
                ? "Returns the sine of x."
                : null
        );
        JassDocService.getInstance().clearCacheForTests();
        try {
            Hover hover = new HoverInfo(
                new TextDocumentPositionParams(new TextDocumentIdentifier(ctx.uri), new Position(data.line, data.column)),
                ctx.bufferManager
            ).execute(ctx.modelManager);

            List<String> text = hover.getContents().getLeft().stream()
                .map(e -> e.isLeft() ? e.getLeft() : e.getRight().getValue())
                .collect(Collectors.toList());
            assertTrue(text.stream().anyMatch(t -> t.contains("_JassDoc_")), "hover text = " + text);
            assertTrue(text.stream().anyMatch(t -> t.contains("Returns the sine of x.")), "hover text = " + text);
        } finally {
            JassDocService.setTestLookup(null);
            JassDocService.getInstance().clearCacheForTests();
        }
    }

    @Test
    public void hoverUsesJassDocFromSingleExtensionWrapperCall() throws IOException {
        CompletionTestData data = input(
            "package test",
            "function real.nativeSin() returns real",
            "    return Sin(this)",
            "init",
            "    1.0.nativeS|in()",
            "endpackage"
        );
        TestContext ctx = createContext(data, data.buffer);

        JassDocService.setTestLookup(k ->
            Objects.equals(k.symbolName(), "Sin")
                && k.symbolKind() == JassDocService.SymbolKind.FUNCTION
                ? "Returns the sine of x."
                : null
        );
        JassDocService.getInstance().clearCacheForTests();
        try {
            Hover hover = new HoverInfo(
                new TextDocumentPositionParams(new TextDocumentIdentifier(ctx.uri), new Position(data.line, data.column)),
                ctx.bufferManager
            ).execute(ctx.modelManager);

            List<String> text = hover.getContents().getLeft().stream()
                .map(e -> e.isLeft() ? e.getLeft() : e.getRight().getValue())
                .collect(Collectors.toList());
            assertTrue(text.stream().anyMatch(t -> t.contains("_JassDoc_")), "hover text = " + text);
            assertTrue(text.stream().anyMatch(t -> t.contains("Returns the sine of x.")), "hover text = " + text);
        } finally {
            JassDocService.setTestLookup(null);
            JassDocService.getInstance().clearCacheForTests();
        }
    }

    @Test
    public void completionUsesJassDocFallbackForBuiltinFunction() throws IOException {
        CompletionTestData data = input(
            "package test",
            "init",
            "    DisplayTextToPl|",
            "endpackage"
        );
        TestContext ctx = createContext(data, data.buffer);

        JassDocService.setTestLookup(k ->
            Objects.equals(k.symbolName(), "DisplayTextToPlayer")
                && k.symbolKind() == JassDocService.SymbolKind.FUNCTION
                ? "Shows text to a specific player."
                : null
        );
        JassDocService.getInstance().clearCacheForTests();
        try {
            CompletionList completions = new de.peeeq.wurstio.languageserver.requests.GetCompletions(
                new org.eclipse.lsp4j.CompletionParams(new TextDocumentIdentifier(ctx.uri), new Position(data.line, data.column)),
                ctx.bufferManager
            ).execute(ctx.modelManager);
            assertNotNull(completions);
            String doc = completions.getItems().stream()
                .filter(i -> "DisplayTextToPlayer".equals(i.getLabel()))
                .map(i -> {
                    if (i.getDocumentation() == null) {
                        return null;
                    }
                    return i.getDocumentation().isLeft()
                        ? i.getDocumentation().getLeft()
                        : i.getDocumentation().getRight().getValue();
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("");
            assertTrue(doc.contains("Shows text to a specific player."), "completion doc = " + doc);
        } finally {
            JassDocService.setTestLookup(null);
            JassDocService.getInstance().clearCacheForTests();
        }
    }

    @Test
    public void signatureHelpUsesJassDocFallbackForBuiltinFunction() throws IOException {
        CompletionTestData data = input(
            "package test",
            "init",
            "    DisplayTextToPlayer(nu|ll, 0., 0., \"x\")",
            "endpackage"
        );
        TestContext ctx = createContext(data, data.buffer);

        JassDocService.setTestLookup(k ->
            Objects.equals(k.symbolName(), "DisplayTextToPlayer")
                && k.symbolKind() == JassDocService.SymbolKind.FUNCTION
                ? "Shows text to a specific player."
                : null
        );
        JassDocService.getInstance().clearCacheForTests();
        try {
            SignatureHelp help = new SignatureInfo(
                new TextDocumentPositionParams(new TextDocumentIdentifier(ctx.uri), new Position(data.line, data.column)),
                ctx.bufferManager
            ).execute(ctx.modelManager);
            assertFalse(help.getSignatures().isEmpty());
            String doc = help.getSignatures().get(0).getDocumentation().getLeft();
            assertTrue(doc.contains("Shows text to a specific player."), "signature doc = " + doc);
        } finally {
            JassDocService.setTestLookup(null);
            JassDocService.getInstance().clearCacheForTests();
        }
    }

    private List<TextEdit> allTextEdits(WorkspaceEdit edit) {
        if (edit.getChanges() != null && !edit.getChanges().isEmpty()) {
            return edit.getChanges().values().stream().flatMap(List::stream).collect(Collectors.toList());
        }
        if (edit.getDocumentChanges() == null) {
            return Collections.emptyList();
        }
        return edit.getDocumentChanges().stream()
                .filter(Either::isLeft)
                .flatMap(dc -> dc.getLeft().getEdits().stream())
                .collect(Collectors.toList());
    }

    private void assertSelectionContainedRecursive(DocumentSymbol s) {
        assertTrue(rangeContains(s.getRange(), s.getSelectionRange()),
            "selectionRange must be contained in range for symbol " + s.getName()
                + " range=" + s.getRange() + " selection=" + s.getSelectionRange());
        if (s.getChildren() != null) {
            for (DocumentSymbol child : s.getChildren()) {
                assertSelectionContainedRecursive(child);
            }
        }
    }

    private boolean rangeContains(Range outer, Range inner) {
        return compare(inner.getStart(), outer.getStart()) >= 0
            && compare(inner.getEnd(), outer.getEnd()) <= 0;
    }

    private int compare(Position a, Position b) {
        if (a.getLine() != b.getLine()) {
            return Integer.compare(a.getLine(), b.getLine());
        }
        return Integer.compare(a.getCharacter(), b.getCharacter());
    }

    private TestContext createContext(CompletionTestData data, String diskContent) throws IOException {
        File projectFolder = new File("./temp/lspNative/" + System.nanoTime());
        File wurstFolder = new File(projectFolder, "wurst");
        Files.createDirectories(wurstFolder.toPath());

        File testFile = new File(wurstFolder, "test.wurst");
        File wurstFile = new File(wurstFolder, "Wurst.wurst");
        Files.writeString(testFile.toPath(), diskContent);
        Files.writeString(wurstFile.toPath(), "package Wurst\n");

        BufferManager bufferManager = new BufferManager();
        ModelManagerImpl modelManager = new ModelManagerImpl(projectFolder.getAbsoluteFile(), bufferManager);
        modelManager.buildProject();

        String uri = testFile.toURI().toString();
        bufferManager.updateFile(WFile.create(uri), data.buffer);
        return new TestContext(bufferManager, modelManager, uri);
    }

    private static class TestContext {
        private final BufferManager bufferManager;
        private final ModelManagerImpl modelManager;
        private final String uri;

        private TestContext(BufferManager bufferManager, ModelManagerImpl modelManager, String uri) {
            this.bufferManager = bufferManager;
            this.modelManager = modelManager;
            this.uri = uri;
        }
    }
}
