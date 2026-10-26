package de.peeeq.wurstscript.translation.imtranslation;

import de.peeeq.wurstscript.jassIm.NodeGraphs;
import io.vavr.control.Either;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Type semantics for IM metadata; generated codecs own every field and reference slot. */
public class ImMetadataAdapter implements NodeGraphs.Adapter {
    @Override public boolean externalReference(Object value) {
        return value instanceof de.peeeq.wurstscript.ast.Element
            || value instanceof de.peeeq.wurstscript.jassAst.Element
            || value instanceof de.peeeq.wurstscript.luaAst.Element;
    }

    @Override public NodeGraphs.MapKind mapKind(Map<?, ?> value) {
        // fastutil exposes insertion-ordered maps through SortedMap without sorting their keys.
        return value instanceof Object2ObjectLinkedOpenHashMap<?, ?>
            ? NodeGraphs.MapKind.EQUALITY : NodeGraphs.Adapter.super.mapKind(value);
    }

    @Override public NodeGraphs.SetKind setKind(Set<?> value) {
        return value instanceof ObjectLinkedOpenHashSet<?>
            ? NodeGraphs.SetKind.EQUALITY : NodeGraphs.Adapter.super.setKind(value);
    }

    @Override public NodeGraphs.Parts decompose(Object value) {
        if (value instanceof Either<?, ?> either) {
            return new NodeGraphs.Parts(either.isLeft() ? "either-left" : "either-right",
                Collections.singletonList(either.isLeft() ? either.getLeft() : either.get()));
        }
        return null;
    }

    @Override public Object compose(String tag, List<Object> values) {
        return switch (tag) {
            case "either-left" -> Either.left(values.getFirst());
            case "either-right" -> Either.right(values.getFirst());
            default -> NodeGraphs.Adapter.super.compose(tag, values);
        };
    }
}
