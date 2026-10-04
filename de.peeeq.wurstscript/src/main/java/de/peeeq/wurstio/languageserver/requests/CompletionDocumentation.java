package de.peeeq.wurstio.languageserver.requests;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.peeeq.wurstio.languageserver.JassDocService;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstscript.ast.AstElementWithSource;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.ConstructorDef;
import de.peeeq.wurstscript.ast.FunctionDefinition;
import de.peeeq.wurstscript.ast.NameDef;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.MarkupContent;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Lazily resolves documentation on the language worker without retaining old model snapshots. */
public final class CompletionDocumentation {
    private static final int MAX_TARGETS = 1000;
    private static final String DATA_KEY = "wurstCompletion";
    private final String session = UUID.randomUUID().toString();
    private final Map<String, Target> targets = new LinkedHashMap<>();
    private long nextId;

    public void attach(CompletionItem item, AstElementWithSource target) {
        CompilationUnit cu = target.attrCompilationUnit();
        if (cu == null) {
            return;
        }
        String id = session + ":" + ++nextId;
        targets.put(id, new Target(new WeakReference<>(target), new WeakReference<>(cu), item.getLabel()));
        if (targets.size() > MAX_TARGETS) {
            targets.remove(targets.keySet().iterator().next());
        }
        JsonObject data = new JsonObject();
        data.addProperty(DATA_KEY, id);
        item.setData(data);
    }

    public final class Resolve extends UserRequest<CompletionItem> {
        private final CompletionItem item;

        public Resolve(CompletionItem item) {
            this.item = item;
        }

        @Override
        public boolean keepDuplicateRequests() {
            return true;
        }

        @Override
        public CompletionItem execute(ModelManager modelManager) {
            String id = targetId(item.getData());
            Target saved = targets.get(id);
            if (saved == null || !Objects.equals(saved.label(), item.getLabel())) {
                return item;
            }
            AstElementWithSource target = saved.element().get();
            CompilationUnit cu = saved.compilationUnit().get();
            // Edits/removals replace CUs; module invalidation can also detach individual declarations.
            // An old list must never resolve against a different declaration at the same offset.
            if (target == null || cu == null || target.attrCompilationUnit() != cu
                    || modelManager.getCompilationUnit(WFile.create(cu.getCuInfo().getFile())) != cu) {
                targets.remove(id);
                return item;
            }
            enrich(item, target);
            return item;
        }
    }

    private static String targetId(Object data) {
        if (data instanceof JsonObject object) {
            JsonElement id = object.get(DATA_KEY);
            if (id != null && id.isJsonPrimitive() && id.getAsJsonPrimitive().isString()) {
                return id.getAsString();
            }
        } else if (data instanceof Map<?, ?> map && map.get(DATA_KEY) instanceof String id) {
            return id;
        }
        return null;
    }

    static void enrich(CompletionItem item, AstElementWithSource target) {
        String comment = "";
        boolean jassDoc = false;
        if (target instanceof FunctionDefinition function) {
            comment = function.attrComment();
            if (comment == null || comment.isEmpty()) {
                comment = JassDocService.getInstance().documentationForFunctionQuick(function);
                jassDoc = comment != null && !comment.isEmpty();
            }
        } else if (target instanceof NameDef name) {
            comment = name.attrComment();
            if (comment == null || comment.isEmpty()) {
                comment = JassDocService.getInstance().documentationForVariableQuick(name);
                jassDoc = comment != null && !comment.isEmpty();
            }
        } else if (target instanceof ConstructorDef constructor) {
            comment = constructor.attrComment();
        }
        if (comment != null && !comment.isEmpty()) {
            if (jassDoc) {
                item.setDocumentation(new MarkupContent("markdown", "*JassDoc*\n\n" + comment));
            } else {
                item.setDocumentation(comment);
            }
        }
    }

    private record Target(WeakReference<AstElementWithSource> element,
                          WeakReference<CompilationUnit> compilationUnit, String label) {
    }
}
