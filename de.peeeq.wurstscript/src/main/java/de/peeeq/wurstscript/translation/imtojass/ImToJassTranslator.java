package de.peeeq.wurstscript.translation.imtojass;

import com.google.common.collect.*;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.jassAst.*;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassFunctions;
import de.peeeq.wurstscript.jassAst.JassNative;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassAst.JassSimpleVar;
import de.peeeq.wurstscript.jassAst.JassVars;
import de.peeeq.wurstscript.jassIm.Element;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.translation.imoptimizer.RestrictedCompressedNames;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;
import de.peeeq.wurstscript.utils.Utils;
import org.eclipse.jdt.annotation.Nullable;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static de.peeeq.wurstscript.jassAst.JassAst.*;

public class ImToJassTranslator {

    private ImProg imProg;
    private Multimap<ImFunction, ImFunction> calledFunctions;
    private ImFunction mainFunc;
    private ImFunction confFunction;
    private @Nullable JassProg prog;
    private Stack<ImFunction> translatingFunctions = new Stack<>();
    private Set<ImFunction> translatedFunctions = Sets.newLinkedHashSet();
    private Set<String> usedNames = Sets.newLinkedHashSet();
    private Multimap<ImFunction, String> usedLocalNames = HashMultimap.create();

    public ImToJassTranslator(ImProg imProg, Multimap<ImFunction, ImFunction> calledFunctions,
                              ImFunction mainFunc, ImFunction confFunction) {
        this.imProg = imProg;
        this.calledFunctions = calledFunctions;
        this.mainFunc = mainFunc;
        this.confFunction = confFunction;
    }

    public JassProg translate() {
        makeNamesUnique(imProg.getGlobals());
        makeNamesUnique(imProg.getFunctions());

        JassVars globals = JassVars();
        JassFunctions functions = JassFunctions();
        prog = JassProg(JassTypeDefs(), globals, JassNatives(), functions);

        collectGlobalVars();

        translateFunctionTransitive(mainFunc);
        translateFunctionTransitive(confFunction);

        return prog;
    }

    /**
     * makes names unique in a consistent way
     */
    private <T extends JassImElementWithName> void makeNamesUnique(List<T> list) {
        List<T> sorted = list.stream()
                .sorted(
                        Comparator.comparing(JassImElementWithName::getName)
                                .thenComparing(v -> v.getTrace().attrSource().getFile())
                                .thenComparing(v -> v.getTrace().attrSource().getLine())
                                .thenComparing(v -> v.getTrace().attrSource().getStartColumn()))
                .collect(Collectors.toList());

        for (int i = 0; i < sorted.size(); i++) {
            T vi = sorted.get(i);
            for (int j = i + 1; j < sorted.size(); j++) {
                T vj = sorted.get(j);
                if (vi.getName().equals(vj.getName())) {
                    vj.setName(vi.getName() + "_" + j);
                }
            }
        }
    }

    private void collectGlobalVars() {
        for (ImVar v : imProg.getGlobals()) {
            globalImVars.add(v);
            getJassVarFor(v);
        }
    }

    private void translateFunctionTransitive(ImFunction imFunc) {
        if (translatedFunctions.contains(imFunc)) {
            // already translated
            return;
        }
        if (translatingFunctions.contains(imFunc)) {
            // TODO extract method
            if (imFunc != translatingFunctions.peek()) {
                StringBuilder msg = new StringBuilder("cyclic dependency between functions: ");
                boolean start = false;
                for (ImFunction f : translatingFunctions) {
                    if (imFunc == f) {
                        start = true;
                    }
                    if (start) {
                        msg.append("\n - ").append(Utils.printElement(getTrace(f))).append("  ( ").append(f.attrTrace().attrSource().getFile()).append(" line" +
                                "  ").append(f.attrTrace().attrSource().getLine()).append(")");
                    }
                }
                WPos src = getTrace(imFunc).attrSource();
                throw new CompileError(src, msg.toString());
            }
            // already translating, recursive function
            return;
        }
        translatingFunctions.push(imFunc);
        for (ImFunction f : sorted(calledFunctions.get(imFunc))) {
            translateFunctionTransitive(f);
        }

        translateFunction(imFunc);

        // translation finished
        if (translatingFunctions.pop() != imFunc) {
            throw new Error("something went wrong...");
        }
        translatedFunctions.add(imFunc);
    }

    private List<ImFunction> sorted(Collection<ImFunction> collection) {
        List<ImFunction> r = Lists.newArrayList(collection);
        r.sort(Comparator.comparing(ImFunction::getName));
        return r;
    }

    private static de.peeeq.wurstscript.ast.Element getTrace(@Nullable Element elem) {
        while (elem != null) {
            if (elem instanceof ElementWithTrace) {
                ElementWithTrace ElementWithTrace = (ElementWithTrace) elem;
                de.peeeq.wurstscript.ast.Element t = ElementWithTrace.getTrace();
                if (t != null) {
                    return t;
                }
            }
            elem = elem.getParent();
        }
        throw new Error("Could not get trace to original program.");
    }

    private void translateFunction(ImFunction imFunc) {
        if (imFunc.isBj() || imFunc.hasFlag(FunctionFlagEnum.IS_VARARG)) {
            return;
        }
        // not a native
        JassFunctionOrNative f = getJassFuncFor(imFunc);

        f.setReturnType(imFunc.getReturnType().translateType());

        // translate parameters
        for (ImVar v : imFunc.getParameters()) {
            f.getParams().add((JassSimpleVar) getJassVarFor(v));
        }
        if (f instanceof JassFunction) {
            JassFunction jf = (JassFunction) f;
            // translate locals
            for (ImVar v : imFunc.getLocals()) {
                jf.getLocals().add(getJassVarFor(v));
            }
            imFunc.getBody().translate(jf.getBody(), jf, this);
        }

    }


    private String getUniqueGlobalName(String name) { // TODO find local names
        name = jassifyName(name);
        name = Utils.makeUniqueName(name, n -> !usedNames.contains(n));
        usedNames.add(name);
        return name;
    }

    private String getUniqueLocalName(ImFunction imFunction, String name) {
        name = jassifyName(name);
        name = Utils.makeUniqueName(name, n -> !usedNames.contains(n) && !usedLocalNames.containsEntry(imFunction, n));
        usedLocalNames.put(imFunction, name);
        return name;
    }


    private Map<ImVar, JassVar> jassVars = Maps.newLinkedHashMap();
    private Set<ImVar> globalImVars = Sets.newLinkedHashSet();

    JassVar getJassVarFor(ImVar v) {
        JassVar result = jassVars.get(v);
        if (result == null) {
            boolean isArray = v.getType() instanceof ImArrayType;
            String type = v.getType().translateType();
            String name = v.getName();
            if (v.getNearestFunc() != null) {
                name = getUniqueLocalName(v.getNearestFunc(), name);
            } else {
                name = getUniqueGlobalName(name);
            }
            if (isArray) {
                result = JassAst.JassArrayVar(type, name);
            } else {
                if (isGlobal(v) && v.getType() instanceof ImSimpleType) {
                    JassExpr initialVal = ImHelper.defaultValueForType((ImSimpleType) v.getType()).translate(this);
                    result = JassAst.JassInitializedVar(type, name, initialVal, v.getIsBJ());
                } else {
                    result = JassAst.JassSimpleVar(type, name);
                }
            }
            if (isGlobal(v) && (!v.getIsBJ() || result instanceof JassInitializedVar)) {
                prog.getGlobals().add(result);
            }
            jassVars.put(v, result);
        }
        return result;
    }

    private String jassifyName(String name) {
        name = filterInvalidSymbols(name);
        if (RestrictedCompressedNames.contains(name) || name.startsWith("_")) {
            name = "w" + name;
        }
        if (name.endsWith("_")) {
            name = name + "u";
        }
        if (name.isEmpty()) {
            name = "empty";
        }
        return name;
    }

    private final Pattern jassValidName = Pattern.compile("[a-zA-Z][a-zA-Z0-9_]*");

    /**
     * replaces all invalid characters with underscores
     */
    private String filterInvalidSymbols(String name) {
        if (jassValidName.matcher(name).matches()) {
            return name;
        }
        StringBuilder sb = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!(c >= 'a' && c <= 'z'
                    || c >= 'A' && c <= 'Z'
                    || c >= '0' && c <= '9'
                    || c == '_')) {
                c = '_';
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private boolean isGlobal(ImVar v) {
        return globalImVars.contains(v);
    }


    private Map<ImFunction, JassFunctionOrNative> jassFuncs = Maps.newLinkedHashMap();

    public JassFunctionOrNative getJassFuncFor(ImFunction func) {
        JassFunctionOrNative f = jassFuncs.get(func);
        if (f == null) {
            if (func.isNative()) {
                f = JassAst.JassNative(func.getName(), JassSimpleVars(), "nothing");
                if (!func.isBj() && !func.isExtern()) {
                    prog.getNatives().add((JassNative) f);
                }
            } else {
                String name = func.getName();
                // find a unique name, but keep special names 'main' and 'config'
                if (!name.equals("main") && !name.equals("config")) {
                    name = getUniqueGlobalName(func.getName());
                }
                boolean isCompiletimeNative = func.hasFlag(FunctionFlagEnum.IS_COMPILETIME_NATIVE);
                f = JassFunction(name, JassSimpleVars(), "nothing", JassVars(), JassStatements(), isCompiletimeNative);
                if (!func.isBj() && !func.isExtern()) {
                    prog.getFunctions().add((JassFunction) f);
                }
            }
            jassFuncs.put(func, f);
        }
        return f;
    }


}
