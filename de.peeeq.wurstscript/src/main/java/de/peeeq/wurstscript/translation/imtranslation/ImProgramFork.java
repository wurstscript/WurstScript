package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.jassIm.*;
import io.vavr.control.Either;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;

/** Copies an entire compilation graph, including referenced types and detached compiler-owned declarations. */
final class ImProgramFork {
    private final Map<Element, Element> copies = new IdentityHashMap<>();
    private final ArrayDeque<Element> pending = new ArrayDeque<>();
    private boolean draining;

    @SuppressWarnings("unchecked")
    <T extends Element> @Nullable T copy(@Nullable T original) {
        if (original == null) return null;
        Element result = copies.get(original);
        if (result == null) {
            result = original.copy();
            register(original, result);
        }
        if (!draining) {
            draining = true;
            try {
                while (!pending.isEmpty()) {
                    Element old = pending.removeFirst();
                    rewrite(old, copies.get(old));
                }
            } finally {
                draining = false;
            }
        }
        return (T) result;
    }

    private void register(Element original, Element result) {
        de.peeeq.wurstscript.translation.BackendWork.copied(original);
        if (copies.put(original, result) != null) throw new IllegalStateException("Owned IM node is present twice");
        pending.addLast(original);
        for (int i = 0; i < original.size(); i++) register(original.get(i), result.get(i));
    }

    private <T extends Element> List<T> list(Collection<T> originals) {
        List<T> result = new ArrayList<>(originals.size());
        for (T original : originals) result.add(copy(original));
        return result;
    }

    private void rewrite(Element original, Element result) {
        if (original instanceof ImProg old && result instanceof ImProg fresh) {
            Map<ImVar, List<ImSet>> inits = new LinkedHashMap<>();
            old.getGlobalInits().forEach((variable, assignments) -> inits.put(copy(variable), list(assignments)));
            fresh.setGlobalInits(inits);
            fresh.setPreservedVariables(new LinkedHashSet<>(old.getPreservedVariables()));
        } else if (original instanceof ImFunction old && result instanceof ImFunction fresh) {
            fresh.setReturnType(copy(old.getReturnType()));
            fresh.setFlags(new ArrayList<>(old.getFlags()));
        } else if (original instanceof ImTypeClassFunc old && result instanceof ImTypeClassFunc fresh) {
            fresh.setReturnType(copy(old.getReturnType()));
        } else if (original instanceof ImVar old && result instanceof ImVar fresh) {
            fresh.setType(copy(old.getType()));
        } else if (original instanceof ImClass old && result instanceof ImClass fresh) {
            fresh.setSuperClasses(list(old.getSuperClasses()));
        } else if (original instanceof ImMethod old && result instanceof ImMethod fresh) {
            fresh.setMethodClass(copy(old.getMethodClass()));
            fresh.setImplementation(copy(old.getImplementation()));
            fresh.setSubMethods(list(old.getSubMethods()));
            fresh.setLuaMethodDispatchAliases(new ArrayList<>(old.getLuaMethodDispatchAliases()));
        } else if (original instanceof ImFuncRefOrCall old && result instanceof ImFuncRefOrCall fresh) {
            fresh.setFunc(copy(old.getFunc()));
        } else if (original instanceof ImMethodCall old && result instanceof ImMethodCall fresh) {
            fresh.setMethod(copy(old.getMethod()));
        } else if (original instanceof ElementWithVar old && result instanceof ElementWithVar fresh) {
            fresh.setVar(copy(old.getVar()));
        } else if (original instanceof ImVarargLoopVar old && result instanceof ImVarargLoopVar fresh) {
            fresh.setVar(copy(old.getVar()));
        } else if (original instanceof ImClassRelatedExprWithClass old && result instanceof ImClassRelatedExprWithClass fresh) {
            fresh.setClazz(copy(old.getClazz()));
        } else if (original instanceof ImCast old && result instanceof ImCast fresh) {
            fresh.setFromType(copy(old.getFromType()));
            fresh.setToType(copy(old.getToType()));
        } else if (original instanceof ImNull old && result instanceof ImNull fresh) {
            fresh.setType(copy(old.getType()));
        } else if (original instanceof ImTypeVarDispatch old && result instanceof ImTypeVarDispatch fresh) {
            fresh.setTypeClassFunc(copy(old.getTypeClassFunc()));
            fresh.setTypeVariable(copy(old.getTypeVariable()));
        } else if (original instanceof ImClassType old && result instanceof ImClassType fresh) {
            fresh.setClassDef(copy(old.getClassDef()));
        } else if (original instanceof ImTypeVarRef old && result instanceof ImTypeVarRef fresh) {
            fresh.setTypeVariable(copy(old.getTypeVariable()));
        } else if (original instanceof ImTupleType old && result instanceof ImTupleType fresh) {
            fresh.setTypes(list(old.getTypes()));
            fresh.setNames(new ArrayList<>(old.getNames()));
        } else if (original instanceof ImArrayType old && result instanceof ImArrayType fresh) {
            fresh.setEntryType(copy(old.getEntryType()));
        } else if (original instanceof ImArrayTypeMulti old && result instanceof ImArrayTypeMulti fresh) {
            fresh.setEntryType(copy(old.getEntryType()));
            fresh.setArraySize(new ArrayList<>(old.getArraySize()));
        } else if (original instanceof ImTypeArgument old && result instanceof ImTypeArgument fresh) {
            fresh.setType(copy(old.getType()));
            Map<ImTypeClassFunc, Either<ImMethod, ImFunction>> bindings = new LinkedHashMap<>();
            old.getTypeClassBinding().forEach((requirement, implementation) -> bindings.put(copy(requirement),
                implementation.isLeft() ? Either.left(copy(implementation.getLeft())) : Either.right(copy(implementation.get()))));
            fresh.setTypeClassBinding(bindings);
        }
    }

    <K, V> void map(Map<K, V> original, Map<K, V> result) {
        original.forEach((key, value) -> result.put(metadata(key), metadata(value)));
    }

    /** Metadata containers have source-node keys and IM references; source nodes belong to the managed model. */
    @SuppressWarnings("unchecked")
    <T> T metadata(T value) {
        if (value instanceof Element element) return (T) copy(element);
        if (value instanceof Map<?, ?> map) {
            Map<Object, Object> result = new LinkedHashMap<>();
            map.forEach((key, item) -> result.put(metadata(key), metadata(item)));
            return (T) result;
        }
        if (value instanceof List<?> list) {
            List<Object> result = new ArrayList<>(list.size());
            list.forEach(item -> result.add(metadata(item)));
            return (T) result;
        }
        if (value instanceof Set<?> set) {
            Set<Object> result = new LinkedHashSet<>();
            set.forEach(item -> result.add(metadata(item)));
            return (T) result;
        }
        return value;
    }
}
