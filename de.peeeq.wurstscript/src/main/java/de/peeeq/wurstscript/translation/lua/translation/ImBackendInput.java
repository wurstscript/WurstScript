package de.peeeq.wurstscript.translation.lua.translation;

import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.*;
import de.peeeq.wurstscript.utils.CompilationCancellation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/** Request-local symbol table and semantic receipts for fresh compile-time output. No mutable input is published. */
final class ImBackendInput extends ImMetadataAdapter {
    static final class Unsupported extends RuntimeException {
        Unsupported(String message) { super(message); }
    }
    record Receipt(String declarations, Map<Integer, String> bodies, Map<Integer, String> shapes) { }
    private record Work(Element node, int owner) { }
    final List<Element> nodes = new ArrayList<>();
    final Map<Element, Integer> ids = new IdentityHashMap<>();
    final Map<Integer, ImFunction> functions = new LinkedHashMap<>();
    final Map<Integer, NodeGraphs.Snapshot> candidates = new LinkedHashMap<>();
    private final Map<NodeGraphs.Symbol, Object> symbols = new HashMap<>();
    private final Map<Integer, Element> externalDeclarations = new TreeMap<>();
    Map<Integer, Element> externalDeclarations() { return Collections.unmodifiableMap(externalDeclarations); }
    private final Map<ImType, String> types = new IdentityHashMap<>();
    private final Map<de.peeeq.wurstscript.ast.Element, String> origins = new IdentityHashMap<>();
    private final Map<Class<?>, Set<Integer>> sourceFields = new HashMap<>();
    final Receipt receipt;

    ImBackendInput(ImProg program, ImTranslator translator) {
        Map<String, Object> facts = new LinkedHashMap<>();
        facts.put("main", translator.getMainFunc()); facts.put("config", translator.getConfFunc());
        facts.put("global-init", translator.getGlobalInitFunc());
        facts.put("package-inits", translator.luaInitFunctions);
        facts.put("global-inits", translator.luaGlobalInitializers);
        facts.put("hooks", translator.hotReloadHooks());
        facts.put("compiletime-expressions", translator.compiletimeExpressionCounts());
        facts.put("compiletime-integer-references", translator.compiletimeIntegerReferences());
        facts.put("compiletime-reference-owners", translator.compiletimeReferenceOwners());
        Map<ImVar, String> compiletimeGlobals = new LinkedHashMap<>();
        for (ImVar global : translator.compiletimeGlobals()) compiletimeGlobals.put(global, translator.compiletimeGlobalIdentity(global));
        facts.put("compiletime-globals", compiletimeGlobals);
        facts.put("compiletime-root", translator.compiletimeStateFunction());
        facts.put("compiletime-parts", translator.compiletimeStateParts());
        facts.put("type-ids-before-generics", translator.typeIdsBeforeGenerics());
        List<Integer> owners = new ArrayList<>();
        registerOwned(program, owners);
        // Compile-time pruning can detach declarations still referenced by initializer metadata.
        // The schema supplies the closure; no compiler-maintained list of reference fields is needed.
        ArrayDeque<Element> roots = new ArrayDeque<>(); roots.add(program);
        Set<ImType> seenTypes = Collections.newSetFromMap(new IdentityHashMap<>());
        new NodeGraphs.MetadataMapper(node -> {
            if (!ids.containsKey(node)) { registerOwned(node, owners); roots.add(node); }
            return node;
        }, this).map(facts);
        while (!roots.isEmpty()) {
            CompilationCancellation.check();
            Element referenceRoot = roots.remove();
            NodeGraphs.referenceSlots(referenceRoot, (field, value) -> {
                if (!(value instanceof Element target)) return;
                // Types are structural values. Incidental sharing of primitive type objects must
                // neither allocate declaration identities nor invalidate an otherwise unchanged input.
                if (target instanceof ImType type) {
                    if (seenTypes.add(type)) roots.add(type);
                    return;
                }
                if (ids.containsKey(target)) return;
                registerOwned(target, owners);
                roots.add(target);
            }, this);
        }
        MessageDigest declarations = digest();
        token(declarations, NodeCodec.SCHEMA);
        token(declarations, metadata(facts));
        Map<Integer, MessageDigest> bodies = new LinkedHashMap<>(), shapes = new LinkedHashMap<>();
        functions.forEach((id, function) -> {
            bodies.put(id, digest());
            if (SymbolFunctionArtifact.supports(function)) shapes.put(id, digest());
        });
        for (int i = 0; i < nodes.size(); i++) {
            if ((i & 4095) == 0) CompilationCancellation.check();
            Element node = nodes.get(i); int owner = owners.get(i);
            MessageDigest exact = owner < 0 ? declarations : bodies.get(owner);
            encode(node, exact, false);
            MessageDigest shape = shapes.get(owner);
            if (shape != null) encode(node, shape, true);
            // Naming observes declaration origins even when semantic lowering does not.
            String binding = node instanceof ImFunction function ? LuaHotReload.functionId(function)
                : node instanceof ImClass clazz ? LuaHotReload.classId(clazz)
                : node instanceof ImVar variable && variable.isGlobal() ? LuaHotReload.variableId(translator, variable) : "";
            token(exact, binding); if (shape != null) token(shape, binding);
        }
        Map<Integer, String> exact = new LinkedHashMap<>(), shape = new LinkedHashMap<>();
        bodies.forEach((id, hash) -> exact.put(id, hex(hash)));
        shapes.forEach((id, hash) -> shape.put(id, hex(hash)));
        receipt = new Receipt(hex(declarations), Map.copyOf(exact), Map.copyOf(shape));
    }

    void freezeCandidates() {
        for (int id : receipt.shapes().keySet()) candidate(id);
    }

    NodeGraphs.Snapshot candidate(int id) {
        if (!receipt.shapes().containsKey(id)) return null;
        CompilationCancellation.check();
        return candidates.computeIfAbsent(id, key -> NodeGraphs.freeze(functions.get(key), this));
    }

    private void registerOwned(Element root, List<Integer> owners) {
        ArrayDeque<Work> pending = new ArrayDeque<>(); pending.push(new Work(root, -1));
        while (!pending.isEmpty()) {
            Work work = pending.pop(); Element node = work.node();
            if (ids.containsKey(node)) continue;
            int id = nodes.size(); ids.put(node, id); nodes.add(node);
            int owner = node instanceof ImFunction ? id : work.owner(); owners.add(owner);
            if (node instanceof ImFunction function) functions.put(id, function);
            for (int i = node.size() - 1; i >= 0; i--) pending.push(new Work(node.get(i), owner));
        }
    }


    private void encode(Element node, MessageDigest hash, boolean shape) {
        Set<Integer> source = sourceFields.computeIfAbsent(node.getClass(), ignored -> NodeCodec.sourceFields(NodeCodec.encode(node, TAGS).kind()));
        NodeCodec.Data<String> data = NodeCodec.encode(node, new NodeCodec.Encoder<>() {
            int index;
            public String child(Element value) { index++; return "owned:" + ids.get(value); }
            public String reference(Element value) { index++; return referenceValue(value); }
            public String attribute(String field, Object value) {
                if (source.contains(index++)) return "origin";
                if (shape && node instanceof ImConst && !(node instanceof ImFuncRef) && !(node instanceof ImNull)) return "literal";
                return metadata(value);
            }
        });
        token(hash, data.kind());
        for (String field : data.fields()) token(hash, field);
        token(hash, "items"); for (String item : data.items()) token(hash, item);
    }
    private static final NodeCodec.Encoder<String> TAGS = new NodeCodec.Encoder<>() {
        public String child(Element node) { return ""; }
        public String reference(Element node) { return ""; }
        public String attribute(String field, Object value) { return ""; }
    };
    private String referenceValue(Element value) {
        if (value == null) return "null";
        if (value instanceof ImType type) return typeKey(type);
        Integer id = ids.get(value);
        if (id == null) {
            if (!(value instanceof JassImElementWithName) && !(value instanceof ImTypeVar)) return "detached:" + structural(value);
            throw new Unsupported("reference outside compilation: " + value.getClass().getSimpleName());
        }
        return "ref:" + id;
    }
    private String typeKey(ImType type) {
        String old = types.get(type); if (old != null) return old;
        NodeCodec.Data<String> data = NodeCodec.encode(type, new NodeCodec.Encoder<>() {
            public String child(Element node) { return structural(node); }
            public String reference(Element node) { return referenceValue(node); }
            public String attribute(String field, Object value) { return metadata(value); }
        });
        String key = "type:" + packed(List.of(data.kind(), packed(data.fields()), packed(data.items()))); types.put(type, key);
        symbols.put(new NodeGraphs.Symbol("type", key), type);
        return key;
    }
    private String structural(Element node) {
        NodeCodec.Data<String> data = NodeCodec.encode(node, new NodeCodec.Encoder<>() {
            public String child(Element child) { return structural(child); }
            public String reference(Element target) { return referenceValue(target); }
            public String attribute(String field, Object value) { return metadata(value); }
        });
        return packed(List.of(data.kind(), packed(data.fields()), packed(data.items())));
    }
    private String metadata(Object value) {
        if (value == null) return "null";
        if (value instanceof Element node) return referenceValue(node);
        if (value instanceof String text) return "text:" + text.length() + ":" + text;
        if (value instanceof Number || value instanceof Boolean || value instanceof Character) return value.getClass().getName() + ":" + value;
        if (value instanceof Enum<?> enumeration) {
            String key = enumeration.getDeclaringClass().getName() + ":" + enumeration.name();
            symbols.put(new NodeGraphs.Symbol("enum", key), value); return key;
        }
        if (value instanceof FunctionFlagAnnotation annotation) return "annotation:" + annotation.getAnnotation();
        if (value instanceof FunctionFlagCompiletime flag) return "compiletime:" + flag.getOrderIndex();
        if (value instanceof de.peeeq.wurstscript.ast.Element source) return origin(source);
        if (value instanceof Map<?, ?> map) {
            List<String> entries = new ArrayList<>();
            map.forEach((key, item) -> entries.add(packed(List.of(metadata(key), metadata(item)))));
            if (mapKind(map) == NodeGraphs.MapKind.IDENTITY || map instanceof HashMap<?, ?> && !(map instanceof LinkedHashMap<?, ?>)) Collections.sort(entries);
            return "map:" + packed(entries);
        }
        if (value instanceof Collection<?> collection) {
            List<String> values = collection.stream().map(this::metadata).toList();
            if (value instanceof Set<?>) { values = new ArrayList<>(values); Collections.sort(values); }
            return (value instanceof Set<?> ? "set:" : "list:") + packed(values);
        }
        NodeGraphs.Parts parts = decompose(value);
        if (parts != null) return parts.tag() + ":" + metadata(parts.values());
        throw new Unsupported("metadata type " + value.getClass().getName());
    }
    private String origin(de.peeeq.wurstscript.ast.Element node) {
        return origins.computeIfAbsent(node, source -> {
            List<Integer> path = new ArrayList<>();
            for (var current = source; current.getParent() != null; current = current.getParent()) {
                var parent = current.getParent();
                for (int i = 0; i < parent.size(); i++) if (parent.get(i) == current) { path.add(i); break; }
            }
            return source.getClass().getSimpleName() + path;
        });
    }
    @Override public NodeGraphs.Symbol symbol(String field, Object value) {
        NodeGraphs.Symbol symbol;
        if (value instanceof ImType type) symbol = new NodeGraphs.Symbol("type", typeKey(type));
        else if (value instanceof Element node) {
            symbol = new NodeGraphs.Symbol("node", referenceValue(node));
            Integer id = ids.get(node);
            if (id != null) externalDeclarations.put(id, node);
        }
        else if (value instanceof de.peeeq.wurstscript.ast.Element source) symbol = new NodeGraphs.Symbol("source", origin(source));
        else if (value instanceof Enum<?>) symbol = new NodeGraphs.Symbol("enum", metadata(value));
        else if (value instanceof FunctionFlag) symbol = new NodeGraphs.Symbol("flag", metadata(value));
        else throw new Unsupported("snapshot metadata " + field + ": " + value.getClass().getName());
        symbols.put(symbol, value); return symbol;
    }
    @Override public Object resolve(NodeGraphs.Symbol symbol) {
        Object value = symbols.get(symbol);
        if (value == null && symbol.namespace().equals("node")) value = nodes.get(Integer.parseInt(symbol.key().substring(4)));
        if (value == null) throw new Unsupported("unresolved symbol " + symbol);
        // Type references of this slice have only primitive/array structure and are owned by scratch work.
        return value instanceof ImType type ? privateType(type) : value;
    }
    private static ImType privateType(ImType type) {
        if (type instanceof ImSimpleType simple) return JassIm.ImSimpleType(simple.getTypename());
        if (type instanceof ImVoid) return JassIm.ImVoid();
        if (type instanceof ImArrayType array) return JassIm.ImArrayType(privateType(array.getEntryType()));
        throw new Unsupported("body type " + type.getClass().getSimpleName());
    }
    String globalReceipt(Set<Integer> admitted) {
        MessageDigest hash = digest(); token(hash, receipt.declarations());
        receipt.bodies().keySet().stream().sorted().forEach(id -> {
            token(hash, Integer.toString(id));
            token(hash, admitted.contains(id) ? receipt.shapes().get(id) : receipt.bodies().get(id));
        });
        return hex(hash);
    }
    private static MessageDigest digest() {
        try { return MessageDigest.getInstance("SHA-256"); }
        catch (NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
    }
    private static String hex(MessageDigest hash) { return HexFormat.of().formatHex(hash.digest()); }
    private static String packed(List<String> values) {
        StringBuilder text = new StringBuilder();
        for (String value : values) text.append(value.length()).append(':').append(value);
        return text.toString();
    }
    private static void token(MessageDigest hash, String value) {
        if (value == null) value = "missing";
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        for (int shift = 24; shift >= 0; shift -= 8) hash.update((byte) (bytes.length >>> shift));
        hash.update(bytes);
    }
}
