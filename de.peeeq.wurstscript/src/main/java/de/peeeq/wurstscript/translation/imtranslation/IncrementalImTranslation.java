package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.translation.lua.translation.ScalarLuaBackend;
import de.peeeq.wurstscript.translation.lua.translation.LuaTranslator;
import org.eclipse.jdt.annotation.Nullable;
import java.util.*;

/** Session-owned frontend input. General builds fork private IR; supported scalar edits use frozen backend artifacts. */
public final class IncrementalImTranslation {
    private @Nullable ImTranslator frontend;
    private List<CompilationUnit> modelUnits = List.of();
    private List<CompilationUnit> selectedUnits = List.of();
    private final Map<FunctionLike, WStatements> bodies = new IdentityHashMap<>();
    private int reused, translated, frontendBuilds;
    private String rebuildReason = "first compilation";
    private final ScalarLuaBackend scalarBackend = new ScalarLuaBackend();
    private long revision;
    public String scalarBackendReason() { return scalarBackend.reason(); }

    /** Opaque preparation token: the retained frontend is never handed to a mutable backend consumer. */
    public static final class Prepared {
        private final ImTranslator frontend;
        private final long revision;
        private Prepared(ImTranslator frontend, long revision) {
            this.frontend = frontend;
            this.revision = revision;
        }
    }

    public record Statistics(int reusedFunctions, int translatedFunctions, int cachedFunctions,
                             int frontendBuilds, String rebuildReason) { }

    public Statistics statistics() {
        return new Statistics(reused, translated, bodies.size(), frontendBuilds, rebuildReason);
    }

    public void clear() {
        scalarBackend.clear();
        revision++;
        frontend = null;
        modelUnits = selectedUnits = List.of();
        bodies.clear();
        reused = translated = frontendBuilds = 0;
        rebuildReason = "model cleared";
    }

    public ImTranslator translate(WurstModel model, Collection<CompilationUnit> units, boolean unitTest, RunArgs args) {
        return fork(prepare(model, units, unitTest, args), args);
    }

    public Prepared prepare(WurstModel model, Collection<CompilationUnit> units, boolean unitTest, RunArgs args) {
        List<CompilationUnit> selected = List.copyOf(units);
        try {
            String reason = frontend == null ? "first compilation"
                : !modelUnits.equals(model) || !selectedUnits.equals(selected) ? "compilation units changed"
                : frontend.isUnitTestMode() != unitTest || frontend.isLuaTarget() != args.isLua()
                    || frontend.getRunArgs().isLuaHotReload() != args.isLuaHotReload() ? "compiler environment changed"
                : !frontend.hasCurrentPreservation() ? "runtime name preservation changed" : null;
            Map<FunctionLike, WStatements> current = sourceBodies(units);
            List<FuncDef> changed = new ArrayList<>();
            if (reason == null) {
                for (Map.Entry<FunctionLike, WStatements> entry : current.entrySet()) {
                    if (bodies.get(entry.getKey()) == entry.getValue()) continue;
                    if (!(entry.getKey() instanceof FuncDef function) || function.attrNearestClassDef() != null
                        && function.attrNearestClassDef().getParent().attrNearestClassDef() != null) {
                        reason = "initializer, constructor, extension or nested-class body changed";
                        break;
                    }
                    changed.add(function);
                }
            }
            if (reason == null && !Objects.requireNonNull(frontend).updateFunctionBodies(changed)) {
                reason = "function dependencies or generated declarations changed";
            }
            if (reason != null) {
                scalarBackend.clear();
                frontend = new ImTranslator(model, unitTest, args, units);
                frontend.translateProg();
                frontendBuilds++;
                translated = current.size();
                reused = 0;
                rebuildReason = reason;
                modelUnits = List.copyOf(model);
                selectedUnits = selected;
            } else {
                translated = changed.size();
                reused = current.size() - translated;
                rebuildReason = "none";
            }
            bodies.clear();
            bodies.putAll(current);
            scalarBackend.sourcePrepared();
            return new Prepared(Objects.requireNonNull(frontend), ++revision);
        } catch (RuntimeException | Error failure) {
            clear();
            throw failure;
        }
    }

    private void requireCurrent(Prepared prepared) {
        if (prepared.frontend != frontend || prepared.revision != revision) throw new IllegalStateException("Stale frontend preparation");
    }

    public ImTranslator fork(Prepared prepared, RunArgs args) {
        requireCurrent(prepared);
        try {
            return prepared.frontend.forkForLowering(args);
        } catch (RuntimeException | Error failure) {
            clear();
            throw failure;
        }
    }

    public ScalarLuaBackend.@Nullable Pending tryScalarBackend(Prepared prepared, RunArgs args) {
        requireCurrent(prepared);
        return scalarBackend.prepare(selectedUnits, prepared.frontend::sourceFunctions, args);
    }

    public void rememberLuaOutput(Prepared prepared, LuaTranslator translated, RunArgs args) {
        requireCurrent(prepared);
        scalarBackend.remember(selectedUnits, prepared.frontend::sourceFunctions, translated, args);
    }

    private static Map<FunctionLike, WStatements> sourceBodies(Collection<CompilationUnit> units) {
        Map<FunctionLike, WStatements> result = new LinkedHashMap<>();
        for (CompilationUnit unit : units) collectBodies(unit, result);
        return result;
    }

    private static void collectBodies(Element element, Map<FunctionLike, WStatements> result) {
        if (element instanceof FunctionLike function && element instanceof AstElementWithBody holder) {
            result.put(function, holder.getBody());
            return;
        }
        for (int i = 0; i < element.size(); i++) collectBodies(element.get(i), result);
    }
}
