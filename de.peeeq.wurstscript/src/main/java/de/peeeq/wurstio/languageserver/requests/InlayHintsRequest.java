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
import de.peeeq.wurstscript.ast.ExprBoolVal;
import de.peeeq.wurstscript.ast.ExprFunctionCall;
import de.peeeq.wurstscript.ast.ExprIntVal;
import de.peeeq.wurstscript.ast.ExprMember;
import de.peeeq.wurstscript.ast.ExprMemberMethod;
import de.peeeq.wurstscript.ast.ExprNewObject;
import de.peeeq.wurstscript.ast.ExprNull;
import de.peeeq.wurstscript.ast.ExprRealVal;
import de.peeeq.wurstscript.ast.ExprStringVal;
import de.peeeq.wurstscript.ast.FuncRef;
import de.peeeq.wurstscript.ast.NameRef;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.InlayHintKind;
import org.eclipse.lsp4j.InlayHintParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InlayHintsRequest extends UserRequest<List<InlayHint>> {

    private static final Map<WFile, List<InlayHint>> lastStableHintsByFile = new ConcurrentHashMap<>();
    private static final int HINT_SCORE_THRESHOLD = 2;
    private static final Set<String> OBVIOUS_STRING_PARAMS = new HashSet<>(List.of(
            "message", "text", "name", "title", "label", "path", "url", "key"
    ));
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
                List<String> paramTypes = f.getParameterTypes().stream()
                        .map(t -> t.toPrettyString())
                        .collect(Collectors.toList());
                addParameterHints(hints, call.getArgs(), f.getParameterNames(), paramTypes);
            }
            return;
        }
        if (e instanceof ExprMemberMethod) {
            ExprMemberMethod call = (ExprMemberMethod) e;
            FuncLink f = call.attrFuncLink();
            if (f != null) {
                List<String> paramTypes = f.getParameterTypes().stream()
                        .map(t -> t.toPrettyString())
                        .collect(Collectors.toList());
                addParameterHints(hints, call.getArgs(), f.getParameterNames(), paramTypes);
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
            List<String> paramTypes = constructorDef.getParameters().stream()
                    .map(p -> p.attrTyp().toPrettyString())
                    .collect(Collectors.toList());
            addParameterHints(hints, exprNew.getArgs(), paramNames, paramTypes);
        }
    }

    private void addParameterHints(List<InlayHint> hints, Arguments args, List<String> paramNames, List<String> paramTypes) {
        int count = Math.min(args.size(), paramNames.size());
        Map<String, Integer> typeFrequencies = typeFrequencies(paramTypes);
        for (int i = 0; i < count; i++) {
            Expr arg = args.get(i);
            String paramName = paramNames.get(i);
            if (paramName == null || paramName.isEmpty()) {
                continue;
            }
            String paramType = i < paramTypes.size() ? paramTypes.get(i) : "";
            if (hintScore(arg, paramName, paramType, typeFrequencies) < HINT_SCORE_THRESHOLD) {
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

    private int hintScore(Expr arg, String paramName, String paramType, Map<String, Integer> typeFrequencies) {
        int score = 0;
        String argName = argumentName(arg);
        String normalizedParam = normalize(paramName);
        String normalizedArg = normalize(argName);

        if (arg instanceof ExprIntVal || arg instanceof ExprRealVal || arg instanceof ExprNull) {
            score += 3;
        } else if (arg instanceof ExprBoolVal) {
            score += 2;
            if (looksLikeBooleanFlagName(paramName)) {
                score -= 2;
            }
        } else if (arg instanceof ExprMember) {
            // Property/static member accesses tend to be less obvious than plain local variable names.
            score += 2;
        } else if (arg instanceof ExprStringVal) {
            if (OBVIOUS_STRING_PARAMS.contains(normalizedParam)) {
                score -= 2;
            }
        }

        if (argName != null && !argName.isEmpty() && argName.length() <= 2) {
            score += 2;
        }

        if (!paramType.isEmpty() && typeFrequencies.getOrDefault(paramType, 0) > 1) {
            score += 2;
        }

        if (isSelfDescribing(normalizedArg, normalizedParam)) {
            score -= 3;
        }

        return score;
    }

    private Map<String, Integer> typeFrequencies(List<String> paramTypes) {
        Map<String, Integer> result = new HashMap<>();
        for (String t : paramTypes) {
            if (t == null || t.isEmpty()) {
                continue;
            }
            result.put(t, result.getOrDefault(t, 0) + 1);
        }
        return result;
    }

    private String argumentName(Expr arg) {
        if (arg instanceof NameRef) {
            return ((NameRef) arg).getVarName();
        }
        if (arg instanceof FuncRef) {
            return ((FuncRef) arg).getFuncName();
        }
        return "";
    }

    private boolean looksLikeBooleanFlagName(String paramName) {
        String n = normalize(paramName);
        return n.startsWith("is")
                || n.startsWith("has")
                || n.startsWith("can")
                || n.startsWith("enable")
                || n.startsWith("enabled")
                || n.startsWith("allow");
    }

    private boolean isSelfDescribing(String normalizedArg, String normalizedParam) {
        if (normalizedArg.isEmpty() || normalizedParam.isEmpty()) {
            return false;
        }
        if (normalizedArg.equals(normalizedParam)) {
            return true;
        }
        if (normalizedArg.length() >= 3 && normalizedParam.contains(normalizedArg)) {
            return true;
        }
        return normalizedParam.length() >= 3 && normalizedArg.contains(normalizedParam);
    }

    private String normalize(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = Character.toLowerCase(s.charAt(i));
            if (Character.isLetterOrDigit(c)) {
                out.append(c);
            }
        }
        return out.toString();
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
