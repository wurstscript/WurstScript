package de.peeeq.datastructures;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class GraphInterpreter<T> {

    protected abstract Collection<T> getIncidentNodes(T t);

    public TopsortResult<T> topSort(List<T> nodes) {
        Set<T> seen = Sets.newHashSet();
        List<T> seenStack = Lists.newArrayList();
        List<T> result = Lists.newArrayList();

        for (T t : nodes) {
            if (!seen.contains(t)) {
                @Nullable TopsortResult<T> r = topSort_add(result, seen, seenStack, t);
                if (r != null) {
                    return r;
                }
            }
        }
        return new TopsortResult<>(false, result);
    }


    private @Nullable TopsortResult<T> topSort_add(List<T> result, Set<T> seen, List<T> seenStack, T n) {
        for (int i = seenStack.size() - 1; i >= 0; i--) {
            if (seenStack.get(i) == n) {
                // cycle
                return new TopsortResult<>(true, Utils.subList(seenStack, i));
            }
        }
        if (!seen.contains(n)) {
            seenStack.add(n);
            for (T m : getIncidentNodes(n)) {
                TopsortResult<T> r = topSort_add(result, seen, seenStack, m);
                if (r != null) {
                    return r;
                }
            }
            seenStack.remove(seenStack.size() - 1);
            seen.add(n);
            result.add(n);
        }
        return null;
    }


    public static class TopsortResult<T> {
        private final boolean isCycle;
        private final List<T> result;

        public TopsortResult(boolean isCycle, List<T> result) {
            this.isCycle = isCycle;
            this.result = result;
        }

        public boolean isCycle() {
            return isCycle;
        }

        public List<T> getResult() {
            return result;
        }


    }

}
