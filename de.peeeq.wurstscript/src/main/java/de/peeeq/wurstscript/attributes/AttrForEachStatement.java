package de.peeeq.wurstscript.attributes;

import com.google.common.collect.ImmutableCollection;
import de.peeeq.wurstscript.ast.Expr;
import de.peeeq.wurstscript.ast.StmtForEach;
import de.peeeq.wurstscript.ast.StmtForFrom;
import de.peeeq.wurstscript.ast.StmtForIn;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeUnknown;

import java.util.Optional;

public class AttrForEachStatement {

    public static Optional<FuncLink> calcIterator(StmtForIn forEach) {
        Expr iterationTarget = forEach.getIn();
        WurstType itrType = iterationTarget.attrTyp();
        // find 'iterator' function:
        ImmutableCollection<FuncLink> iterator = iterationTarget.lookupMemberFuncs(itrType, "iterator", false);
        // find the 'iterator' function without parameters:
        // must exist, because this is after type check
        Optional<FuncLink> iteratorFunc = iterator.stream().filter(nl -> nl.getParameterTypes().isEmpty()).findFirst();
        // find the 'hasNext' function without parameters
        if (!iteratorFunc.isPresent()) {
            forEach.getIn().addError("For loop target " + itrType + " doesn't provide a iterator() function");
        } else {
            FuncLink f = iteratorFunc.get();
            if (f.isStatic() && !itrType.isStaticRef()) {
                iterationTarget.addError("Cannot use static iterator method from " + itrType + " on dynamic value.");
            } else if (!f.isStatic() && itrType.isStaticRef()) {
                iterationTarget.addError("Cannot use dynamic iterator method from " + itrType + " without a dynamic value.");
            }
        }
        return iteratorFunc;
    }

    public static Optional<FuncLink> calcHasNext(StmtForEach forEach) {
        WurstType iteratorType = calcItrType(forEach);
        // find 'hasNext' function:
        ImmutableCollection<FuncLink> hasNext = forEach.getIn().lookupMemberFuncs(iteratorType, "hasNext", false);
        // find the 'hasNext' function without parameters
        Optional<FuncLink> nextFunc = hasNext.stream().filter(nl -> nl.getParameterTypes().isEmpty()).findFirst();
        if (!nextFunc.isPresent()) {
            forEach.getIn().addError("For loop iterator doesn't provide a hasNext() function that returns boolean");
        } else {
            FuncLink f = nextFunc.get();
            if (f.isStatic()) {
                forEach.addError("Cannot use a foreach loop here, because the 'hasNext' method " + iteratorType + " is static.");
            }
        }
        return nextFunc;
    }

    public static Optional<FuncLink> calcGetNext(StmtForEach forEach) {
        WurstType iteratorType = calcItrType(forEach);

        // find 'next' function:
        ImmutableCollection<FuncLink> next = forEach.getIn().lookupMemberFuncs(iteratorType, "next", false);
        // find the 'next' function without parameters
        Optional<FuncLink> nextFunc = next.stream().filter(nl -> nl.getParameterTypes().isEmpty()).findFirst();
        if (!nextFunc.isPresent()) {
            forEach.getIn().addError("Target of for-loop '" + forEach.getIn().attrTyp().getName() + "' doesn't provide a proper next() function");
        } else {
            FuncLink f = nextFunc.get();
            if (f.isStatic()) {
                forEach.addError("Cannot use a foreach loop here, because the 'next' method " + iteratorType + " is static.");
            }
        }
        return nextFunc;
    }

    public static Optional<FuncLink> calcClose(StmtForEach forEach) {
        if (forEach instanceof StmtForFrom) {
            return Optional.empty();
        }
        WurstType iteratorType = calcItrType(forEach);

        // find 'close' function:
        ImmutableCollection<FuncLink> close = forEach.getIn().lookupMemberFuncs(iteratorType, "close", false);
        // find the 'close' function without parameters
        Optional<FuncLink> closeFunc = close.stream().filter(nl -> nl.getParameterTypes().isEmpty()).findFirst();
        if (!closeFunc.isPresent()) {
            forEach.getIn().addError("Target of for-loop <" + forEach.getIn().attrTyp().getName() + " doesn't provide a proper close() function");
        } else {
            FuncLink f = closeFunc.get();
            if (f.isStatic()) {
                forEach.addError("Cannot use a foreach loop here, because the 'close' method " + iteratorType + " is static.");
            }
        }
        return closeFunc;
    }

    public static WurstType calcItrType(StmtForEach forEach) {
        WurstType iteratorType = WurstTypeUnknown.instance();
        if (forEach instanceof StmtForFrom) {
            iteratorType = forEach.getIn().attrTyp();
        } else if (forEach instanceof StmtForIn) {
            Optional<FuncLink> nameLink = calcIterator((StmtForIn) forEach);
            if (nameLink.isPresent()) {
                FuncLink iteratorFunc = nameLink.get();
                iteratorType = iteratorFunc.getReturnType().normalize();
            }
        }
        return iteratorType;
    }
}
