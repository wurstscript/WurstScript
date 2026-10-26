package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.luaAst.LuaFunction;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.translation.lua.printing.LuaPrinter;
import de.peeeq.wurstscript.utils.CompilationCancellation;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;

/** Reuses runtime artifacts after fresh compile-time execution. Global preparation is validated explicitly. */
public final class SymbolLuaBackend {
    private record Artifact(SymbolFunctionArtifact body, LuaTranslator.FunctionFrame frame) { }
    private record Fragment(LuaPrinter.PrintedStatement printed, @Nullable Integer owner, boolean abi) { }
    private record Snapshot(String environment, String global, ImBackendInput.Receipt receipt, Map<Integer, Artifact> functions,
                            Map<Integer, String> functionNames, Map<Integer, String> variableNames,
                            Set<String> declarationNames, List<Fragment> fragments, LuaHotReload.Manifest manifest) { }
    public record Result(String script, LuaHotReload.Manifest manifest) { }
    private @Nullable Snapshot snapshot;
    private @Nullable ImBackendInput pending;
    private long generation;
    private String reason = "no completed symbol backend output";
    public String reason() { return reason; }
    public void clear() { snapshot = null; pending = null; generation++; reason = "model cleared"; }
    public void sourcePrepared() { pending = null; generation++; }
    private void requireGeneration(long expected) {
        if (generation != expected) throw new java.util.concurrent.CancellationException("Stale symbol backend preparation");
    }

    private static boolean supports(RunArgs args) {
        return args.isLua() && args.isLuaHotReload() && !args.isRunTests() && !args.isIncludeStacktraces()
            && !args.isInline() && !args.isOptimize() && !args.isLocalOptimizations()
            && !args.isExportW3o() && !args.isExportObjects() && !args.isExtractImports();
    }
    private static String environment(RunArgs args) {
        return args.arguments() + ":" + args.isLegacyJassTypeChecks() + ":" + args.getFunctionSplitLimit();
    }

    /** Called only after this request's compile-time evaluation and object generation have completed. */
    public @Nullable Result prepare(ImProg program, ImTranslator translator, RunArgs args) {
        pending = null;
        long revision = generation;
        if (!supports(args)) { snapshot = null; reason = "unsupported symbol backend options"; return null; }
        try {
            return prepare(new ImBackendInput(program, translator), args, revision);
        } catch (ImBackendInput.Unsupported unsupported) {
            snapshot = null; pending = null; reason = unsupported.getMessage(); return null;
        }
    }

    private @Nullable Result prepare(ImBackendInput input, RunArgs args, long revision) {
        requireGeneration(revision);
        pending = input;
        Snapshot previous = snapshot;
        if (previous == null || !previous.environment.equals(environment(args))
            || !previous.global.equals(input.globalReceipt(previous.functions.keySet()))) {
            reason = previous == null ? "first symbol backend compilation"
                : !previous.receipt.declarations().equals(input.receipt.declarations()) ? "symbol declarations changed"
                : "symbol function products changed: " + input.functions.entrySet().stream().filter(entry -> {
                    int id = entry.getKey();
                    return !Objects.equals(previous.functions.containsKey(id) ? previous.receipt.shapes().get(id) : previous.receipt.bodies().get(id),
                        previous.functions.containsKey(id) ? input.receipt.shapes().get(id) : input.receipt.bodies().get(id));
                }).limit(3).map(entry -> entry.getValue().getName()).toList();
            snapshot = null;
            input.freezeCandidates();
            return null;
        }
        Map<Integer, Artifact> replacements = new LinkedHashMap<>();
        for (var entry : previous.functions.entrySet()) {
            int id = entry.getKey(); Artifact old = entry.getValue();
            String version = input.receipt.bodies().get(id);
            if (Objects.equals(old.body.version(), version)) continue;
            NodeGraphs.Snapshot body = input.candidate(id);
            if (body == null || !Objects.equals(old.body.shape(), input.receipt.shapes().get(id))) {
                snapshot = null; reason = "function shape changed"; return null;
            }
            replacements.put(id, new Artifact(new SymbolFunctionArtifact(body, version, old.body.shape()), old.frame));
        }
        try {
            Map<String, String> definitions = new LinkedHashMap<>();
            for (Artifact artifact : replacements.values()) {
                CompilationCancellation.check();
                definitions.put(artifact.frame.name(), emit(artifact, input, previous.functionNames, previous.variableNames, previous.declarationNames, args));
            }
            LuaHotReload.Manifest manifest = LuaHotReload.replaceScalarDefinitions(previous.manifest, definitions);
            List<Fragment> fragments = new ArrayList<>(); StringBuilder script = new StringBuilder();
            for (Fragment old : previous.fragments) {
                CompilationCancellation.check();
                String replacement = old.abi ? LuaHotReload.abiDefinition(manifest.abi())
                    : old.owner == null ? null : definitions.get(previous.functions.get(old.owner).frame.name());
                LuaPrinter.PrintedStatement printed = replacement == null ? old.printed
                    : new LuaPrinter.PrintedStatement(old.printed.prefix(), replacement, old.printed.suffix());
                fragments.add(new Fragment(printed, old.owner, old.abi)); script.append(printed.render(printed.text()));
            }
            Map<Integer, Artifact> functions = new LinkedHashMap<>(previous.functions); functions.putAll(replacements);
            CompilationCancellation.check();
            requireGeneration(revision);
            snapshot = new Snapshot(previous.environment, previous.global, input.receipt, Map.copyOf(functions), previous.functionNames,
                previous.variableNames, previous.declarationNames, List.copyOf(fragments), manifest);
            pending = null; reason = "incremental symbol functions: " + replacements.size();
            return new Result(script.toString(), manifest);
        } catch (RuntimeException | Error failure) {
            clear(); throw failure;
        }
    }

    /** The cold compiler is an admission oracle for phases which this body slice has not migrated. */
    public void remember(LuaTranslator translated, RunArgs args) {
        ImBackendInput input = pending; pending = null;
        long revision = generation;
        if (input == null) return;
        LuaHotReload.Manifest manifest = translated.hotReloadManifest(); if (manifest == null) return;
        Set<String> declarationNames = translated.occupiedDeclarationNames();
        Map<Integer, String> functionNames = translated.functionBindings(input), variableNames = translated.variableBindings(input);
        Map<Integer, Artifact> artifacts = new LinkedHashMap<>();
        int unported = 0;
        Map<LuaFunction, Integer> owners = new IdentityHashMap<>();
        for (var entry : translated.translatedFunctions().entrySet()) {
            CompilationCancellation.check();
            Integer id = input.ids.get(entry.getValue());
            if (id == null || !input.candidates.containsKey(id) || !manifest.functions().containsKey(entry.getKey().getName())
                || manifest.functions().get(entry.getKey().getName()).kind() != LuaHotReload.Kind.PROGRAM) continue;
            try {
                SymbolFunctionArtifact body = new SymbolFunctionArtifact(input.candidates.get(id), input.receipt.bodies().get(id), input.receipt.shapes().get(id));
                SymbolFunctionArtifact.Lowered lowered = body.lower(input, args);
                if (!NodeGraphs.freeze(lowered.function(), input).sameSemantics(NodeGraphs.freeze(entry.getValue(), input))) continue;
                Artifact artifact = new Artifact(body, translated.scalarFrame(entry.getKey(), entry.getValue()));
                if (!emit(artifact, input, functionNames, variableNames, declarationNames, args).equals(manifest.functions().get(entry.getKey().getName()).definition())) continue;
                artifacts.put(id, artifact); owners.put(entry.getKey(), id);
            } catch (ImBackendInput.Unsupported unsupported) {
                // A cold producer introduced a symbol/type outside this slice. Keep its batch fragment.
                unported++;
            }
        }
        List<Fragment> fragments = new ArrayList<>(); var model = translated.translatedModel(); var printed = LuaPrinter.fragments(model);
        for (int i = 0; i < model.size(); i++) {
            var statement = model.get(i);
            fragments.add(new Fragment(printed.get(i), statement instanceof LuaFunction function ? owners.get(function) : null,
                statement == translated.hotReloadAbiStatement()));
        }
        CompilationCancellation.check();
        requireGeneration(revision);
        snapshot = new Snapshot(environment(args), input.globalReceipt(artifacts.keySet()), input.receipt, Map.copyOf(artifacts),
            functionNames, variableNames, declarationNames, List.copyOf(fragments), manifest);
        reason += "; captured " + artifacts.size() + " symbol functions; unported products: " + unported;
    }

    private static String emit(Artifact artifact, ImBackendInput input, Map<Integer, String> functionNames,
                               Map<Integer, String> variableNames, Set<String> declarationNames, RunArgs args) {
        SymbolFunctionArtifact.Lowered lowered = artifact.body.lower(input, args);
        Map<ImFunction, String> functions = new IdentityHashMap<>();
        Map<ImVar, String> variables = new IdentityHashMap<>();
        NodeGraphs.referenceSlots(lowered.function(), (field, node) -> {
            Integer id = node instanceof Element element ? input.ids.get(element) : null;
            if (id == null) return;
            if (node instanceof ImFunction function) {
                String name = functionNames.get(id);
                if (name == null) throw new ImBackendInput.Unsupported("callee binding outside body slice");
                functions.put(function, name);
            } else if (node instanceof ImVar variable) {
                String name = variableNames.get(id);
                if (name == null) throw new ImBackendInput.Unsupported("storage binding outside body slice");
                variables.put(variable, name);
            }
        }, input);
        try (var scope = NodeOwnership.CHECKS ? NodeOwnership.scope(lowered.function()) : null) {
            return LuaTranslator.emitOwnedFunction(lowered.function(), artifact.frame, lowered.context(), functions, variables, declarationNames);
        }
    }
}
