package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.ParamTypes.ParamInfo;
import de.peeeq.wurstscript.types.WurstType;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class ArgTypes {
    private final List<Arg> args;

    public ArgTypes(List<Arg> args) {
        this.args = args;
    }

    public Arg get(int i) {
        return args.get(i);
    }


    public static class Arg {
        final @Nullable Identifier name;
        final Expr expr;

        public Arg(Expr expr) {
            this.name = null;
            this.expr = expr;
        }

        public Arg(Argument arg) {
            if (arg.getArgName() instanceof Identifier) {
                this.name = (Identifier) arg.getArgName();
            } else {
                this.name = null;
            }
            this.expr = arg.getExpr();
        }

        public Expr getExpr() {
            return expr;
        }

        public Identifier getName() {
            return name;
        }
    }

    static class ArgTypeCompatibilityRes {
        private ArgTypeCompatibility compatibility = ArgTypeCompatibility.FULL_COMPATIBILITY;
        private List<CompileError> errors = new ArrayList<>();

        public void compatibilityInfo(ArgTypeCompatibility compatibility) {
            if (compatibility.compareTo(this.compatibility) < 0) {
                this.compatibility = compatibility;
            }
        }
    }

    private <T extends Comparable<T>> T min(T a, T b) {
        return a.compareTo(b) < 0 ? a : b;
    }


    public static ArgTypes fromArguments(Arguments args) {
        return new ArgTypes(
                args.stream()
                        .map(Arg::new)
                        .collect(Collectors.toList())
        );
    }

    public static ArgTypes fromExprs(List<Expr> argTypes) {
        return new ArgTypes(
                argTypes.stream()
                        .map(Arg::new)
                        .collect(Collectors.toList())
        );
    }

    public static ArgTypes fromExprs(Expr... argTypes) {
        return fromExprs(Arrays.asList(argTypes));
    }

    public int argCount() {
        return args.size();
    }

    public ArgTypeCompatibility compatibilityWith(ParamTypes parameterTypes) {
        if (argCount() < parameterTypes.getMinRequiredArgs()
                || argCount() > parameterTypes.getMaxArgs()) {
            // shortcut for wrong number of args:
            return ArgTypeCompatibility.WRONG_NUMBER_OF_ARGS;
        }
        return checkCall(parameterTypes, null, null);
    }

    public LinkedHashMap<ParamInfo, Arg> matchWith(ParamTypes parameterTypes) {
        LinkedHashMap<ParamInfo, Arg> result = new LinkedHashMap<>();
        boolean outOfOrder = false;
        for (int i = 0; i < args.size(); i++) {
            Arg arg = args.get(i);
            if (arg.name != null) {
                Optional<ParamInfo> opi = parameterTypes.getParam(arg.name.getName());
                if (opi.isPresent()) {
                    if (i >= parameterTypes.getParams().size() || opi.get() != parameterTypes.getParams().get(i)) {
                        outOfOrder = true;
                    }
                    result.put(opi.get(), arg);
                }
            } else { // no name given
                if (outOfOrder) {
                    continue;
                }
                if (i < parameterTypes.getParams().size()) {
                    ParamInfo pi = parameterTypes.getParams().get(i);
                    result.put(pi, arg);
                }
            }
        }
        return result;
    }


    public ArgTypeCompatibility checkCall(ParamTypes parameterTypes, String funcName, @Nullable Element errorPos) {
        ArgTypeCompatibility res = ArgTypeCompatibility.FULL_COMPATIBILITY;
        List<WurstType> paramTypes = parameterTypes.getTypeList();
        Arg outOfOrderArg = null;
        Set<String> assignedParameters = new HashSet<>();
        for (int i = 0; i < args.size(); i++) {
            Arg arg = args.get(i);
            String argName = arg.name == null ? null : arg.name.getName();
            if (outOfOrderArg != null) {
                if (argName == null) {
                    if (errorPos != null) {
                        arg.expr.addError("Missing argument name for parameter " + i + " when calling " + funcName + ". " +
                                "Because argument " + outOfOrderArg.name + " appears out of order, all following arguments must be given with their name.");
                    }
                    res = min(res, ArgTypeCompatibility.WRONG_ARG_NAME);
                    continue;
                }
            } else { // still handling parameters in order
                if (i >= paramTypes.size()) {
                    if (errorPos != null) {
                        arg.expr.addError("Too many arguments when calling " + funcName + ".");
                    }
                    res = min(res, ArgTypeCompatibility.WRONG_NUMBER_OF_ARGS);
                    break;
                }
                String paramName = parameterTypes.getParamName(i);
                if (argName == null) {
                    argName = paramName;
                } else if (!argName.equals(paramName)) {
                    outOfOrderArg = arg;
                }
            }

            // just look up by name:
            Optional<WurstType> pt = parameterTypes.getParamType(argName);
            if (pt.isPresent()) {
                if (!arg.expr.attrTyp().isSubtypeOf(pt.get(), arg.expr)) {
                    if (errorPos != null) {
                        arg.expr.addError("Expected exression of type " + pt.get()
                                + " for parameter " + argName + " " + funcName + ", but found " + arg.expr.attrTyp() + ".");
                    }
                    res = min(res, ArgTypeCompatibility.WRONG_TYPE);
                }
            } else {
                if (errorPos != null) {
                    assert arg.name != null;
                    arg.name.addError("Could not find parameter with name " + argName + " in function " + funcName + " .");
                }
                res = min(res, ArgTypeCompatibility.WRONG_ARG_NAME);
            }
            boolean nameIsNew = assignedParameters.add(argName);
            if (!nameIsNew) {
                if (errorPos != null) {
                    arg.expr.addError("Parameter with name " + argName + " is already given.");
                }
            }
        }

        // check if there are any arguments missing:
        List<ParamInfo> missingParams = parameterTypes.getParams().stream()
                .filter(p -> !assignedParameters.contains(p.getName()))
                .collect(Collectors.toList());

        if (!missingParams.isEmpty()) {
            if (errorPos != null) {
                errorPos.addError("Call to " + funcName + " is missing arguments: " + Utils.join(missingParams, ", "));
            }
            res = min(res, ArgTypeCompatibility.WRONG_NUMBER_OF_ARGS);
        }
        return res;
    }


    public enum ArgTypeCompatibility implements Comparable<ArgTypeCompatibility> {
        WRONG_NUMBER_OF_ARGS,
        WRONG_ARG_NAME,
        WRONG_TYPE,
        FULL_COMPATIBILITY,
    }


}
