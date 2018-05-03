package de.peeeq.wurstscript.attributes;

import com.google.common.collect.ImmutableCollection;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeUnknown;
import de.peeeq.wurstscript.utils.Utils;

import java.util.Optional;

public class AttrForEachStatement {

    public static Optional<NameLink> calcIterator(StmtForIn forEach) {
        Expr iterationTarget = forEach.getIn();
        WurstType itrType = iterationTarget.attrTyp();
        // find 'iterator' function:
        ImmutableCollection<NameLink> iterator = iterationTarget.lookupMemberFuncs(itrType, "iterator", false);
        // find the 'iterator' function without parameters:
        // must exist, because this is after type check
        Optional<NameLink> iteratorFunc = iterator.stream().filter(nl -> nl.getParameterTypes().isEmpty()).findFirst();
        // find the 'hasNext' function without parameters
        if (!iteratorFunc.isPresent()) {
            forEach.getIn().addError("For loop target <" + Utils.printElement(iterationTarget) + "> doesn't provide a iterator() function");
        }
        return iteratorFunc;
    }

    public static Optional<NameLink> calcHasNext(StmtForEach forEach) {
        WurstType iteratorType = calcItrType(forEach);
        // find 'hasNext' function:
        ImmutableCollection<NameLink> hasNext = forEach.getIn().lookupMemberFuncs(iteratorType, "hasNext", false);
        // find the 'hasNext' function without parameters
        Optional<NameLink> nextFunc = hasNext.stream().filter(nl -> nl.getParameterTypes().isEmpty()).findFirst();
        if (!nextFunc.isPresent()) {
            forEach.getIn().addError("For loop iterator doesn't provide a hasNext() function that returns boolean");
        }
        return nextFunc;
    }

    public static Optional<NameLink> calcGetNext(StmtForEach forEach) {
        WurstType iteratorType = calcItrType(forEach);

        // find 'next' function:
        ImmutableCollection<NameLink> next = forEach.getIn().lookupMemberFuncs(iteratorType, "next", false);
        // find the 'next' function without parameters
        Optional<NameLink> nextFunc = next.stream().filter(nl -> nl.getParameterTypes().isEmpty()).findFirst();
        if (!nextFunc.isPresent()) {
            forEach.getIn().addError("Target of for-loop <" + forEach.getIn().attrTyp().getName() + " doesn't provide a proper next() function");
        }
        return nextFunc;
    }

    public static WurstType calcItrType(StmtForEach forEach) {
        try {
            WurstType iteratorType = WurstTypeUnknown.instance();
            if (forEach instanceof StmtForFrom) {
                iteratorType = forEach.getIn().attrTyp();
            } else if (forEach instanceof StmtForIn) {
                Optional<NameLink> nameLink = calcIterator((StmtForIn) forEach);
                if (nameLink.isPresent()) {
                    NameLink iteratorFunc = nameLink.get();
                    Expr iterationTarget = forEach.getIn();

                    iteratorType = iteratorFunc.getReturnType().setTypeArgs(iterationTarget.attrTyp().getTypeArgBinding()).normalize();
                }
            }
            return iteratorType;
        } catch (CyclicDependencyError error) {
            forEach.addError("Iteration input and target type may not depend on each other.");
        }
        return WurstTypeUnknown.instance();
    }
}
