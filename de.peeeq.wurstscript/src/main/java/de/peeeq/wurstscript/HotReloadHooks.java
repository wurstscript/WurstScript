package de.peeeq.wurstscript;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.FuncDef;

/**
 * Functions annotated {@code @onHotReload}: in a {@code -luaHotReload} build each payload calls them, with their bodies
 * as the payload has them, once it installed everything else, so a map can run its setup code again
 * (docs/LUA_HOT_RELOAD.md, "Reload hooks"). In any other build the annotation does nothing.
 */
public final class HotReloadHooks {
    public static final String ANNOTATION = "onHotReload";

    private HotReloadHooks() {
    }

    public static boolean isHook(Element trace) {
        return trace instanceof FuncDef f && f.attrHasAnnotation(ANNOTATION);
    }
}
