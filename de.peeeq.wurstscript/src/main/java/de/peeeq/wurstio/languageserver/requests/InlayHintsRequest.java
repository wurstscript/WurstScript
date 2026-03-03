package de.peeeq.wurstio.languageserver.requests;

import de.peeeq.wurstio.languageserver.BufferManager;
import de.peeeq.wurstio.languageserver.Convert;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.ast.Arguments;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.InlayHintKind;
import org.eclipse.lsp4j.InlayHintParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InlayHintsRequest extends UserRequest<List<InlayHint>> {

    private static final Map<WFile, List<InlayHint>> lastStableHintsByFile = new ConcurrentHashMap<>();
    private final WFile filename;
    private final String buffer;
    private final Range requestedRange;

    public InlayHintsRequest(InlayHintParams params, BufferManager bufferManager) {
        this.filename = WFile.create(params.getTextDocument().getUri());
        this.buffer = bufferManager.getBuffer(params.getTextDocument());
        this.requestedRange = params.getRange();
    }

    @Override
    public List<InlayHint> execute(ModelManager modelManager) {
        CompilationUnit cu = modelManager.replaceCompilationUnitContent(filename, buffer, false);
        if (cu == null) {
            return filterByRange(lastStableHintsByFile.getOrDefault(filename, Collections.emptyList()));
        }

        List<InlayHint> hints = new ArrayList<>();
        ArrayDeque<Element> todo = new ArrayDeque<>();
        todo.push(cu);
        while (!todo.isEmpty()) {
            Element e = todo.pop();
            collectHints(hints, e);
            for (int i = e.size() - 1; i >= 0; i--) {
                todo.push(e.get(i));
            }
        }

        if (hasParseErrorsForFile(modelManager)) {
            return filterByRange(lastStableHintsByFile.getOrDefault(filename, Collections.emptyList()));
        }

        lastStableHintsByFile.put(filename, hints);
        return filterByRange(hints);
    }

    private void collectHints(List<InlayHint> hints, Element e) {
        if (e instanceof ExprFunctionCall) {
            ExprFunctionCall call = (ExprFunctionCall) e;
            FuncLink f = call.attrFuncLink();
            if (f != null) {
                addParameterHints(hints, call.getArgs(), f.getParameterNames());
            }
            return;
        }
        if (e instanceof ExprMemberMethod) {
            ExprMemberMethod call = (ExprMemberMethod) e;
            FuncLink f = call.attrFuncLink();
            if (f != null) {
                addParameterHints(hints, call.getArgs(), f.getParameterNames());
            }
            return;
        }
        if (e instanceof ExprNewObject) {
            ExprNewObject exprNew = (ExprNewObject) e;
            ConstructorDef constructorDef = exprNew.attrConstructorDef();
            if (constructorDef == null) {
                return;
            }
            List<String> paramNames = new ArrayList<>();
            constructorDef.getParameters().forEach(p -> paramNames.add(p.getName()));
            addParameterHints(hints, exprNew.getArgs(), paramNames);
        }
    }

    private void addParameterHints(List<InlayHint> hints, Arguments args, List<String> paramNames) {
        int count = Math.min(args.size(), paramNames.size());
        for (int i = 0; i < count; i++) {
            Expr arg = args.get(i);
            String paramName = paramNames.get(i);
            if (paramName == null || paramName.isEmpty()) {
                continue;
            }
            Position pos = argumentStart(arg);
            InlayHint hint = new InlayHint();
            hint.setPosition(pos);
            hint.setLabel(paramName + ":");
            hint.setKind(InlayHintKind.Parameter);
            hint.setPaddingRight(true);
            hints.add(hint);
        }
    }

    private Position argumentStart(Expr arg) {
        Range sourceRange = Convert.range(arg);
        Position sourceStart = sourceRange.getStart();
        if (sourceStart.getLine() >= 0 && sourceStart.getCharacter() >= 0) {
            return sourceStart;
        }
        return Convert.errorRange(arg).getStart();
    }

    private boolean hasParseErrorsForFile(ModelManager modelManager) {
        List<CompileError> parseErrors = modelManager.getParseErrors();
        for (CompileError err : parseErrors) {
            if (err.getSource() != null && WFile.create(err.getSource().getFile()).equals(filename)) {
                return true;
            }
        }
        return false;
    }

    private List<InlayHint> filterByRange(List<InlayHint> hints) {
        return hints.stream()
                .filter(h -> isInRequestedRange(h.getPosition()))
                .collect(Collectors.toList());
    }

    private boolean isInRequestedRange(Position pos) {
        if (pos.getLine() < requestedRange.getStart().getLine() || pos.getLine() > requestedRange.getEnd().getLine()) {
            return false;
        }
        if (pos.getLine() == requestedRange.getStart().getLine()
                && pos.getCharacter() < requestedRange.getStart().getCharacter()) {
            return false;
        }
        if (pos.getLine() == requestedRange.getEnd().getLine()
                && pos.getCharacter() > requestedRange.getEnd().getCharacter()) {
            return false;
        }
        return true;
    }
}
