package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ReferenceRewritingCopy;
import de.peeeq.wurstscript.translation.imtranslation.CallType;
import de.peeeq.wurstscript.translation.imtranslation.ImMetadataAdapter;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;

import java.util.*;

/** Frozen producer-selected targets. Only argument-preserving call replacements are replayed. */
record CallLoweringProducts(NodeGraphs.Snapshot graph, List<String> names, Map<Integer, Target> calls, int owner,
                            List<Type> types, List<Source> sources, Set<Integer> hashtables) {
    record Target(int helper, boolean tuplesEliminated, CallType callType) { }
    record Replacement(ImFunction function, boolean tuplesEliminated, CallType callType) { }
    /** A generated helper's source is relative to its producer's source function, or explicitly synthetic. */
    record Source(int function, List<Integer> path) {
        static Source freeze(de.peeeq.wurstscript.ast.Element source, ImBackendInput input) {
            if (source instanceof de.peeeq.wurstscript.ast.NoExpr && source.getParent() == null) return new Source(-1, List.of());
            List<Integer> path = new ArrayList<>();
            for (var node = source; node != null; node = node.getParent()) {
                Integer function = input.sourceFunctions().get(node);
                if (function != null) return new Source(function, List.copyOf(path));
                var parent = node.getParent();
                if (parent != null) {
                    for (int i = 0; i < parent.size(); i++) if (parent.get(i) == node) { path.add(i); break; }
                }
            }
            throw new ImBackendInput.Unsupported("helper source outside input functions");
        }
        de.peeeq.wurstscript.ast.Element materialize(ImBackendInput input) {
            if (function < 0) return de.peeeq.wurstscript.ast.Ast.NoExpr();
            var source = input.functions.get(function).getTrace();
            for (int i = path.size() - 1; i >= 0; i--) source = source.get(path.get(i));
            return source;
        }
    }
    /** Types introduced by lowering are structural values, independent of this request's input registry. */
    record Type(String kind, String name, Type entry) {
        static Type freeze(ImType type) {
            if (type instanceof ImSimpleType simple) return new Type("simple", simple.getTypename(), null);
            if (type instanceof ImVoid) return new Type("void", "", null);
            if (type instanceof ImArrayType array) return new Type("array", "", freeze(array.getEntryType()));
            throw new ImBackendInput.Unsupported("helper type " + type.getClass().getSimpleName());
        }
        ImType materialize() {
            return switch (kind) {
                case "simple" -> JassIm.ImSimpleType(name);
                case "void" -> JassIm.ImVoid();
                case "array" -> JassIm.ImArrayType(entry.materialize());
                default -> throw new IllegalStateException("Unknown helper type " + kind);
            };
        }
    }
    private static NodeGraphs.Adapter adapter(ImBackendInput input, List<Type> types, List<Source> sources) {
        return new ImMetadataAdapter() {
            @Override public NodeGraphs.Symbol symbol(String field, Object value) {
                if (value instanceof ImType type) {
                    Type frozen = Type.freeze(type);
                    int index = types.indexOf(frozen);
                    if (index < 0) { index = types.size(); types.add(frozen); }
                    return new NodeGraphs.Symbol("helper-type", Integer.toString(index));
                }
                if (value instanceof de.peeeq.wurstscript.ast.Element source) {
                    Source frozen = Source.freeze(source, input);
                    int index = sources.indexOf(frozen);
                    if (index < 0) { index = sources.size(); sources.add(frozen); }
                    return new NodeGraphs.Symbol("helper-source", Integer.toString(index));
                }
                return input.symbol(field, value);
            }
            @Override public Object resolve(NodeGraphs.Symbol symbol) {
                return switch (symbol.namespace()) {
                    case "helper-type" -> types.get(Integer.parseInt(symbol.key())).materialize();
                    case "helper-source" -> sources.get(Integer.parseInt(symbol.key())).materialize(input);
                    default -> input.resolve(symbol);
                };
            }
        };
    }

    static CallLoweringProducts capture(ImBackendInput input, int owner, Map<Integer, Replacement> replacements,
                                        LuaTranslator translated) {
        List<ImFunction> helpers = new ArrayList<>();
        Map<ImFunction, Integer> indexes = new IdentityHashMap<>();
        Map<Integer, Target> calls = new LinkedHashMap<>();
        List<Integer> sourceCalls = input.calls.getOrDefault(owner, List.of());
        for (int i = 0; i < sourceCalls.size(); i++) {
            Replacement replacement = replacements.get(sourceCalls.get(i));
            if (replacement == null) continue;
            int index = add(replacement.function, helpers, indexes);
            calls.put(i, new Target(index, replacement.tuplesEliminated, replacement.callType));
        }
        // Include generated dependencies as owned graphs, including recursive helper groups.
        for (int i = 0; i < helpers.size(); i++) {
            NodeGraphs.references(helpers.get(i), node -> {
                if (node instanceof ImFunction function && !input.ids.containsKey(function)) add(function, helpers, indexes);
            }, input);
        }
        List<String> names = helpers.stream().map(translated::functionBinding).toList();
        Set<Integer> hashtables = new LinkedHashSet<>();
        for (int i = 0; i < helpers.size(); i++) if (translated.hashtableStub(helpers.get(i))) hashtables.add(i);
        ImFunctions copies = JassIm.ImFunctions();
        Map<ImFunction, ImFunction> copied = new IdentityHashMap<>();
        for (int i = 0; i < helpers.size(); i++) {
            ImFunction original = helpers.get(i), copy = ReferenceRewritingCopy.copy(original);
            input.product(original, owner + ":" + i, names.get(i));
            copies.add(copy); copied.put(original, copy);
        }
        copies.accept(new Element.DefaultVisitor() {
            @Override public void visit(ImFunctionCall call) {
                super.visit(call);
                ImFunction target = copied.get(call.getFunc());
                if (target != null) call.setFunc(target);
            }
            @Override public void visit(ImFuncRef ref) {
                ImFunction target = copied.get(ref.getFunc());
                if (target != null) ref.setFunc(target);
            }
        });
        List<Type> types = new ArrayList<>();
        List<Source> sources = new ArrayList<>();
        NodeGraphs.Snapshot graph = NodeGraphs.freeze(copies, adapter(input, types, sources));
        return new CallLoweringProducts(graph, List.copyOf(names), Map.copyOf(calls), owner, List.copyOf(types), List.copyOf(sources), Set.copyOf(hashtables));
    }

    private static int add(ImFunction function, List<ImFunction> helpers, Map<ImFunction, Integer> indexes) {
        Integer old = indexes.get(function);
        if (old != null) return old;
        int index = helpers.size(); indexes.put(function, index); helpers.add(function); return index;
    }

    void lower(ImFunction function, ImBackendInput input, ImTranslator context) {
        if (calls.isEmpty()) return;
        ImFunctions helpers = (ImFunctions) graph.materialize(adapter(input, types, sources));
        for (int i = 0; i < helpers.size(); i++) input.product(helpers.get(i), owner + ":" + i, names.get(i));
        for (int index : hashtables) {
            ImFunction stub = helpers.get(index);
            context.luaHashtableStubs.put(stub.getName(), stub);
        }
        List<ImFunctionCall> source = new ArrayList<>();
        function.accept(new Element.DefaultVisitor() {
            @Override public void visit(ImFunctionCall call) { source.add(call); super.visit(call); }
        });
        calls.forEach((ordinal, target) -> {
            ImFunctionCall call = source.get(ordinal);
            call.setFunc(helpers.get(target.helper));
            call.setTuplesEliminated(target.tuplesEliminated);
            call.setCallType(target.callType);
        });
    }
}
