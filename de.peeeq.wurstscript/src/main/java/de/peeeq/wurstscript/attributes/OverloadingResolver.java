package de.peeeq.wurstscript.attributes;

import com.google.common.collect.Ordering;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.types.WurstTypeTypeParam;
import de.peeeq.wurstscript.types.WurstTypeVararg;
import de.peeeq.wurstscript.utils.NotNullList;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public abstract class OverloadingResolver<F extends Element, C> {

    abstract int getParameterCount(F f);

    abstract WurstType getParameterType(F f, int i);

    abstract int getArgumentCount(C c);

    abstract WurstType getArgumentType(C c, int i);

    abstract void handleError(List<String> hints);

    boolean isVararg(F f) {
        int paramCount = getParameterCount(f);
        return paramCount > 0 && getParameterType(f, paramCount - 1) instanceof WurstTypeVararg;
    }

    int getMinParameterCount(F f) {
        return isVararg(f) ? getParameterCount(f) - 1 : getParameterCount(f);
    }

    boolean hasValidParameterCount(F f, C caller) {
        int argCount = getArgumentCount(caller);
        return argCount >= getMinParameterCount(f) && (isVararg(f) || argCount <= getParameterCount(f));
    }

    WurstType getParameterTypeForArg(F f, int i) {
        if (isVararg(f) && i >= getParameterCount(f) - 1) {
            return ((WurstTypeVararg) getParameterType(f, getParameterCount(f) - 1)).getBaseType();
        }
        return getParameterType(f, i);
    }

    Optional<F> resolve(Iterable<F> alternativeFunctions, C caller) {
        int size = Utils.size(alternativeFunctions);
        if (size == 0) {
            return Optional.empty();
        }
        if (size == 1) {
            return Optional.of(Utils.getFirst(alternativeFunctions));
        }
        List<String> hints = new NotNullList<>();

        Map<F, Integer> numMatches = new HashMap<>();
        for (F f : alternativeFunctions) {
            if (!hasValidParameterCount(f, caller)) {
                numMatches.put(f, -1);
                continue;
            }
            int matches = 0;
            for (int i = 0; i < getArgumentCount(caller); i++) {
                WurstType expectedParamType = getParameterTypeForArg(f, i);
                if (getArgumentType(caller, i) instanceof WurstTypeTypeParam
                        && expectedParamType instanceof WurstTypeTypeParam) {
                    // should be ok!
                } else if (!getArgumentType(caller, i).isSubtypeOf(expectedParamType, f)) {
                    hints.add("Expected " + expectedParamType
                            + " as parameter " + i + " ,but found " + getArgumentType(caller, i) + ".");
                    continue;
                }
                matches++;
            }
            numMatches.put(f, matches);
        }
        // sort by number of matches
        List<F> funcs = Ordering.natural().reverse()
                .onResultOf(numMatches::get)
                .sortedCopy(alternativeFunctions);

        int maxMatches = numMatches.get(funcs.get(0));
        funcs.removeIf(f -> numMatches.get(f) < maxMatches);

        if (funcs.size() == 1) {
            // exactly one match
            return Optional.of(funcs.get(0));
        }

        // if we have several functions matching a prefix,
        // we have to check if there is a function with the right number of parameters

        List<F> rightNumberOfParams = new ArrayList<>();
        for (F f1 : funcs) {
            if (hasValidParameterCount(f1, caller)) {
                rightNumberOfParams.add(f1);
            }
        }
        if (rightNumberOfParams.size() == 1) {
            return Optional.of(rightNumberOfParams.get(0));
        } else if (rightNumberOfParams.size() > 1) {
            // if there are many funcs with the right number of arguments
            // take thos as the basis for showing errors
            funcs = rightNumberOfParams;
        }
        // there are many alternatives
        String alts = funcs.stream()
                .map((F f) -> {
                    if (f instanceof FunctionDefinition) {
                        FunctionDefinition func = (FunctionDefinition) f;
                        return "function " + func.getName() + " defined in " +
                                "  line " + func.getSource().getLine();
                    }
                    return Utils.printElementWithSource(Optional.of(f));
                }).collect(Collectors.joining("\n * "));
        handleError(Utils.list("call is ambiguous, there are several alternatives: \n * " + alts));

        // return one with least number of args
        return Optional.of(
                Ordering.natural()
                        .onResultOf(this::getParameterCount)
                        .max(funcs));
    }


    public static @Nullable ConstructorDef resolveExprNew(List<ConstructorDef> constructors, final ExprNewObject node) {
        return new OverloadingResolver<ConstructorDef, ExprNewObject>() {

            @Override
            int getParameterCount(ConstructorDef f) {
                return f.getParameters().size();
            }

            @Override
            WurstType getParameterType(ConstructorDef f, int i) {
                return f.getParameters().get(i).attrTyp();
            }

            @Override
            int getArgumentCount(ExprNewObject c) {
                return c.getArgs().size();
            }

            @Override
            WurstType getArgumentType(ExprNewObject c, int i) {
                return c.getArgs().get(i).attrTyp();
            }

            @Override
            void handleError(List<String> hints) {
                node.addError("No suitable constructor found. \n" + Utils.join(hints, ", \n"));
            }
        }.resolve(constructors, node).orElse(null);
    }

    public static @Nullable ConstructorDef resolveSuperCall(List<ConstructorDef> constructors, final ConstructorDef node) {
        return new OverloadingResolver<ConstructorDef, ConstructorDef>() {

            @Override
            int getParameterCount(ConstructorDef f) {
                return f.getParameters().size();
            }

            @Override
            WurstType getParameterType(ConstructorDef f, int i) {
                return f.getParameters().get(i).attrTyp();
            }

            @Override
            int getArgumentCount(ConstructorDef c) {
                return c.getSuperConstructorCall().match(new SuperConstructorCall.Matcher<Integer>() {
                    @Override
                    public Integer case_NoSuperConstructorCall(NoSuperConstructorCall c) {
                        return 0;
                    }

                    @Override
                    public Integer case_SomeSuperConstructorCall(SomeSuperConstructorCall c) {
                        return c.getSuperArgs().size();
                    }
                });
            }

            @Override
            WurstType getArgumentType(ConstructorDef c, int i) {
                SomeSuperConstructorCall sc = (SomeSuperConstructorCall) c.getSuperConstructorCall();
                return sc.getSuperArgs().get(i).attrTyp();
            }

            @Override
            void handleError(List<String> hints) {
                node.addError("No suitable constructor found. \n" + Utils.join(hints, ", \n"));
            }
        }.resolve(constructors, node).orElse(null);
    }

    public static @Nullable ConstructorDef resolveThisCall(List<ConstructorDef> constructors, final FunctionCall node) {
        return new OverloadingResolver<ConstructorDef, FunctionCall>() {

            @Override
            int getParameterCount(ConstructorDef f) {
                return f.getParameters().size();
            }

            @Override
            WurstType getParameterType(ConstructorDef f, int i) {
                return f.getParameters().get(i).attrTyp();
            }

            @Override
            int getArgumentCount(FunctionCall c) {
                return c.getArgs().size();
            }

            @Override
            WurstType getArgumentType(FunctionCall c, int i) {
                return c.getArgs().get(i).attrTyp();
            }

            @Override
            void handleError(List<String> hints) {
                node.addError("No suitable constructor found. \n" + Utils.join(hints, ", \n"));
            }
        }.resolve(constructors, node).orElse(null);
    }


}
