package de.peeeq.wurstscript.translation.imtranslation;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.google.common.collect.ImmutableList.Builder;
import de.peeeq.datastructures.ImmutableTree;
import de.peeeq.datastructures.Partitions;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.*;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.names.FuncLink;
import de.peeeq.wurstscript.attributes.names.NameLink;
import de.peeeq.wurstscript.attributes.names.PackageLink;
import de.peeeq.wurstscript.jassIm.Element;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.jassIm.ImArrayType;
import de.peeeq.wurstscript.jassIm.ImClass;
import de.peeeq.wurstscript.jassIm.ImExprs;
import de.peeeq.wurstscript.jassIm.ImFuncRef;
import de.peeeq.wurstscript.jassIm.ImFunction;
import de.peeeq.wurstscript.jassIm.ImFunctionCall;
import de.peeeq.wurstscript.jassIm.ImMethod;
import de.peeeq.wurstscript.jassIm.ImProg;
import de.peeeq.wurstscript.jassIm.ImReturn;
import de.peeeq.wurstscript.jassIm.ImSimpleType;
import de.peeeq.wurstscript.jassIm.ImStatementExpr;
import de.peeeq.wurstscript.jassIm.ImStmts;
import de.peeeq.wurstscript.jassIm.ImTupleArrayType;
import de.peeeq.wurstscript.jassIm.ImTupleExpr;
import de.peeeq.wurstscript.jassIm.ImTupleSelection;
import de.peeeq.wurstscript.jassIm.ImTupleType;
import de.peeeq.wurstscript.jassIm.ImVar;
import de.peeeq.wurstscript.jassIm.ImVars;
import de.peeeq.wurstscript.jassIm.ImVoid;
import de.peeeq.wurstscript.parser.WPos;
import de.peeeq.wurstscript.types.*;
import de.peeeq.wurstscript.utils.Pair;
import de.peeeq.wurstscript.utils.Utils;
import de.peeeq.wurstscript.validation.WurstValidator;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static de.peeeq.wurstscript.jassIm.JassIm.*;
import static de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum.*;
import static de.peeeq.wurstscript.utils.Utils.elementNameWithPath;

public class ImTranslator {


    public static final String $DEBUG_PRINT = "$debugPrint";

    private static final de.peeeq.wurstscript.ast.Element emptyTrace = Ast.NoExpr();

    private @Nullable Multimap<ImFunction, ImFunction> callRelations = null;
    private @Nullable Set<ImVar> usedVariables = null;
    private @Nullable Set<ImVar> readVariables = null;
    private @Nullable Set<ImFunction> usedFunctions = null;

    private @Nullable ImFunction debugPrintFunction;

    private final Map<TranslatedToImFunction, ImFunction> functionMap = new LinkedHashMap<>();
    private @Nullable ImFunction globalInitFunc;

    private final ImProg imProg;

    final Map<WPackage, ImFunction> initFuncMap = new LinkedHashMap<>();

    private final Map<TranslatedToImFunction, ImVar> thisVarMap = new LinkedHashMap<>();

    private final Set<WPackage> translatedPackages = new LinkedHashSet<>();
    private final Set<ClassDef> translatedClasses = new LinkedHashSet<>();


    private final Map<VarDef, ImVar> varMap = new LinkedHashMap<>();

    private final WurstModel wurstProg;

    private @Nullable ImFunction mainFunc = null;

    private @Nullable ImFunction configFunc = null;

    private final Map<ImVar, ImmutableTree<ImVar>> varsForTupleVar = new LinkedHashMap<>();

    private boolean isUnitTestMode;

    private ImVar lastInitFunc = JassIm.ImVar(emptyTrace, WurstTypeString.instance().imTranslateType(), "lastInitFunc", false);

    private int compiletimeOrderCounter = 1;
    private final Map<TranslatedToImFunction, FunctionFlagCompiletime> compiletimeFlags = new HashMap<>();
    private final Map<ExprFunctionCall, Integer> compiletimeExpressionsOrder = new HashMap<>();

    de.peeeq.wurstscript.ast.Element lasttranslatedThing;

    public ImTranslator(WurstModel wurstProg, boolean isUnitTestMode) {
        this.wurstProg = wurstProg;
        this.lasttranslatedThing = wurstProg;
        this.isUnitTestMode = isUnitTestMode;
        imProg = ImProg(ImVars(), ImFunctions(), JassIm.ImClasses(), new LinkedHashMap<>());
    }


    /**
     * translates a program
     */
    public ImProg translateProg() {
        try {
            globalInitFunc = ImFunction(emptyTrace, "initGlobals", ImVars(), ImVoid(), ImVars(), ImStmts(), flags());
            addFunction(getGlobalInitFunc());
            debugPrintFunction = ImFunction(emptyTrace, $DEBUG_PRINT, ImVars(JassIm.ImVar(wurstProg, WurstTypeString.instance().imTranslateType(), "msg",
                    false)), ImVoid(), ImVars(), ImStmts(), flags(IS_NATIVE, IS_BJ));

            calculateCompiletimeOrder();

            for (CompilationUnit cu : wurstProg) {
                translateCompilationUnit(cu);
            }

            if (mainFunc == null) {
                mainFunc = ImFunction(emptyTrace, "main", ImVars(), ImVoid(), ImVars(), ImStmts(), flags());
                addFunction(mainFunc);
            }
            if (configFunc == null) {
                configFunc = ImFunction(emptyTrace, "config", ImVars(), ImVoid(), ImVars(), ImStmts(), flags());
                addFunction(configFunc);
            }
            finishInitFunctions();
            EliminateCallFunctionsWithAnnotation.process(imProg);
            removeDuplicateNatives(imProg);
            sortEverything();
            return imProg;
        } catch (CompileError t) {
            throw t;
        } catch (Throwable t) {
            WLogger.severe(t);
            throw new RuntimeException("There was a Wurst bug in the translation of " + Utils.printElementWithSource(lasttranslatedThing) + ": " + t
                    .getMessage() +
                    "\nPlease open a ticket with source code and the error log.", t);
        }
    }

    /**
     * Number all the compiletime functions and expressions,
     * so that the one with the lowest number can be executed first.
     * <p>
     * Dependendend packages are executed first and inside a package
     * it goes from top to bottom.
     */
    private void calculateCompiletimeOrder() {
        Set<WPackage> visited = new HashSet<>();
        ImmutableCollection<WPackage> packages = wurstProg.attrPackages().values();

        for (WPackage p : packages) {
            calculateCompiletimeOrder_walk(p, visited);
        }
    }

    private void calculateCompiletimeOrder_walk(WPackage p, Set<WPackage> visited) {
        if (!visited.add(p)) {
            return;
        }
        for (WPackage dep : p.attrInitDependencies()) {
            calculateCompiletimeOrder_walk(dep, visited);
        }
        p.accept(new de.peeeq.wurstscript.ast.Element.DefaultVisitor() {
            @Override
            public void visit(FuncDef funcDef) {
                super.visit(funcDef);
                if (funcDef.attrIsCompiletime()) {
                    compiletimeFlags.put(funcDef, new FunctionFlagCompiletime(compiletimeOrderCounter++));
                }
            }

            @Override
            public void visit(ExprFunctionCall fc) {
                super.visit(fc);
                if (fc.getFuncName().equals("compiletime")) {
                    compiletimeExpressionsOrder.put(fc, compiletimeOrderCounter++);
                }
            }
        });
    }


    /**
     * sorting everything is supposed to make the translation deterministic
     */
    private void sortEverything() {
        sortList(imProg.getClasses());
        sortList(imProg.getGlobals());
        sortList(imProg.getFunctions());
        for (ImClass c : imProg.getClasses()) {
            sortList(c.getFields());
            sortList(c.getMethods());
        }
    }


    private <T extends Element> void sortList(List<T> list) {
        List<T> classes = removeAll(list);
        Comparator<T> comparator = Comparator.comparing(this::getQualifiedClassName);
        classes.sort(comparator);
        list.addAll(classes);
    }

    public <T> List<T> removeAll(List<T> list) {
        List<T> result = new ArrayList<>();
        while (!list.isEmpty()) {
            result.add(0, list.remove(list.size() - 1));
        }
        return result;
    }

    private String getQualifiedClassName(Element c) {
        return getQualifiedClassName(c.attrTrace());
    }


    private String getQualifiedClassName(de.peeeq.wurstscript.ast.Element e) {
        String result = "";
        if (e instanceof NamedScope) {
            NamedScope ns = (NamedScope) e;
            result = ns.getName();
        }
        de.peeeq.wurstscript.ast.Element parent = e.getParent();
        if (parent == null) {
            return result;
        }
        parent = parent.attrNearestNamedScope();
        if (parent == null) {
            return result;
        }
        return getQualifiedClassName(parent) + "_" + result;
    }


    /***
     * this phase removes duplicate native declarations
     */
    private void removeDuplicateNatives(ImProg imProg) {
        Map<String, ImFunction> natives = new HashMap<>();
        Map<ImFunction, ImFunction> removed = new HashMap<>();
        ListIterator<ImFunction> it = imProg.getFunctions().listIterator();
        while (it.hasNext()) {
            ImFunction f = it.next();
            if (f.isNative() && natives.containsKey(f.getName())) {
                ImFunction existing = natives.get(f.getName());
                if (!compatibleTypes(f, existing)) {
                    throw new CompileError(f.attrTrace().attrErrorPos(), "Native function definition conflicts with other native function defined in " +
                            existing.attrTrace().attrErrorPos());
                }
                // remove duplicate
                it.remove();
                removed.put(f, existing);
            } else {
                natives.put(f.getName(), f);
            }
        }
        // rewrite removed links
        imProg.accept(new ImProg.DefaultVisitor() {
            public void visit(ImFunctionCall e) {
                super.visit(e);
                if (removed.containsKey(e.getFunc())) {
                    e.setFunc(removed.get(e.getFunc()));
                }
            }

            public void visit(ImFuncRef e) {
                super.visit(e);
                if (removed.containsKey(e.getFunc())) {
                    e.setFunc(removed.get(e.getFunc()));
                }
            }
        });
    }


    /**
     * checks if two functions f and g have compatible types
     */
    private boolean compatibleTypes(ImFunction f, ImFunction g) {
        if (!f.getReturnType().equalsType(g.getReturnType())) {
            return false;
        }
        if (f.getParameters().size() != g.getParameters().size()) {
            return false;
        }
        for (int i = 0; i < f.getParameters().size(); i++) {
            if (!f.getParameters().get(i).getType().equalsType(g.getParameters().get(i).getType())) {
                return false;
            }
        }
        return true;
    }


    private ArrayList<FunctionFlag> flags(FunctionFlag... flags) {
        return Lists.newArrayList(flags);
    }


    private void translateCompilationUnit(CompilationUnit cu) {
        lasttranslatedThing = cu;
        // TODO can we make this smarter? Only translate functions which are actually called...
        for (WPackage p : cu.getPackages()) {
            lasttranslatedThing = p;
            p.imTranslateTLD(this);
        }
        for (JassToplevelDeclaration tld : cu.getJassDecls()) {
            lasttranslatedThing = tld;
            tld.imTranslateTLD(this);
        }
    }


    private void finishInitFunctions() {
        // init globals, at beginning of main func:
        getMainFunc().getBody().add(0, ImFunctionCall(emptyTrace, globalInitFunc, ImExprs(), false, CallType.NORMAL));


        for (ImFunction initFunc : initFuncMap.values()) {
            addFunction(initFunc);
        }
        Set<WPackage> calledInitializers = Sets.newLinkedHashSet();

        ImVar initTrigVar = prepareTrigger();

        for (WPackage p : Utils.sortByName(initFuncMap.keySet())) {
            callInitFunc(calledInitializers, p, initTrigVar);
        }

        ImFunction native_DestroyTrigger = getNativeFunc("DestroyTrigger");
        if (native_DestroyTrigger != null) {
            getMainFunc().getBody().add(JassIm.ImFunctionCall(emptyTrace, native_DestroyTrigger,
                    JassIm.ImExprs(JassIm.ImVarAccess(initTrigVar)), false, CallType.NORMAL));
        }
    }

    @NotNull
    private ImVar prepareTrigger() {
        ImVar initTrigVar = JassIm.ImVar(emptyTrace, JassIm.ImSimpleType("trigger"), "initTrig", false);
        getMainFunc().getLocals().add(initTrigVar);

        // initTrigVar = CreateTrigger()
        ImFunction createTrigger = getNativeFunc("CreateTrigger");
        if (createTrigger != null) {
            getMainFunc().getBody().add(JassIm.ImSet(getMainFunc().getTrace(), initTrigVar,
                    JassIm.ImFunctionCall(getMainFunc().getTrace(), getNativeFunc("CreateTrigger"), JassIm.ImExprs(), false, CallType.NORMAL)));
        }
        return initTrigVar;
    }


    private ImFunction getNativeFunc(String funcName) {
        ImmutableCollection<FuncLink> wurstFunc = wurstProg.lookupFuncs(funcName);
        if (wurstFunc.isEmpty()) {
            return null;
        }
        return getFuncFor((TranslatedToImFunction) Utils.getFirst(wurstFunc).getDef());
    }

    private void callInitFunc(Set<WPackage> calledInitializers, WPackage p, ImVar initTrigVar) {
        Preconditions.checkNotNull(p);
        if (calledInitializers.contains(p)) {
            return;
        }
        calledInitializers.add(p);
        // first initialize all packages imported by this package:
        for (WPackage dep : p.attrInitDependencies()) {
            callInitFunc(calledInitializers, dep, initTrigVar);
        }
        ImFunction initFunc = initFuncMap.get(p);
        if (initFunc == null) {
            return;
        }
        if (initFunc.getBody().size() == 0) {
            return;
        }
        boolean successful = createInitFuncCall(p, initTrigVar, initFunc);

        if (!successful) {
            getMainFunc().getBody().add(ImFunctionCall(initFunc.getTrace(), initFunc, ImExprs(), false, CallType.NORMAL));
        }
    }


    private boolean createInitFuncCall(WPackage p, ImVar initTrigVar, ImFunction initFunc) {
        ImStmts mainBody = getMainFunc().getBody();

        ImFunction native_ClearTrigger = getNativeFunc("TriggerClearConditions");
        ImFunction native_TriggerAddCondition = getNativeFunc("TriggerAddCondition");
        ImFunction native_Condition = getNativeFunc("Condition");
        ImFunction native_TriggerEvaluate = getNativeFunc("TriggerEvaluate");
        ImFunction native_DisplayTimedTextToPlayer = getNativeFunc("DisplayTimedTextToPlayer");
        ImFunction native_GetLocalPlayer = getNativeFunc("GetLocalPlayer");

        if (native_ClearTrigger == null
                || native_TriggerAddCondition == null
                || native_Condition == null
                || native_TriggerEvaluate == null
                || native_DisplayTimedTextToPlayer == null
                || native_GetLocalPlayer == null
                ) {
            return false;
        }


        // rewrite init func to return boolean true:
        initFunc.setReturnType(WurstTypeBool.instance().imTranslateType());
        initFunc.accept(new ImFunction.DefaultVisitor() {
            @Override
            public void visit(ImReturn imReturn) {
                super.visit(imReturn);
                imReturn.setReturnValue(JassIm.ImBoolVal(true));
            }
        });
        de.peeeq.wurstscript.ast.Element trace = initFunc.getTrace();
        initFunc.getBody().add(JassIm.ImReturn(trace, JassIm.ImBoolVal(true)));


        // TriggerAddCondition(initTrigVar, Condition(function myInit))
        mainBody.add(JassIm.ImFunctionCall(trace, native_TriggerAddCondition, JassIm.ImExprs(
                JassIm.ImVarAccess(initTrigVar),
                JassIm.ImFunctionCall(trace, native_Condition, JassIm.ImExprs(
                        JassIm.ImFuncRef(initFunc)), false, CallType.NORMAL)
        ), true, CallType.NORMAL));
        // if not TriggerEvaluate(initTrigVar) ...
        mainBody.add(JassIm.ImIf(trace,
                JassIm.ImOperatorCall(WurstOperator.NOT, JassIm.ImExprs(
                        JassIm.ImFunctionCall(trace, native_TriggerEvaluate,
                                JassIm.ImExprs(JassIm.ImVarAccess(initTrigVar)), false, CallType.NORMAL)
                )),
                // then: DisplayTimedTextToPlayer(GetLocalPlayer(), 0., 0., 45., "Could not initialize package")
                JassIm.ImStmts(
                        JassIm.ImFunctionCall(trace, native_DisplayTimedTextToPlayer, JassIm.ImExprs(
                                JassIm.ImFunctionCall(trace, native_GetLocalPlayer, JassIm.ImExprs(), false, CallType.NORMAL),
                                JassIm.ImRealVal("0."),
                                JassIm.ImRealVal("0."),
                                JassIm.ImRealVal("45."),
                                JassIm.ImStringVal("Could not initialize package " + p.getName() + ".")
                        ), false, CallType.NORMAL)
                ),
                // else:
                JassIm.ImStmts()));
        mainBody.add(JassIm.ImFunctionCall(trace, native_ClearTrigger,
                JassIm.ImExprs(JassIm.ImVarAccess(initTrigVar)), false, CallType.NORMAL));
        return true;
    }

    private void addFunction(ImFunction f) {
        imProg.getFunctions().add(f);
    }

    public void addGlobal(ImVar v) {
        imProg.getGlobals().add(v);
    }


    public void addGlobalInitalizer(ImVar v, PackageOrGlobal packageOrGlobal, VarInitialization initialExpr) {
        if (initialExpr instanceof NoExpr) {
            // nothing to initialize
            return;
        }


        ImFunction f;
        if (packageOrGlobal instanceof WPackage) {
            WPackage p = (WPackage) packageOrGlobal;
            f = getInitFuncFor(p);
        } else {
            f = globalInitFunc;
        }
        de.peeeq.wurstscript.ast.Element trace = packageOrGlobal == null ? emptyTrace : packageOrGlobal;
        if (initialExpr instanceof Expr) {
            Expr expr = (Expr) initialExpr;
            ImExpr translated = expr.imTranslateExpr(this, f);
            if (!v.getIsBJ()) {
                // add init statement for non-bj vars
                // bj-vars are already initalized by blizzard
                f.getBody().add(ImSet(trace, v, translated));
            }
            imProg.getGlobalInits().put(v, Collections.singletonList(translated));
        } else if (initialExpr instanceof ArrayInitializer) {
            ArrayInitializer arInit = (ArrayInitializer) initialExpr;
            List<ImExpr> translatedExprs = arInit.getValues().stream()
                    .map(expr -> expr.imTranslateExpr(this, f))
                    .collect(Collectors.toList());
            for (int i = 0; i < arInit.getValues().size(); i++) {
                ImExpr translated = translatedExprs.get(i);
                f.getBody().add(ImSetArray(trace, v, JassIm.ImIntVal(i), translated));
            }
            // add list of init-values to translatedExprs
            imProg.getGlobalInits().put(v, translatedExprs);
        }
    }

    public void addGlobalWithInitalizer(ImVar g, ImExpr initial) {
        imProg.getGlobals().add(g);
        getGlobalInitFunc().getBody().add(ImSet(g.getTrace(), g, initial));
        imProg.getGlobalInits().put(g, Collections.singletonList(initial));
    }


    public ImExpr getDefaultValueForJassType(ImType type) {
        if (type instanceof ImSimpleType) {
            ImSimpleType imSimpleType = (ImSimpleType) type;
            String typeName = imSimpleType.getTypename();
            return getDefaultValueForJassTypeName(typeName);
        } else if (type instanceof ImTupleType) {
            ImTupleType imTupleType = (ImTupleType) type;
            return getDefaultValueForJassType(imTupleType.getTypes().get(0));
        } else {
            throw new IllegalArgumentException("could not get default value for type " + type);
        }
    }

    private ImExpr getDefaultValueForJassTypeName(String typeName) {
        switch (typeName) {
            case "integer":
                return ImIntVal(0);
            case "real":
                return ImRealVal("0.");
            case "boolean":
                return ImBoolVal(false);
            default:
                return ImNull();
        }
    }

    public GetAForB<StructureDef, ImFunction> destroyFunc = new GetAForB<StructureDef, ImFunction>() {

        @Override
        public ImFunction initFor(StructureDef classDef) {
            ImVars params = ImVars(JassIm.ImVar(classDef, TypesHelper.imInt(), "this", false));
            ImFunction f = JassIm.ImFunction(classDef.getOnDestroy(), "destroy" + classDef.getName(), params, TypesHelper.imVoid(), ImVars(), ImStmts(),
                    flags());
            addFunction(f);
            return f;
        }
    };

    public GetAForB<StructureDef, ImMethod> destroyMethod = new GetAForB<StructureDef, ImMethod>() {

        @Override
        public ImMethod initFor(StructureDef classDef) {
            ImFunction impl = destroyFunc.getFor(classDef);
            ImMethod m = JassIm.ImMethod(classDef, "destroy" + classDef.getName(),
                    impl, Lists.<ImMethod>newArrayList(), false);
            return m;
        }
    };

    public GetAForB<ImClass, ImFunction> allocFunc = new GetAForB<ImClass, ImFunction>() {

        @Override
        public ImFunction initFor(ImClass c) {
            return JassIm.ImFunction(c.getTrace(), "alloc_" + c.getName(), JassIm.ImVars(), TypesHelper.imInt(),
                    JassIm.ImVars(), JassIm.ImStmts(), Collections.<FunctionFlag>emptyList());
        }

    };

    public GetAForB<ImClass, ImFunction> deallocFunc = new GetAForB<ImClass, ImFunction>() {

        @Override
        public ImFunction initFor(ImClass c) {
            return JassIm.ImFunction(c.getTrace(), "dealloc_" + c.getName(), JassIm.ImVars(JassIm.ImVar(c.getTrace(), TypesHelper.imInt(), "obj", false)),
                    TypesHelper.imVoid(),
                    JassIm.ImVars(), JassIm.ImStmts(), Collections.<FunctionFlag>emptyList());
        }

    };


    public ImFunction getFuncFor(TranslatedToImFunction funcDef) {
        if (functionMap.containsKey(funcDef)) {
            return functionMap.get(funcDef);
        }
        String name = getNameFor(funcDef);
        List<FunctionFlag> flags = flags();
        if (funcDef instanceof NativeFunc) {
            flags.add(IS_NATIVE);
        }
        if (isBJ(funcDef.getSource())) {
            flags.add(IS_BJ);
        }
        if (isExtern(funcDef)) {
            flags.add(FunctionFlagEnum.IS_EXTERN);
        }
        if (funcDef instanceof FuncDef) {
            FuncDef funcDef2 = (FuncDef) funcDef;
            if (funcDef2.attrIsCompiletime()) {
                FunctionFlagCompiletime flag = compiletimeFlags.get(funcDef);
                if (flag == null) {
                    throw new CompileError(funcDef.getSource(), "Compiletime flag not supported here.");
                }
                flags.add(flag);
            }
            if (funcDef2.attrHasAnnotation("compiletimenative")) {
                flags.add(FunctionFlagEnum.IS_COMPILETIME_NATIVE);
            }
            if (funcDef2.attrHasAnnotation("test")) {
                flags.add(IS_TEST);
            }
        }

        // Check if last parameter is vararg
        if (funcDef instanceof AstElementWithParameters) {
            WParameters params = ((AstElementWithParameters) funcDef).getParameters();
            if (params.size() >= 1 && params.get(params.size() - 1).attrIsVararg()) {
                flags.add(IS_VARARG);
            }
        }


        if (funcDef instanceof HasModifier) {
            HasModifier awm = (HasModifier) funcDef;
            for (Modifier m : awm.getModifiers()) {
                if (m instanceof Annotation) {
                    Annotation annotation = (Annotation) m;
                    flags.add(new FunctionFlagAnnotation(annotation.getAnnotationType()));
                }
            }
        }

        ImFunction f = JassIm.ImFunction(funcDef, name, ImVars(), ImVoid(), ImVars(), ImStmts(), flags);
        funcDef.imCreateFuncSkeleton(this, f);

        addFunction(f);
        functionMap.put(funcDef, f);
        return f;
    }


    private boolean isExtern(TranslatedToImFunction funcDef) {
        if (funcDef instanceof HasModifier) {
            HasModifier f = (HasModifier) funcDef;
            for (Modifier m : f.getModifiers()) {
                if (m instanceof Annotation) {
                    Annotation a = (Annotation) m;
                    if (a.getAnnotationType().equals("@extern")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    private boolean isBJ(WPos source) {
        String f = source.getFile().toLowerCase();
        return f.endsWith("blizzard.j") || f.endsWith("common.j");
    }

    public ImFunction getInitFuncFor(WPackage p) {
        // TODO more precise trace
        return initFuncMap.computeIfAbsent(p, p1 -> JassIm.ImFunction(p1, "init_" + p1.getName(), ImVars(), ImVoid(), ImVars(), ImStmts(), flags()));
    }

    /**
     * returns a suitable name for the given element
     * the returned name is a valid jass identifier
     */
    public String getNameFor(de.peeeq.wurstscript.ast.Element e) {
        if (e instanceof FuncDef) {
            FuncDef f = (FuncDef) e;
            if (f.attrNearestStructureDef() != null) {
                return getNameFor(f.attrNearestStructureDef()) + "_" + f.getName();
            }
        } else if (e instanceof ExtensionFuncDef) {
            ExtensionFuncDef f = (ExtensionFuncDef) e;
            return getNameFor(f.getExtendedType()) + "_" + f.getName();
        } else if (e instanceof TypeExprSimple) {
            TypeExprSimple t = (TypeExprSimple) e;
            return t.getTypeName();
        } else if (e instanceof TypeExprThis) {
            return "thistype";
        } else if (e instanceof TypeExprArray) {
            TypeExprArray t = (TypeExprArray) e;
            return getNameFor(t.getBase()) + "Array";
        } else if (e instanceof ModuleInstanciation) {
            ModuleInstanciation mi = (ModuleInstanciation) e;
            return getNameFor(mi.getParent().attrNearestNamedScope()) + "_" + mi.getName();
        }


        if (e instanceof AstElementWithNameId) {
            AstElementWithNameId wn = (AstElementWithNameId) e;
            return wn.getNameId().getName();
        } else if (e instanceof ConstructorDef) {
            return "new_" + e.attrNearestClassDef().getName();
        } else if (e instanceof OnDestroyDef) {
            return "ondestroy_" + e.attrNearestClassDef().getName();
        } else if (e instanceof ExprClosure) {
            return e.attrNearestNamedScope().getName() + "_closure";
        }
        throw new RuntimeException("unhandled case: " + e.getClass().getName());
    }

    public ImVar getThisVar(TranslatedToImFunction f) {
        if (f instanceof OnDestroyDef) {
            // special case for onDestroy defs
            // TODO also special case for constructors needed?
            OnDestroyDef od = (OnDestroyDef) f;
            if (od.getParent() instanceof ModuleInstanciation) {
                ModuleInstanciation mi = (ModuleInstanciation) od.getParent();
                ClassDef c = mi.attrNearestClassDef();
                f = c.getOnDestroy();
            }
        }
        if (thisVarMap.containsKey(f)) {
            return thisVarMap.get(f);
        }
        ImVar v = JassIm.ImVar(f, ImSimpleType("integer"), "this", false);
        thisVarMap.put(f, v);
        return v;
    }

    public ImVar getThisVar(ImFunction f, ExprThis e) {
        return getThisVarForNode(f, e);
    }

    public ImVar getThisVar(ImFunction f, ExprSuper e) {
        return getThisVarForNode(f, e);
    }

    private ImVar getThisVarForNode(ImFunction f, de.peeeq.wurstscript.ast.Element node1) {
        de.peeeq.wurstscript.ast.Element node = node1;
        while (node != null) {
            if (node instanceof TranslatedToImFunction && !(node instanceof ExprClosure)) {
                return getThisVar((TranslatedToImFunction) node);
            }
            node = node.getParent();
        }
        if (f.getParameters().isEmpty()) {
            throw new CompileError(node1.attrSource(), "Could not get 'this'.");
        }
        return f.getParameters().get(0);
    }


    public int getTupleIndex(TupleDef tupleDef, VarDef parameter) {
        int i = 0;
        for (WParameter p : tupleDef.getParameters()) {
            if (p == parameter) {
                return i;
            }
            i++;
        }
        throw new Error("");
    }


    public ImVar getVarFor(VarDef varDef) {
        ImVar v = varMap.get(varDef);
        if (v == null) {
            ImType type = varDef.attrTyp().imTranslateType();
            String name = varDef.getName();
            if (isNamedScopeVar(varDef)) {
                name = getNameFor(varDef.attrNearestNamedScope()) + "_" + name;
            }
            boolean isBj = isBJ(varDef.getSource());
            v = JassIm.ImVar(varDef, type, name, isBj);
            varMap.put(varDef, v);
        }
        return v;
    }

    private boolean isNamedScopeVar(VarDef varDef) {
        if (varDef.getParent() == null) {
            return false;
        }
        return varDef.getParent().getParent() instanceof NamedScope;
    }

    public WurstModel getWurstProg() {
        return wurstProg;
    }

    public ImProg imProg() {
        return imProg;
    }

    public boolean isTranslated(WPackage pack) {
        return translatedPackages.contains(pack);
    }

    public void setTranslated(WPackage pack) {
        translatedPackages.add(pack);
    }

    public boolean isTranslated(ClassDef c) {
        return translatedClasses.contains(c);
    }

    public void setTranslated(ClassDef c) {
        translatedClasses.add(c);
    }

    public List<ImStmt> translateStatements(ImFunction f, List<WStatement> statements) {
        List<ImStmt> result = Lists.newArrayList();
        for (WStatement s : statements) {
            lasttranslatedThing = s;
            ImStmt translated = s.imTranslateStmt(this, f);
            result.add(translated);
        }
        return result;
    }

    public void setMainFunc(ImFunction f) {
        if (mainFunc != null) {
            throw new Error("mainFunction already set");
        }
        mainFunc = f;
    }

    public void setConfigFunc(ImFunction f) {
        if (configFunc != null) {
            throw new Error("configFunction already set");
        }
        configFunc = f;
    }

    public Multimap<ImFunction, ImFunction> getCalledFunctions() {
        if (callRelations == null) {
            calculateCallRelationsAndUsedVariables();
        }
        return callRelations;
    }

    public void calculateCallRelationsAndUsedVariables() {
        callRelations = HashMultimap.create();
        usedVariables = Sets.newLinkedHashSet();
        readVariables = Sets.newLinkedHashSet();
        usedFunctions = Sets.newLinkedHashSet();
        calculateCallRelations(getMainFunc());
        calculateCallRelations(getConfFunc());

//		WLogger.info("USED FUNCS:");
//		for (ImFunction f : usedFunctions) {
//			WLogger.info("	" + f.getName());
//		}
    }

    private void calculateCallRelations(ImFunction f) {
        if (getUsedFunctions().contains(f)) {
            return;
        }
        getUsedFunctions().add(f);

        getUsedVariables().addAll(f.calcUsedVariables());
        getReadVariables().addAll(f.calcReadVariables());

        Set<ImFunction> calledFuncs = f.calcUsedFunctions();
        for (ImFunction called : calledFuncs) {
            if (f != called) { // ignore reflexive call relations
                getCallRelations().put(f, called);
            }
            calculateCallRelations(called);
        }

    }

    private Multimap<ImFunction, ImFunction> getCallRelations() {
        return callRelations;
    }


    public ImFunction getMainFunc() {
        return mainFunc;
    }

    public ImFunction getConfFunc() {
        return configFunc;
    }


    /**
     * returns a list of classes and functions implementing funcDef
     */
    public Map<ClassDef, FuncDef> getClassesWithImplementation(Collection<ClassDef> instances, FuncDef func) {
        if (func.attrIsPrivate()) {
            // private functions cannot be overridden
            return Collections.emptyMap();
        }
        Map<ClassDef, FuncDef> result = Maps.newLinkedHashMap();
        for (ClassDef c : instances) {
            FuncLink funcNameLink = null;
            WurstTypeClass cType = c.attrTypC();
            for (FuncLink nameLink : func.lookupMemberFuncs(cType, func.getName())) {
                if (nameLink.getDef() == func) {
                    funcNameLink = nameLink;
                }
            }
            if (funcNameLink == null) {
                throw new Error("Could not find " + Utils.printElementWithSource(func) + " in " + Utils.printElementWithSource(c));
            }
            for (NameLink nameLink : c.attrNameLinks().get(func.getName())) {
                NameDef nameDef = nameLink.getDef();
                if (nameLink.getDefinedIn() == c) {
                    if (nameLink instanceof FuncLink && nameLink.getDef() instanceof FuncDef) {
                        FuncLink funcLink = (FuncLink) nameLink;
                        FuncDef f = (FuncDef) funcLink.getDef();
                        // check if function f overrides func
                        if (WurstValidator.canOverride(funcLink, funcNameLink)) {
                            result.put(c, f);
                        }
                    }
                }
            }
        }
        return result;
    }


    private Map<ClassDef, List<Pair<ImVar, VarInitialization>>> classDynamicInitMap = Maps.newLinkedHashMap();
    private Map<ClassDef, List<WStatement>> classInitStatements = Maps.newLinkedHashMap();

    public List<Pair<ImVar, VarInitialization>> getDynamicInits(ClassDef c) {
        return classDynamicInitMap.computeIfAbsent(c, k -> Lists.newArrayList());
    }


    Map<ConstructorDef, ImFunction> constructorFuncs = Maps.newLinkedHashMap();

    public ImFunction getConstructFunc(ConstructorDef constr) {
        ImFunction f = constructorFuncs.get(constr);
        if (f == null) {
            String name = constructorName(constr);
            ImVars params = ImVars(getThisVar(constr));
            for (WParameter p : constr.getParameters()) {
                params.add(getVarFor(p));
            }
            f = JassIm.ImFunction(constr, name, params, ImVoid(), ImVars(), ImStmts(), flags());
            addFunction(f);
            constructorFuncs.put(constr, f);
        }
        return f;
    }


    private String constructorName(ConstructorDef constr) {
        ArrayDeque<String> names = new ArrayDeque<>();
        de.peeeq.wurstscript.ast.Element e = constr;
        while (e != null) {
            if (e instanceof ClassOrModuleInstanciation) {
                ClassOrModuleInstanciation mi = (ClassOrModuleInstanciation) e;
                int index = mi.getConstructors().indexOf(constr);
                names.addFirst(mi.getName() + (index > 0 ? 1 + index : ""));
                if (e instanceof ClassDef) {
                    break;
                }
            }
            e = e.getParent();
        }
        return "construct_" + names.stream().collect(Collectors.joining("_"));
    }


    Map<ConstructorDef, ImFunction> constrNewFuncs = Maps.newLinkedHashMap();

    public ImFunction getConstructNewFunc(ConstructorDef constr) {
        ImFunction f = constrNewFuncs.get(constr);
        if (f == null) {
            String name = "new_" + constr.attrNearestClassDef().getName();
            f = JassIm.ImFunction(constr, name, ImVars(), TypesHelper.imInt(), ImVars(), ImStmts(), flags());
            addFunction(f);
            constrNewFuncs.put(constr, f);
        }
        return f;
    }

    public ImProg getImProg() {
        return imProg;
    }


    private Multimap<InterfaceDef, ClassDef> interfaceInstances = null;

    public Collection<ClassDef> getInterfaceInstances(InterfaceDef interfaceDef) {
        if (interfaceInstances == null) {
            calculateInterfaceInstances();
        }
        return interfaceInstances.get(interfaceDef);
    }

    private void calculateInterfaceInstances() {
        interfaceInstances = HashMultimap.create();
        for (CompilationUnit cu : wurstProg) {
            for (ClassDef c : cu.attrGetByType().classes) {
                for (WurstTypeInterface i : c.attrTypC().transitiveSuperInterfaces()) {
                    interfaceInstances.put(i.getDef(), c);
                }
            }
        }
    }


    private Multimap<ClassDef, ClassDef> subclasses = null;
    private Multimap<ClassDef, ClassDef> directSubclasses = null;

    private boolean isEclipseMode = false;

    public Collection<ClassDef> getSubClasses(ClassDef classDef) {
        calculateSubclasses();
        return subclasses.get(classDef);
    }

    private void calculateSubclasses() {
        if (subclasses != null) {
            return;
        }
        calculateDirectSubclasses();
        subclasses = Utils.transientClosure(directSubclasses);
    }


    private void calculateDirectSubclasses() {
        if (directSubclasses != null) {
            return;
        }
        directSubclasses = HashMultimap.create();
        for (ClassDef c : classes()) {
            WurstTypeClass extendedClass = c.attrTypC().extendedClass();
            if (extendedClass != null) {
                directSubclasses.put(((ClassDef) extendedClass.getDef()), c);
            }
        }
    }

    /**
     * calculates list of all classes
     * ignoring the ones in modules, only module instantiations
     */
    private List<ClassDef> classes() {
        List<ClassDef> result = new ArrayList<>();
        for (CompilationUnit cu : wurstProg) {
            for (WPackage p : cu.getPackages()) {
                for (WEntity e : p.getElements()) {
                    if (e instanceof ClassDef) {
                        ClassDef c = (ClassDef) e;
                        classesAdd(result, c);
                    }
                }
            }
        }
        return result;

    }

    private void classesAdd(List<ClassDef> result, ClassOrModuleInstanciation c) {
        if (c instanceof ClassDef) {
            result.add(((ClassDef) c));
        }
        for (ClassDef ic : c.getInnerClasses()) {
            classesAdd(result, ic);
        }
        for (ModuleInstanciation mi : c.getModuleInstanciations()) {
            classesAdd(result, mi);
        }
    }

    private List<ImFunction> compiletimeFuncs = Lists.newArrayList();

    public void addCompiletimeFunc(ImFunction f) {
        compiletimeFuncs.add(f);
    }

    public int getEnumMemberId(EnumMember enumMember) {
        return ((EnumMembers) enumMember.getParent()).indexOf(enumMember);
    }

    private ImFunction getDebugPrintFunction() {
        return debugPrintFunction;
    }

    public boolean isEclipseMode() {
        return isEclipseMode;
    }

    public void setEclipseMode(boolean enabled) {
        isEclipseMode = enabled;
    }

    public ImmutableTree<ImVar> getVarsForTuple(ImVar v) {

        ImmutableTree<ImVar> result = varsForTupleVar.get(v);
        if (result == null) {
            if (v.getType() instanceof ImArrayType || v.getType() instanceof ImSimpleType) {
                result = ImmutableTree.leaf(v);
            } else {
                result = createVarsForType(v.getName(), v.getType(), false, v.getTrace());
            }

//			if (v.getType() instanceof ImArrayType || v.getType() instanceof ImSimpleType) {
//				result = ImmutableTree.leaf(v);
//			} else {
//				result = Lists.newArrayList();
//				addVarsForType(result, v.getName(), v.getType(), false, v.getTrace());
//			}
            varsForTupleVar.put(v, result);
        }
        return result;
    }

    private ImmutableTree<ImVar> createVarsForType(String name, ImType type, boolean array, de.peeeq.wurstscript.ast.Element tr) {
        if (type instanceof ImTupleType) {
            ImTupleType tt = (ImTupleType) type;
            int i = 0;
            Builder<ImmutableTree<ImVar>> ts = ImmutableList.builder();
            for (ImType t : tt.getTypes()) {
                ts.add(createVarsForType(name + "_" + tt.getNames().get(i), t, array, tr));
                i++;
            }
            return ImmutableTree.node(ts.build());
        } else if (type instanceof ImTupleArrayType) {
            ImTupleArrayType tt = (ImTupleArrayType) type;
            Builder<ImmutableTree<ImVar>> ts = ImmutableList.builder();
            for (ImType t : tt.getTypes()) {
                ts.add(createVarsForType(name, t, true, tr));
            }
            return ImmutableTree.node(ts.build());
        } else if (type instanceof ImVoid) {
            return ImmutableTree.empty();
        } else {
            if (array && type instanceof ImSimpleType) {
                ImSimpleType st = (ImSimpleType) type;
                type = JassIm.ImArrayType(st.getTypename());
            }
            return ImmutableTree.leaf(JassIm.ImVar(tr, type, name, false));
        }
    }


    private void addVarsForType(List<ImVar> result, String name, ImType type, boolean array, de.peeeq.wurstscript.ast.Element tr) {
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(result);
        // TODO handle names
        if (type instanceof ImTupleType) {
            ImTupleType tt = (ImTupleType) type;
            int i = 0;
            for (ImType t : tt.getTypes()) {
                addVarsForType(result, name + "_" + tt.getNames().get(i), t, false, tr);
                i++;
            }
        } else if (type instanceof ImTupleArrayType) {
            ImTupleArrayType tt = (ImTupleArrayType) type;
            for (ImType t : tt.getTypes()) {
                addVarsForType(result, name, t, true, tr);
            }
        } else if (type instanceof ImVoid) {

        } else {
            if (array && type instanceof ImSimpleType) {
                ImSimpleType st = (ImSimpleType) type;
                type = JassIm.ImArrayType(st.getTypename());
            }
            result.add(JassIm.ImVar(tr, type, name, false));
        }

    }

    private Map<ImFunction, List<ImVar>> tempReturnVars = Maps.newLinkedHashMap();

    public List<ImVar> getTupleTempReturnVarsFor(ImFunction f) {
        List<ImVar> result = tempReturnVars.get(f);
        if (result == null) {
            result = Lists.newArrayList();
            addVarsForType(result, f.getName() + "_return", getOriginalReturnValue(f), false, f.getTrace());
            if (result.size() > 1) {
                imProg.getGlobals().addAll(result);
                // if we only have one return var it will never get used
            }
            tempReturnVars.put(f, result);
        }
        return result;
    }

    private Map<ImFunction, ImType> originalReturnValues = Maps.newLinkedHashMap();


    public void setOriginalReturnValue(ImFunction f, ImType t) {
        originalReturnValues.put(f, t);
    }

    public ImType getOriginalReturnValue(ImFunction f) {
        return originalReturnValues.computeIfAbsent(f, ImFunction::getReturnType);
    }

    public void assertProperties(AssertProperty... properties1) {
        final Set<AssertProperty> properties = Sets.newEnumSet(Lists.newArrayList(properties1), AssertProperty.class);
        assertProperties(properties, imProg);
    }

    private void assertProperties(Set<AssertProperty> properties, Element e) {
        if (e instanceof ElementWithLeft) {
            checkVar(((ElementWithLeft) e).getLeft(), properties);
        }
        if (e instanceof ElementWithVar) {
            checkVar(((ElementWithVar) e).getVar(), properties);
        }
        if (properties.contains(AssertProperty.NOTUPLES)) {
            if (e instanceof ImTupleExpr
                    || e instanceof ImTupleSelection
                    ) {
                throw new Error("contains tuple expr " + e);
            }
            if (e instanceof ImVar) {
                ImVar v = (ImVar) e;
                if (v.getType() instanceof ImTupleType
                        || v.getType() instanceof ImTupleArrayType) {
                    throw new Error("contains tuple var: " + v + " in\n" + v.getParent().getParent());
                }
            }
        }
        if (properties.contains(AssertProperty.FLAT)) {
            if (e instanceof ImStatementExpr) {
                throw new Error("contains statementExpr " + e);
            }
        }
        for (int i = 0; i < e.size(); i++) {
            assertProperties(properties, e.get(i));
        }
    }

    private void checkVar(ImVar left, Set<AssertProperty> properties) {
        if (left.getParent() == null) {
            throw new Error("var not attached: " + left);
        }
        if (properties.contains(AssertProperty.NOTUPLES)) {
            if (left.getType() instanceof ImTupleType
                    || left.getType() instanceof ImTupleArrayType) {
                throw new Error("program contains tuple var " + left);
            }
        }
    }

    public Set<ImVar> getUsedVariables() {
        if (usedVariables == null) {
            calculateCallRelationsAndUsedVariables();
        }
        return usedVariables;
    }

    public Set<ImVar> getReadVariables() {
        if (readVariables == null) {
            calculateCallRelationsAndUsedVariables();
        }
        return readVariables;
    }

    public Set<ImFunction> getUsedFunctions() {
        if (usedFunctions == null) {
            calculateCallRelationsAndUsedVariables();
        }
        return usedFunctions;
    }

    public boolean isUnitTestMode() {
        return isUnitTestMode;
    }


    Map<StructureDef, @Nullable ImClass> classForStructureDef = Maps.newLinkedHashMap();

    public ImClass getClassFor(StructureDef s) {
        return classForStructureDef.computeIfAbsent(s, s1 -> JassIm.ImClass(s1, s1.getName(), JassIm.ImVars(), JassIm.ImMethods(),
                Lists.<ImClass>newArrayList()));
    }


    Map<FuncDef, ImMethod> methodForFuncDef = Maps.newLinkedHashMap();

    public ImMethod getMethodFor(FuncDef f) {
        ImMethod m = methodForFuncDef.get(f);
        if (m == null) {
            ImFunction imFunc = getFuncFor(f);
            m = JassIm.ImMethod(f, elementNameWithPath(f), imFunc, Lists.<ImMethod>newArrayList(), false);
            methodForFuncDef.put(f, m);
        }
        return m;
    }

    public ClassManagementVars getClassManagementVarsFor(ImClass c) {
        return getClassManagementVars().get(c);
    }


    private Map<ImClass, ClassManagementVars> classManagementVars = null;

    private @Nullable ImFunction errorFunc;

    public Map<ImClass, ClassManagementVars> getClassManagementVars() {
        if (classManagementVars != null) {
            return classManagementVars;
        }
        // create partitions, such that each sub-class and super-class are in
        // the same partition
        Partitions<ImClass> p = new Partitions<>();
        for (ImClass c : imProg.getClasses()) {
            p.add(c);
            for (ImClass sc : c.getSuperClasses()) {
                p.union(c, sc);
            }
        }
        // generate typeId variables
        classManagementVars = Maps.newLinkedHashMap();
        for (ImClass c : imProg.getClasses()) {
            ImClass rep = p.getRep(c);
            ClassManagementVars v = classManagementVars.computeIfAbsent(rep, r -> new ClassManagementVars(r, this));
            classManagementVars.put(c, v);
        }
        return classManagementVars;
    }


    public ImFunction getGlobalInitFunc() {
        return globalInitFunc;
    }


    public ImExpr imError(de.peeeq.wurstscript.ast.Element trace, ImExpr message) {
        ImFunction ef = errorFunc;
        if (ef == null) {
            Optional<ImFunction> f = findErrorFunc().map(this::getFuncFor);
            ef = errorFunc = f.orElseGet(this::makeDefaultErrorFunc);
        }
        ImExprs arguments = JassIm.ImExprs(message);
        return JassIm.ImFunctionCall(trace, ef, arguments, false, CallType.NORMAL);
    }

    private ImFunction makeDefaultErrorFunc() {
        ImVar msgVar = JassIm.ImVar(emptyTrace, TypesHelper.imString(), "msg", false);
        ImVars parameters = JassIm.ImVars(msgVar);
        ImType returnType = JassIm.ImVoid();
        ImVars locals = JassIm.ImVars();
        ImStmts body = JassIm.ImStmts();

        // print message:
        body.add(JassIm.ImFunctionCall(emptyTrace, getDebugPrintFunction(),
                JassIm.ImExprs(JassIm.ImVarAccess(msgVar)),
                false, CallType.NORMAL));
        // TODO divide by zero to crash thread:


//		stmts.add(JassAst.JassStmtCall("BJDebugMsg", 
//				JassAst.JassExprlist(JassAst.JassExprBinary(
//						JassAst.JassExprStringVal("|cffFF3A29Wurst Error:|r" + nl), 
//						JassAst.JassOpPlus(),
//						s.getMessage().translate(translator)))));
//		// crash thread (divide by zero)
//		stmts.add(JassAst.JassStmtCall("I2S", JassAst.JassExprlist(JassAst.JassExprBinary(JassAst.JassExprIntVal("1"), JassAst.JassOpDiv(), Jas
//				               

        List<FunctionFlag> flags = Lists.newArrayList();
        return JassIm.ImFunction(emptyTrace, "error", parameters, returnType, locals, body, flags);
    }


    private Optional<FuncDef> findErrorFunc() throws CompileError {
        PackageLink p = wurstProg.lookupPackage("ErrorHandling");
        if (p == null) {
            return Optional.empty();
        }
        ImmutableCollection<FuncLink> funcs = p.getDef().getElements().lookupFuncs("error");
        if (funcs.isEmpty()) {
            return Optional.empty();
        } else if (funcs.size() > 1) {
            return Optional.empty();
        }
        FuncDef f = (FuncDef) funcs.stream().findAny().get().getDef();
        return Optional.of(f);
    }

    int getCompiletimeExpressionsOrder(FunctionCall fc) {
        return compiletimeExpressionsOrder.getOrDefault(fc, 0);
    }


}
