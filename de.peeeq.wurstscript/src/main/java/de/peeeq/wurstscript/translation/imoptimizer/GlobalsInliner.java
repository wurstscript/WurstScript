package de.peeeq.wurstscript.translation.imoptimizer;

import com.google.common.collect.Sets;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.ast.GlobalVarDef;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.translation.imtranslation.ImHelper;
import de.peeeq.wurstscript.translation.imtranslation.ImTranslator;
import de.peeeq.wurstscript.utils.Utils;
import de.peeeq.wurstscript.validation.NamePreservation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class GlobalsInliner implements OptimizerPass {
    @Override
    public int optimize(ImTranslator trans) {
        int obsoleteCount = 0;
        ImProg prog = trans.getImProg();
        prog.clearAttributes(); // TODO only clear read/write attributes
        LiteralConstantAnalysis literalConstants = analyzeLiteralConstants(trans, prog);

        Set<ImVar> obsoleteVars = Sets.newLinkedHashSet();
        for (final ImVar v : prog.getGlobals()) {
            if (trans.isUnitTestMode() && v.getName().equals("MagicFunctions_compiletime")) {
                // in unit test mode we run tests and compiletime functions with optimizations,
                // so it is important, that we do not optimize away the compiletime constant
                continue;
            }
            if (v.getName().equals("MagicFunctions_isLua") && trans.isLuaTarget()) {
                // In Lua mode, isLua must evaluate to true.
                // Normal inlining would use the declared value (false); override it here.
                for (ImVarRead read : new ArrayList<>(v.attrReads())) {
                    read.replaceBy(JassIm.ImBoolVal(true));
                }
                for (ImVarWrite write : new ArrayList<>(v.attrWrites())) {
                    if (write.getParent() != null) {
                        write.replaceBy(ImHelper.nullExpr());
                    }
                }
                obsoleteVars.add(v);
                obsoleteCount++;
                continue;
            }
            if (v.getType() instanceof ImArrayType
                || v.getType() instanceof ImArrayTypeMulti) {
                // cannot optimize arrays yet
                continue;
            }
            if (NamePreservation.isPreserved(v)) {
                // keep names which are part of the external Warcraft III API
                continue;
            }

            boolean literalConstant = literalConstants.safeConstants.contains(v);
            if (v.attrWrites().size() == 1 || literalConstant) {
                ImExpr right = null;
                ImVarWrite obs = null;
                if (literalConstant) {
                    obs = literalConstants.replacementWrites.get(v);
                    right = obs.getRight();
                } else {
                    for (ImVarWrite write : v.attrWrites()) {
                        ImFunction func = write.getNearestFunc();
                        if (isInInitGlobals(func)) {
                            right = write.getRight();
                            obs = write;
                            break;
                        }
                    }
                }
                if (obs == null) {
                    continue;
                }

                ImExpr replacement = findReplacement(right, obs);
                if (replacement != null) {
                    for (ImVarRead v3 : v.attrReads()) {
                        v3.replaceBy(replacement.copy());
                    }
                }
                if ((replacement != null || v.attrReads().size() == 0) && v.attrWrites().size() == 1) {
                    obsoleteVars.add(v);
                }
            } else if (v.attrWrites().size() > 1 && !(v.getType() instanceof ImTupleType)) {
                List<ImVarWrite> initWrites = new ArrayList<>();
                for (ImVarWrite imVarWrite : v.attrWrites()) {
                    ImFunction nearestFunc = imVarWrite.getNearestFunc();
                    if (isInInitGlobals(nearestFunc)) {
                        initWrites.add(imVarWrite);
                    }
                }
                if (initWrites.size() == 1) {
                    if(v.getType() instanceof ImSimpleType) {
                        ImVarWrite initWrite = initWrites.get(0);
                        ImExpr write = initWrite.getRight();
                        try {
                            ImExpr defaultValue = ImHelper.defaultValueForType((ImSimpleType) v.getType());
                            boolean isDefault = defaultValue.structuralEquals(write);
                            if (isDefault) {
                                // Only remove the init write when it assigns the default value.
                                // Never touch non-init writes here.
                                initWrite.replaceBy(ImHelper.nullExpr());
                            }
                        } catch (Exception e) {
                            throw new CompileError(write.attrTrace().attrErrorPos(),
                                "Could not inline " + Utils.printElementWithSource(Optional.of(v.getTrace())),
                                CompileError.ErrorType.ERROR, e);
                        }
                    }
                }
            }

        }
        obsoleteCount += obsoleteVars.size();
        for (ImVar i : obsoleteVars) {
            // remove the write
            if (i.attrWrites().size() > 0) {
                ImVarWrite write = Utils.getFirstAndOnly(i.attrWrites());
                if (write.getParent() != null) {
                    write.replaceBy(write.getRight().copy());
                }
            }
        }
        prog.getGlobals().removeAll(obsoleteVars);
        return obsoleteCount;
    }

    @Nullable
    private ImExpr findReplacement(ImExpr right, ImVarWrite obs) {
        ImExpr replacement;
        if (right instanceof ImIntVal) {
            ImIntVal val = (ImIntVal) right;
            replacement = (JassIm.ImIntVal(val.getValI()));
            if (obs.getParent() != null)
                obs.replaceBy(ImHelper.nullExpr());
        } else if (right instanceof ImRealVal) {
            ImRealVal val = (ImRealVal) right;
            replacement = (JassIm.ImRealVal(val.getValR()));
            if (obs.getParent() != null)
                obs.replaceBy(ImHelper.nullExpr());
        } else if (right instanceof ImStringVal) {
            ImStringVal val = (ImStringVal) right;
            replacement = (JassIm.ImStringVal(val.getValS()));
            if (obs.getParent() != null)
                obs.replaceBy(ImHelper.nullExpr());
        } else if (right instanceof ImBoolVal) {
            ImBoolVal val = (ImBoolVal) right;
            replacement = (JassIm.ImBoolVal(val.getValB()));
            if (obs.getParent() != null)
                obs.replaceBy(ImHelper.nullExpr());
        } else {
            replacement = null;
        }
        return replacement;
    }

    private static boolean isLiteral(ImExpr expr) {
        return expr instanceof ImIntVal || expr instanceof ImRealVal || expr instanceof ImStringVal || expr instanceof ImBoolVal;
    }

    @Override
    public String getName() {
        return "Globals Inlined";
    }


    private static boolean isInInitGlobals(ImFunction func) {
        return func != null && func.getName().equals("initGlobals");
    }

    /**
     * A package constant is assigned at runtime in a package initializer. Replacing all reads is
     * valid only when no startup path can observe the default value before that emitted assignment.
     * Analyze the actual IM startup order once, including transitive calls and function references,
     * rather than trying to reconstruct translation and dependency order from source positions.
     */
    private static LiteralConstantAnalysis analyzeLiteralConstants(ImTranslator trans, ImProg prog) {
        List<ImFunction> initializationOrder = trans.getInitializationOrder();
        IdentityHashMap<ImStmt, Integer> statementRanks = new IdentityHashMap<>();
        IdentityHashMap<ImVarWrite, Long> writeRanks = new IdentityHashMap<>();
        for (int functionRank = 0; functionRank < initializationOrder.size(); functionRank++) {
            ImFunction initializer = initializationOrder.get(functionRank);
            for (int statementRank = 0; statementRank < initializer.getBody().size(); statementRank++) {
                statementRanks.put(initializer.getBody().get(statementRank), statementRank);
            }
            int[] writeRank = {0};
            int currentFunctionRank = functionRank;
            initializer.getBody().accept(new ImStmt.DefaultVisitor() {
                @Override
                public void visit(ImSet write) {
                    long rank = ((long) currentFunctionRank << 32) | (writeRank[0]++ & 0xffffffffL);
                    writeRanks.put(write, rank);
                    super.visit(write);
                }
            });
        }

        List<ImVar> candidates = new ArrayList<>();
        IdentityHashMap<ImVar, ImVarWrite> replacementWrites = new IdentityHashMap<>();
        for (ImVar var : prog.getGlobals()) {
            if (!isSourceConstant(var)) {
                continue;
            }
            ImVarWrite replacementWrite = null;
            ImExpr replacement = null;
            long replacementRank = Long.MAX_VALUE;
            boolean eligible = !var.attrWrites().isEmpty();
            for (ImVarWrite write : var.attrWrites()) {
                ImFunction initializer = write.getNearestFunc();
                ImStmt statement = initializer == null ? null
                    : topLevelStatement((de.peeeq.wurstscript.jassIm.Element) write, initializer);
                Integer statementRank = statementRanks.get(statement);
                Long writeRank = writeRanks.get(write);
                if (statement == null || statementRank == null
                    || writeRank == null || !isLiteral(write.getRight())) {
                    eligible = false;
                    break;
                }
                if (replacement == null) {
                    replacement = write.getRight();
                } else if (!replacement.structuralEquals(write.getRight())) {
                    eligible = false;
                    break;
                }
                if (writeRank < replacementRank) {
                    replacementRank = writeRank;
                    replacementWrite = write;
                }
            }
            if (eligible) {
                candidates.add(var);
                replacementWrites.put(var, replacementWrite);
            }
        }
        if (candidates.isEmpty()) {
            return new LiteralConstantAnalysis(identitySet(), replacementWrites);
        }

        Set<ImFunction> functions = identitySet();
        functions.addAll(ImHelper.calculateFunctionsOfProg(prog));
        functions.addAll(initializationOrder);
        IdentityHashMap<ImFunction, BitSet> readsByFunction = new IdentityHashMap<>();
        IdentityHashMap<ImStmt, BitSet> readsByStatement = new IdentityHashMap<>();
        IdentityHashMap<ImStmt, BitSet> writesByStatement = new IdentityHashMap<>();
        BitSet unsafe = new BitSet(candidates.size());

        for (int i = 0; i < candidates.size(); i++) {
            ImVar candidate = candidates.get(i);
            for (ImVarRead read : candidate.attrReads()) {
                ImFunction function = read.getNearestFunc();
                if (function == null) {
                    unsafe.set(i);
                    continue;
                }
                readsByFunction.computeIfAbsent(function, ignored -> new BitSet()).set(i);
                ImStmt statement = topLevelStatement((de.peeeq.wurstscript.jassIm.Element) read, function);
                if (statement != null) {
                    readsByStatement.computeIfAbsent(statement, ignored -> new BitSet()).set(i);
                }
            }
            for (ImVarWrite write : candidate.attrWrites()) {
                ImFunction function = write.getNearestFunc();
                ImStmt statement = function == null ? null
                    : topLevelStatement((de.peeeq.wurstscript.jassIm.Element) write, function);
                if (statement != null) {
                    writesByStatement.computeIfAbsent(statement, ignored -> new BitSet()).set(i);
                }
            }
        }

        IdentityHashMap<ImFunction, Set<ImFunction>> callers = new IdentityHashMap<>();
        ArrayDeque<ImFunction> undiscovered = new ArrayDeque<>(functions);
        while (!undiscovered.isEmpty()) {
            ImFunction caller = undiscovered.removeFirst();
            readsByFunction.computeIfAbsent(caller, ignored -> new BitSet());
            for (ImFunction callee : caller.calcUsedFunctions()) {
                if (callee == null) {
                    continue;
                }
                callers.computeIfAbsent(callee, ignored -> identitySet()).add(caller);
                if (functions.add(callee)) {
                    undiscovered.addLast(callee);
                }
            }
        }

        ArrayDeque<ImFunction> changedFunctions = new ArrayDeque<>();
        Set<ImFunction> queued = identitySet();
        for (Map.Entry<ImFunction, BitSet> entry : readsByFunction.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                changedFunctions.addLast(entry.getKey());
                queued.add(entry.getKey());
            }
        }
        while (!changedFunctions.isEmpty()) {
            ImFunction callee = changedFunctions.removeFirst();
            queued.remove(callee);
            BitSet calleeReads = readsByFunction.get(callee);
            for (ImFunction caller : callers.getOrDefault(callee, Collections.emptySet())) {
                BitSet callerReads = readsByFunction.computeIfAbsent(caller, ignored -> new BitSet());
                int before = callerReads.cardinality();
                callerReads.or(calleeReads);
                if (callerReads.cardinality() != before && queued.add(caller)) {
                    changedFunctions.addLast(caller);
                }
            }
        }

        BitSet pending = new BitSet(candidates.size());
        pending.set(0, candidates.size());
        ImFunction config = trans.getConfFunc();
        if (config != null) {
            scanStartupStatements(config.getBody(), pending, unsafe, readsByStatement, writesByStatement,
                readsByFunction);
        }
        if (!initializationOrder.isEmpty()) {
            scanStartupStatements(initializationOrder.get(0).getBody(), pending, unsafe, readsByStatement,
                writesByStatement, readsByFunction);
            scanMainPrefix(trans, initializationOrder, pending, unsafe, readsByStatement, writesByStatement,
                readsByFunction);
        }
        for (int i = 1; i < initializationOrder.size(); i++) {
            ImFunction initializer = initializationOrder.get(i);
            scanStartupStatements(initializer.getBody(), pending, unsafe, readsByStatement, writesByStatement,
                readsByFunction);
        }
        unsafe.or(pending);

        Set<ImVar> safeConstants = identitySet();
        for (int i = 0; i < candidates.size(); i++) {
            if (!unsafe.get(i)) {
                safeConstants.add(candidates.get(i));
            }
        }
        return new LiteralConstantAnalysis(safeConstants, replacementWrites);
    }

    private static void scanMainPrefix(ImTranslator trans, List<ImFunction> initializationOrder,
                                       BitSet pending, BitSet unsafe,
                                       Map<ImStmt, BitSet> readsByStatement,
                                       Map<ImStmt, BitSet> writesByStatement,
                                       Map<ImFunction, BitSet> readsByFunction) {
        Set<ImFunction> packageInitializers = identitySet();
        packageInitializers.addAll(initializationOrder.subList(1, initializationOrder.size()));
        for (ImStmt statement : trans.getMainFunc().getBody()) {
            Set<ImFunction> usedFunctions = directlyUsedFunctions(statement);
            if (!Collections.disjoint(usedFunctions, packageInitializers)) {
                return;
            }
            scanStartupStatements(Collections.singleton(statement), pending, unsafe, readsByStatement,
                writesByStatement, readsByFunction);
        }
    }

    private static void scanStartupStatements(Collection<ImStmt> statements, BitSet pending, BitSet unsafe,
                                               Map<ImStmt, BitSet> readsByStatement,
                                               Map<ImStmt, BitSet> writesByStatement,
                                               Map<ImFunction, BitSet> readsByFunction) {
        for (ImStmt statement : statements) {
            BitSet reads = readsByStatement.containsKey(statement)
                ? (BitSet) readsByStatement.get(statement).clone() : new BitSet();
            for (ImFunction used : directlyUsedFunctions(statement)) {
                BitSet functionReads = readsByFunction.get(used);
                if (functionReads != null) {
                    reads.or(functionReads);
                }
            }
            reads.and(pending);
            unsafe.or(reads);
            BitSet writes = writesByStatement.get(statement);
            if (writes != null) {
                pending.andNot(writes);
            }
        }
    }

    private static Set<ImFunction> directlyUsedFunctions(ImStmt statement) {
        Set<ImFunction> result = identitySet();
        statement.accept(new ImStmt.DefaultVisitor() {
            @Override
            public void visit(ImFunctionCall call) {
                super.visit(call);
                result.add(call.getFunc());
            }

            @Override
            public void visit(ImFuncRef ref) {
                super.visit(ref);
                result.add(ref.getFunc());
            }

            @Override
            public void visit(ImMethodCall call) {
                super.visit(call);
                if (call.getMethod().getImplementation() != null) {
                    result.add(call.getMethod().getImplementation());
                }
                for (ImMethod subMethod : call.getMethod().getSubMethods()) {
                    if (subMethod.getImplementation() != null) {
                        result.add(subMethod.getImplementation());
                    }
                }
            }
        });
        return result;
    }

    @Nullable
    @SuppressWarnings("ReferenceEquality")
    private static ImStmt topLevelStatement(de.peeeq.wurstscript.jassIm.Element element, ImFunction function) {
        de.peeeq.wurstscript.jassIm.Element current = element;
        while (current != null && current.getParent() != function.getBody()) {
            current = current.getParent();
        }
        return current instanceof ImStmt ? (ImStmt) current : null;
    }

    private static boolean isSourceConstant(ImVar var) {
        if (!(var.getTrace() instanceof GlobalVarDef)) {
            return false;
        }
        if (var.getName().equals("MagicFunctions_compiletime")
            || var.getName().equals("MagicFunctions_isLua")) {
            // These values depend on compiler execution context/backend and are lowered by their
            // dedicated paths. They are not ordinary source literals for package-constant folding.
            return false;
        }
        GlobalVarDef global = (GlobalVarDef) var.getTrace();
        return global.attrIsConstant() && !global.hasAnnotation("@configurable");
    }

    private static <T> Set<T> identitySet() {
        return Collections.newSetFromMap(new IdentityHashMap<>());
    }

    private static final class LiteralConstantAnalysis {
        private final Set<ImVar> safeConstants;
        private final Map<ImVar, ImVarWrite> replacementWrites;

        private LiteralConstantAnalysis(Set<ImVar> safeConstants, Map<ImVar, ImVarWrite> replacementWrites) {
            this.safeConstants = safeConstants;
            this.replacementWrites = replacementWrites;
        }
    }

}
