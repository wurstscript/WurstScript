package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.luaAst.LuaFunction;
import de.peeeq.wurstscript.luaAst.LuaStatement;
import de.peeeq.wurstscript.translation.lua.printing.LuaPrinter;
import de.peeeq.wurstscript.utils.CompilationCancellation;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;
import java.util.function.Supplier;

/** First backend artifact boundary: immutable text/frame/scalar IM, with declaration identity kept in a source index. */
public final class ScalarLuaBackend {
    private record Symbol(int id) { }
    private record Artifact(ScalarFunctionArtifact body, LuaTranslator.FunctionFrame frame) { }
    private record Fragment(LuaPrinter.PrintedStatement printed, @Nullable Symbol owner, boolean abi) { }
    private record Position(int left, int right, int line, int column, int endLine, int endColumn) { }
    private record Environment(List<String> arguments, boolean legacyJassChecks, int functionSplitLimit) {
        static Environment of(RunArgs args) { return new Environment(args.arguments(), args.isLegacyJassTypeChecks(), args.getFunctionSplitLimit()); }
    }
    private record Snapshot(List<Fragment> fragments, Map<Symbol, Artifact> functions, LuaHotReload.Manifest manifest,
                            Map<Symbol, Integer> sourceVersions, List<Position> positions, Environment environment) { }
    public record Result(String script, LuaHotReload.Manifest manifest) { }

    private final Map<FuncDef, Symbol> sourceIndex = new IdentityHashMap<>();
    private @Nullable Snapshot snapshot;
    private long sourceRevision;
    private String reason = "no completed batch output";
    public String reason() { return reason; }

    public void clear() { snapshot = null; sourceIndex.clear(); sourceRevision++; }

    /** Invalidates outstanding work tokens, not cached artifacts; artifact validity uses owner modification counts. */
    public void sourcePrepared() { sourceRevision++; }

    /** Private work contains frozen values only; editor AST/IM was read during shared preparation. */
    public final class Pending {
        private final Snapshot previous;
        private final Map<Symbol, Artifact> replacements;
        private final RunArgs args;
        private final Map<Symbol, Integer> sourceVersions;
        private final long revision;
        private Pending(Snapshot previous, Map<Symbol, Artifact> replacements, Map<Symbol, Integer> sourceVersions, RunArgs args) {
            this.previous = previous;
            this.replacements = Collections.unmodifiableMap(new LinkedHashMap<>(replacements));
            this.args = args;
            this.sourceVersions = Collections.unmodifiableMap(new LinkedHashMap<>(sourceVersions));
            this.revision = sourceRevision;
        }
        public Result compile() { return finish(this); }
    }

    public static boolean supports(RunArgs args) {
        return args.isLua() && args.isLuaHotReload() && !args.runCompiletimeFunctions() && !args.isRunTests()
            && !args.isIncludeStacktraces() && !args.isInline() && !args.isOptimize() && !args.isLocalOptimizations()
            && !args.isInjectObjects() && !args.isExportW3o() && !args.isExportObjects() && !args.isExtractImports();
    }

    public void remember(Collection<CompilationUnit> units, Supplier<Map<FuncDef, ImFunction>> sources,
                         LuaTranslator translated, RunArgs args) {
        clear();
        if (!supports(args)) { reason = "unsupported compiler options " + args.arguments(); return; }
        LuaHotReload.Manifest manifest = translated.hotReloadManifest();
        if (manifest == null) return;
        Map<FuncDef, ImFunction> sourceFunctions = sources.get();
        Map<Symbol, Artifact> functions = new LinkedHashMap<>();
        Map<Symbol, Integer> sourceVersions = new LinkedHashMap<>();
        sourceFunctions.forEach((source, function) -> {
            Symbol symbol = new Symbol(sourceIndex.size());
            sourceIndex.put(source, symbol);
            sourceVersions.put(symbol, function.modificationCount());
        });
        Map<LuaFunction, Symbol> owners = new IdentityHashMap<>();
        for (var entry : translated.translatedFunctions().entrySet()) {
            if (!(entry.getValue().getTrace() instanceof FuncDef source)) continue;
            ImFunction original = sourceFunctions.get(source);
            ScalarFunctionArtifact body = original == null ? null : ScalarFunctionArtifact.freeze(original);
            ScalarFunctionArtifact lowered = body == null ? null : body.loweredArtifact(args);
            if (lowered == null || !lowered.equals(ScalarFunctionArtifact.freeze(entry.getValue()))
                || !manifest.functions().containsKey(entry.getKey().getName())) continue;
            Symbol symbol = sourceIndex.get(source);
            functions.put(symbol, new Artifact(body, translated.scalarFrame(entry.getKey(), entry.getValue())));
            owners.put(entry.getKey(), symbol);
        }
        List<Fragment> fragments = new ArrayList<>();
        var model = translated.translatedModel();
        var printed = LuaPrinter.fragments(model);
        for (int i = 0; i < model.size(); i++) {
            LuaStatement statement = model.get(i);
            fragments.add(new Fragment(printed.get(i), statement instanceof LuaFunction function ? owners.get(function) : null,
                statement == translated.hotReloadAbiStatement()));
        }
        snapshot = new Snapshot(List.copyOf(fragments), Collections.unmodifiableMap(new LinkedHashMap<>(functions)), manifest,
            Collections.unmodifiableMap(sourceVersions), positions(units), Environment.of(args));
        reason = "captured " + functions.size() + " scalar functions";
    }

    public @Nullable Pending prepare(Collection<CompilationUnit> units,
                                    Supplier<Map<FuncDef, ImFunction>> sources, RunArgs args) {
        Snapshot previous = snapshot;
        if (previous == null) return null;
        sourceRevision++;
        if (!supports(args) || !previous.environment.equals(Environment.of(args)) || !previous.positions.equals(positions(units))) {
            clear();
            reason = "options or source positions changed";
            return null;
        }
        Map<Symbol, Artifact> replacements = new LinkedHashMap<>();
        Map<Symbol, Integer> sourceVersions = new LinkedHashMap<>();
        Map<FuncDef, ImFunction> sourceFunctions = sources.get();
        if (sourceFunctions.size() != sourceIndex.size()) {
            clear();
            reason = "source declaration membership changed";
            return null;
        }
        for (var entry : sourceFunctions.entrySet()) {
            FuncDef source = entry.getKey();
            Symbol key = sourceIndex.get(source);
            int version = entry.getValue().modificationCount();
            if (key != null) sourceVersions.put(key, version);
            if (key != null && Objects.equals(previous.sourceVersions.get(key), version)) continue;
            Artifact old = key == null ? null : previous.functions.get(key);
            ScalarFunctionArtifact body = ScalarFunctionArtifact.freeze(entry.getValue());
            if (old == null || body == null || !old.body.sameShape(body)) {
                clear();
                reason = "unsupported changed function " + source.getName();
                return null;
            }
            replacements.put(key, new Artifact(body, old.frame));
        }
        return new Pending(previous, replacements, sourceVersions, args);
    }

    private Result finish(Pending pending) {
        if (pending.revision != sourceRevision || pending.previous != snapshot) {
            throw new IllegalStateException("Stale scalar backend preparation");
        }
        Snapshot previous = pending.previous;
        Map<Symbol, Artifact> replacements = pending.replacements;
        try {
            Map<String, String> definitions = new LinkedHashMap<>();
            for (Artifact artifact : replacements.values()) {
                CompilationCancellation.check();
                definitions.put(artifact.frame.name(), artifact.body.emit(artifact.frame, pending.args));
            }
            LuaHotReload.Manifest manifest = LuaHotReload.replaceScalarDefinitions(previous.manifest, definitions);
            Map<Symbol, Artifact> functions = new LinkedHashMap<>(previous.functions);
            functions.putAll(replacements);
            List<Fragment> fragments = new ArrayList<>(previous.fragments.size());
            StringBuilder script = new StringBuilder();
            for (Fragment old : previous.fragments) {
                CompilationCancellation.check();
                String definition = old.abi ? LuaHotReload.abiDefinition(manifest.abi())
                    : old.owner == null ? null : definitions.get(previous.functions.get(old.owner).frame.name());
                LuaPrinter.PrintedStatement text = definition == null ? old.printed
                    : new LuaPrinter.PrintedStatement(old.printed.prefix(), definition, old.printed.suffix());
                script.append(text.render(text.text()));
                fragments.add(definition == null ? old : new Fragment(text, old.owner, old.abi));
            }
            snapshot = new Snapshot(List.copyOf(fragments), Collections.unmodifiableMap(new LinkedHashMap<>(functions)), manifest,
                pending.sourceVersions, previous.positions, previous.environment);
            reason = "incremental scalar functions: " + replacements.size();
            return new Result(script.toString(), manifest);
        } catch (RuntimeException | Error failure) {
            clear();
            throw failure;
        }
    }

    /** Conservative slice guard: no source relocation until source-map artifacts are implemented. */
    private static List<Position> positions(Collection<CompilationUnit> units) {
        List<Position> result = new ArrayList<>();
        for (CompilationUnit unit : units) positions(unit, result);
        return List.copyOf(result);
    }

    private static void positions(Element element, List<Position> result) {
        if (element instanceof AstElementWithSource source) addPosition(source.getSource(), result);
        if (element instanceof ExprClosure closure) addPosition(closure.getArrowSource(), result);
        if (element instanceof SomeSuperConstructorCall call) addPosition(call.getKeywordSource(), result);
        for (int i = 0; i < element.size(); i++) positions(element.get(i), result);
    }

    private static void addPosition(de.peeeq.wurstscript.parser.WPos p, List<Position> result) {
        result.add(new Position(p.getLeftPos(), p.getRightPos(), p.getLine(), p.getStartColumn(), p.getEndLine(), p.getEndColumn()));
    }
}
