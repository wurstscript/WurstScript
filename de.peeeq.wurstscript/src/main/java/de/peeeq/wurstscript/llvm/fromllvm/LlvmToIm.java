package de.peeeq.wurstscript.llvm.fromllvm;

import de.peeeq.wurstscript.WurstOperator;
import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.llvm.ast.*;
import de.peeeq.wurstscript.translation.imtranslation.CallType;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlag;
import de.peeeq.wurstscript.translation.imtranslation.GetAForB;
import de.peeeq.wurstscript.types.TypesHelper;
import de.peeeq.wurstscript.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import de.peeeq.wurstscript.ast.Element;

/**
 * Transform Llvm programs to our own intermediate language
 * <p>
 * Could first make Im basic block based, which would also simplify translation
 * of something like 'continue'.
 * <p>
 * Then translation to Jass or an Im-transform has to handle the complicated stuff.
 * <p>
 * Maybe useful resources:
 * https://www2.cs.arizona.edu/~collberg/Teaching/553/2011/Resources/cifuentes96structuring.pdf
 * <p>
 * TODO handle phi-nodes
 * TODO add llvm translations to test-suite
 * TODO handle missing instructions
 * TODO optimize basic block translation -- most cases can probably be translated to a loop
 */
public class LlvmToIm {
    private final ImVars globals = JassIm.ImVars();
    private final ImFunctions functions = JassIm.ImFunctions();
    private final ImClasses classes = JassIm.ImClasses();
    private final Map<ImVar, List<ImExpr>> globalInits = new LinkedHashMap<>();
    private final WurstModel trace;

    private final GetAForB<Proc, ImFunction> funcForProcedure = new GetAForB<Proc, ImFunction>() {
        @Override
        public ImFunction initFor(Proc a) {
            ImVars params = JassIm.ImVars();
            for (Parameter parameter : a.getParameters()) {
                params.add(transformParameter(parameter));
            }
            ImType returnType = transformType(a.getReturnType());

            ImVars locals = JassIm.ImVars();

            List<FunctionFlag> flags = Collections.emptyList();
            return JassIm.ImFunction(trace, a.getName(), params, returnType, locals, JassIm.ImStmts(), flags);
        }
    };

    private final GetAForB<Global, ImVar> varForGlobal = new GetAForB<Global, ImVar>() {
        @Override
        public ImVar initFor(Global a) {
            return JassIm.ImVar(trace, transformType(a.getType()), a.getName(), false);
        }
    };

    private final GetAForB<Variable, ImVar> varFor = new GetAForB<Variable, ImVar>() {
        @Override
        public ImVar initFor(Variable a) {
            return JassIm.ImVar(trace, transformType(a.calculateType()), a.getName(), false);
        }
    };
    private HashMap<BasicBlock, ImBlock> blockMap;

    private void transformBasicBlocks(ImFunction func, BasicBlockList basicBlocks) {
        List<ImBlock> blocks = new ArrayList<>();
        blockMap = new LinkedHashMap<>();
        for (BasicBlock block : basicBlocks) {
            blockMap.put(block, new ImBlock(block.getName()));
        }

        for (BasicBlock block : basicBlocks) {
            blocks.add(transformBlock(func, block, blockMap));
        }
        blocksToStatements(func, blocks);
    }

    private void blocksToStatements(ImFunction func, List<ImBlock> blocks) {
        if (blocks.isEmpty()) {
            // skip
            return;
        } else if (blocks.size() == 1) {
            blocks.get(0).toStatements(this, func, func.getBody());
        } else {
            ImVar blockVar = JassIm.ImVar(trace, TypesHelper.imInt(), "currentBlock", false);
            func.getLocals().add(blockVar);
            func.getBody().add(JassIm.ImSet(trace, JassIm.ImVarAccess(blockVar), JassIm.ImIntVal(0)));
            ImStmts stmts = JassIm.ImStmts();
            func.getBody().add(JassIm.ImLoop(trace, stmts));

            for (int i = 0; i < blocks.size(); i++) {
                ImBlock block = blocks.get(i);
                block.setBlockVar(blockVar);
                block.setNumber(i);
            }

            for (int i = 0; i < blocks.size(); i++) {
                ImBlock block = blocks.get(i);
                ImStmts thenStmts;
                if (i < blocks.size() - 1) {

                    thenStmts = JassIm.ImStmts();
                    ImStmts elseStmts = JassIm.ImStmts();
                    stmts.add(JassIm.ImIf(trace, JassIm.ImOperatorCall(WurstOperator.EQ,
                            JassIm.ImExprs(JassIm.ImVarAccess(blockVar), JassIm.ImIntVal(i))),
                            thenStmts, elseStmts));

                    stmts = elseStmts;
                } else {
                    thenStmts = stmts;
                }
                block.toStatements(this, func, thenStmts);
            }
        }
    }

    private ImBlock transformBlock(ImFunction func, BasicBlock block, HashMap<BasicBlock, ImBlock> blockMap) {
        ImBlock imBlock = blockMap.get(block);
        TranslationContext ctxt = new TranslationContext(imBlock);
        for (Instruction instr : block) {
            if (instr instanceof TerminatingInstruction) {
                TerminatingInstruction ti = (TerminatingInstruction) instr;
                imBlock.setTerminalInstruction(transformTerminatingInstruction(ti, func, imBlock, ctxt));
                break;
            }
            transformInstr(instr, func, imBlock, ctxt);
        }

        ctxt.cleanup(func);
        return imBlock;
    }

    private TerminalInstruction transformTerminatingInstruction(TerminatingInstruction ti, ImFunction func, ImBlock imBlock, TranslationContext ctxt) {
        return ti.match(new TerminatingInstruction.Matcher<TerminalInstruction>() {
            @Override
            public TerminalInstruction case_Branch(Branch branch) {
                return new TerminalInstruction.ConditionalGoto(imBlock,
                        transformOperand(branch.getCondition(), ctxt),
                        blockMap.get(branch.getIfTrueLabel()),
                        blockMap.get(branch.getIfFalseLabel()));
            }

            @Override
            public TerminalInstruction case_ReturnVoid(ReturnVoid returnVoid) {
                return new TerminalInstruction.Return(imBlock, JassIm.ImNoExpr());
            }

            @Override
            public TerminalInstruction case_Jump(Jump jump) {
                return new TerminalInstruction.Goto(imBlock, blockMap.get(jump.getLabel()));
            }

            @Override
            public TerminalInstruction case_ReturnExpr(ReturnExpr returnExpr) {
                return new TerminalInstruction.Return(imBlock, transformOperand(returnExpr.getReturnValue(), ctxt));
            }

            @Override
            public TerminalInstruction case_HaltWithError(HaltWithError haltWithError) {
                // TODO add error expression
                return new TerminalInstruction.Unreachable(imBlock);
            }
        });
    }

    private void transformInstr(Instruction instr, ImFunction func, ImBlock into, TranslationContext ctxt) {
        instr.match(new Instruction.MatcherVoid() {
            @Override
            public void case_CallVoid(CallVoid c) {
                into.addStmt(translateCall(c.getFunction(), c.getArguments(), ctxt));

            }

            @Override
            public void case_ReturnVoid(ReturnVoid returnVoid) {
                into.setTerminalInstruction(new TerminalInstruction.Return(into, JassIm.ImNoExpr()));

            }

            @Override
            public void case_Print(Print print) {
                throw new RuntimeException("TODO" + instr);
            }

            @Override
            public void case_ReturnExpr(ReturnExpr returnExpr) {
                throw new RuntimeException("TODO " + instr);
            }

            @Override
            public void case_Assign(Assign assign) {
                TemporaryVar tv = assign.getVar();
                ImVar v = varFor.getFor(tv);
                func.getLocals().add(v);
                assign.getValueInstruction().match(new de.peeeq.wurstscript.llvm.ast.ValueInstruction.MatcherVoid() {
                    @Override
                    public void case_Alloc(Alloc alloc) {
                        throw new RuntimeException("TODO " + instr);
                    }

                    @Override
                    public void case_PhiNode(PhiNode phiNode) {
                        // nothing to do, phi nodes are handled separately
                    }

                    @Override
                    public void case_Bitcast(Bitcast bitcast) {
                        throw new RuntimeException("TODO " + instr);
                    }

                    @Override
                    public void case_GetElementPtr(GetElementPtr getElementPtr) {
                        throw new RuntimeException("TODO " + instr);
                    }

                    @Override
                    public void case_Alloca(Alloca alloca) {
                        // nothing to do, this is just allocating space for the locla var
                    }

                    @Override
                    public void case_Load(Load load) {
                        ctxt.addSet(tv, v, transformOperand(load.getAddress(), ctxt));
                    }

                    @Override
                    public void case_Select(Select select) {
                        ImExpr ifTrue = transformOperand(select.getIfTrue(), ctxt);
                        ImExpr ifFalse = transformOperand(select.getIfFalse(), ctxt);
                        into.getStmts().add(
                                JassIm.ImIf(
                                        trace,
                                        transformOperand(select.getCond(), ctxt),
                                        JassIm.ImStmts(
                                                JassIm.ImSet(trace, JassIm.ImVarAccess(v), ifTrue)
                                        ),
                                        JassIm.ImStmts(
                                                JassIm.ImSet(trace, JassIm.ImVarAccess(v), ifFalse)
                                        )
                                )
                        );
                    }

                    @Override
                    public void case_Call(Call call) {
                        ImFunctionCall fc = translateCall(call.getFunction(), call.getArguments(), ctxt);
                        into.getStmts().add(JassIm.ImSet(trace, JassIm.ImVarAccess(v), fc));
                    }

                    @Override
                    public void case_BinaryOperation(BinaryOperation binaryOperation) {
                        Operator op = binaryOperation.getOperator();
                        if (op instanceof Shl) {
                            if (binaryOperation.getRight() instanceof ConstInt) {
                                ConstInt right = (ConstInt) binaryOperation.getRight();
                                ImExprs args = JassIm.ImExprs(
                                        transformOperand(binaryOperation.getLeft(), ctxt),
                                        JassIm.ImIntVal(Utils.intPow(2, right.getIntVal()))
                                );
                                ctxt.addSet(tv, v, JassIm.ImOperatorCall(WurstOperator.MULT, args));
                                return;
                            }
                        }
                        ImExprs args = JassIm.ImExprs(
                                transformOperand(binaryOperation.getLeft(), ctxt),
                                transformOperand(binaryOperation.getRight(), ctxt)
                        );

                        WurstOperator wop = transformOperator(op);
                        ctxt.addSet(tv, v, JassIm.ImOperatorCall(wop, args));
                    }
                });
            }

            @Override
            public void case_Store(Store store) {
                ImLExpr addr = transformAddress(store.getAddress(), ctxt);
                into.addStmt(JassIm.ImSet(trace, addr, transformOperand(store.getValue(), ctxt)));
            }

            @Override
            public void case_Branch(Branch branch) {
                throw new RuntimeException("TODO" + instr);
            }

            @Override
            public void case_CommentInstr(CommentInstr commentInstr) {
            }

            @Override
            public void case_Jump(Jump jump) {
                throw new RuntimeException("TODO" + instr);
            }

            @Override
            public void case_HaltWithError(HaltWithError haltWithError) {
                throw new RuntimeException("TODO" + instr);
            }
        });
    }

    private WurstOperator transformOperator(Operator operator) {
        return operator.match(new Operator.Matcher<WurstOperator>() {
            @Override
            public WurstOperator case_Sge(Sge sge) {
                throw new RuntimeException("TODO " + operator);
            }

            @Override
            public WurstOperator case_Mul(Mul mul) {
                return WurstOperator.MULT;
            }

            @Override
            public WurstOperator case_Sgt(Sgt sgt) {
                return WurstOperator.GREATER;
            }

            @Override
            public WurstOperator case_Fdiv(Fdiv fdiv) {
                return WurstOperator.DIV_REAL;
            }

            @Override
            public WurstOperator case_And(And and) {
                return WurstOperator.AND;
            }

            @Override
            public WurstOperator case_Sdiv(Sdiv sdiv) {
                return WurstOperator.DIV_INT;
            }

            @Override
            public WurstOperator case_Add(Add add) {
                return WurstOperator.PLUS;
            }

            @Override
            public WurstOperator case_Slt(Slt slt) {
                return WurstOperator.LESS;
            }

            @Override
            public WurstOperator case_Srem(Srem srem) {
                return WurstOperator.MOD_INT;
            }

            @Override
            public WurstOperator case_Eq(Eq eq) {
                return WurstOperator.EQ;
            }

            @Override
            public WurstOperator case_Fadd(Fadd fadd) {
                return WurstOperator.PLUS;
            }

            @Override
            public WurstOperator case_Shl(Shl shl) {
                throw new RuntimeException("TODO " + operator);
            }

            @Override
            public WurstOperator case_NotEq(NotEq notEq) {
                return WurstOperator.NOTEQ;
            }

            @Override
            public WurstOperator case_Fsub(Fsub fsub) {
                return WurstOperator.MINUS;
            }

            @Override
            public WurstOperator case_Or(Or or) {
                return WurstOperator.OR;
            }

            @Override
            public WurstOperator case_Xor(Xor xor) {
                throw new RuntimeException("TODO " + operator);
            }

            @Override
            public WurstOperator case_Sub(Sub sub) {
                return WurstOperator.MINUS;
            }

            @Override
            public WurstOperator case_Sle(Sle sle) {
                return WurstOperator.LESS_EQ;
            }

            @Override
            public WurstOperator case_Frem(Frem frem) {
                return WurstOperator.MOD_REAL;
            }

            @Override
            public WurstOperator case_Fmul(Fmul fmul) {
                return WurstOperator.MULT;
            }
        });
    }

    private ImLExpr transformAddress(Operand address, TranslationContext ctxt) {
        return (ImLExpr) transformOperand(address, ctxt);
    }

    @NotNull
    private ImFunctionCall translateCall(Operand func, OperandList callArgs, TranslationContext ctxt) {
        ImFunction calledFunc;
        if (func instanceof GlobalRef) {
            GlobalRef ref = (GlobalRef) func;
            if (ref.getGlobal() instanceof Proc) {
                calledFunc = funcForProcedure.getFor((Proc) ref.getGlobal());
            } else {
                throw new RuntimeException("TODO: " + ref);
            }
        } else {
            throw new RuntimeException("TODO" + func + " " + callArgs);
        }
        ImExprs args = JassIm.ImExprs();
        for (Operand arg : callArgs) {
            args.add(transformOperand(arg, ctxt));
        }
        return JassIm.ImFunctionCall(trace, calledFunc, args, true, CallType.NORMAL);
    }

    private ImExpr transformOperand(Operand arg, TranslationContext ctxt) {
        return arg.match(new Operand.Matcher<ImExpr>() {
            @Override
            public ImExpr case_GlobalRef(GlobalRef r) {
                return r.getGlobal().match(new GlobalDef.Matcher<ImExpr>() {
                    @Override
                    public ImExpr case_Global(Global global) {
                        return JassIm.ImVarAccess(varForGlobal.getFor(global));
                    }

                    @Override
                    public ImExpr case_Proc(Proc proc) {
                        throw new RuntimeException("TODO: " + arg);
                    }
                });
            }

            @Override
            public ImExpr case_ConstStruct(ConstStruct s) {
                ImExprs exprs = JassIm.ImExprs();
                for (Const value : s.getValues()) {
                    exprs.add(transformOperand(value, ctxt));
                }
                return JassIm.ImTupleExpr(exprs);
            }

            @Override
            public ImExpr case_Sizeof(Sizeof s) {
                TypeDef structType = s.getStructType();
                return JassIm.ImIntVal(structSize(structType));
            }

            @Override
            public ImExpr case_ConstBool(ConstBool c) {
                return JassIm.ImBoolVal(c.getBoolVal());
            }

            @Override
            public ImExpr case_ConstString(ConstString c) {
                return JassIm.ImStringVal(c.getStringVal());
            }

            @Override
            public ImExpr case_VarRef(VarRef v) {
                return ctxt.accessTemporary(v.getVariable());
            }

            @Override
            public ImExpr case_ConstInt(ConstInt c) {
                return JassIm.ImIntVal(c.getIntVal());
            }

            @Override
            public ImExpr case_Nullpointer(Nullpointer nullpointer) {
                return JassIm.ImNull();
            }
        });
    }

    private int structSize(TypeDef structType) {
        return structType.getFields().size();
    }

    @NotNull
    private ImVar transformParameter(Parameter parameter) {
        return JassIm.ImVar(trace, transformType(parameter.getType()), parameter.getName(), false);
    }

    private ImType transformType(Type type) {
        return type.match(new Type.Matcher<ImType>() {
            @Override
            public ImType case_TypeArray(TypeArray t) {
                throw new RuntimeException("TODO");
            }

            @Override
            public ImType case_TypeRef(TypeRef t) {
                return JassIm.ImSimpleType(t.getTypeDef().getName());
            }

            @Override
            public ImType case_TypeVoid(TypeVoid t) {
                return TypesHelper.imVoid();
            }

            @Override
            public ImType case_TypeProc(TypeProc t) {
                throw new RuntimeException("TODO");
            }

            @Override
            public ImType case_TypeBool(TypeBool t) {
                return TypesHelper.imBool();
            }

            @Override
            public ImType case_TypePointer(TypePointer t) {
                ImType pt = transformType(t.getTo());
                return pt;
            }

            @Override
            public ImType case_TypeInt(TypeInt typeInt) {
                return TypesHelper.imInt();
            }

            @Override
            public ImType case_TypeNullpointer(TypeNullpointer t) {
                throw new RuntimeException("TODO");
            }

            @Override
            public ImType case_TypeByte(TypeByte t) {
                throw new RuntimeException("TODO");
            }
        });
    }

    public LlvmToIm(WurstModel trace) {
        this.trace = trace;
    }

    public ImProg transformProg(Prog prog) {

        transformTypedefs(prog.getTypeDefs());
        transformGlobals(prog.getGlobals());
        transformProcedures(prog.getProcedures());

        ImProg imProg = JassIm.ImProg(trace, globals, functions, classes, globalInits);
//        ImTranslator trans = new ImTranslator(trace, false);
//        trans.setMainFunc(functions.stream().filter(f -> f.getName().equals("main")).findFirst().orElseGet(() -> {
//            throw new RuntimeException("Could not find main func");
//        }));
//        trans.setConfigFunc(functions.stream().filter(f -> f.getName().equals("config")).findFirst().orElseGet(() -> {
//            throw new RuntimeException("Could not find config func");
//        }));
//        new ImOptimizer(new TimeTaker.Default(), trans).removeGarbage();
        return imProg;
    }

    private void transformProcedures(ProcList procs) {
        for (Proc proc : procs) {
            transformProc(proc);
        }


    }

    private void transformProc(Proc proc) {
        ImFunction func = funcForProcedure.getFor(proc);
        functions.add(func);
        transformBasicBlocks(func, proc.getBasicBlocks());
    }

    private void transformGlobals(GlobalList globals) {

    }

    private void transformTypedefs(TypeDefList typeDefs) {

    }

    public Element getTrace() {
        return trace;
    }

    private class TranslationContext {
        private final ImBlock imBlock;
        // expressions currently stored in temporary vars
        // will only be used once
        // included expressions have no side-effects
        private final Map<Variable, ImExpr> tempExpr = new HashMap<>();
        private final Map<Variable, List<ImExpr>> tempUses = new HashMap<>();
        private final Set<ImSet> tempSets = new HashSet<>();

        public TranslationContext(ImBlock imBlock) {

            this.imBlock = imBlock;
        }

        public void addSet(TemporaryVar tv, ImVar v, ImExpr right) {
            ImSet imSet = JassIm.ImSet(trace, JassIm.ImVarAccess(v), right);
            imBlock.addStmt(imSet);
            tempSets.add(imSet);
            tempExpr.put(tv, right);
        }

        public ImExpr accessTemporary(Variable v) {
            ImExpr te = tempExpr.get(v);
            if (te != null) {
                ImExpr teCopy = te.copy();
                if (!(te instanceof ImLExpr)) {
                    tempUses.computeIfAbsent(v, vv -> new ArrayList<>()).add(teCopy);
                }
                return teCopy;
            }
            return JassIm.ImVarAccess(varFor.getFor(v));
        }

        public void cleanup(ImFunction func) {
            for (Map.Entry<Variable, List<ImExpr>> e : tempUses.entrySet()) {
                Variable v = e.getKey();
                List<ImExpr> uses = e.getValue();
                if (uses.size() > 1) {
                    // computation is used more than once --> replace uses with reference to temporary
                    for (ImExpr use : uses) {
                        use.replaceBy(JassIm.ImVarAccess(varFor.getFor(v)));
                    }
                }
            }

            Set<ImVar> usedVars = getUsedVars();
            // removed unnecessary temp assignments
            ListIterator<ImStmt> it = imBlock.getStmts().listIterator();
            Set<ImVar> localsToRemove = new HashSet<>();
            while (it.hasNext()) {
                ImStmt stmt = it.next();
                //noinspection SuspiciousMethodCalls
                if (tempSets.contains(stmt)) {
                    ImVar v = ((ImVarAccess) ((ImSet) stmt).getLeft()).getVar();
                    if (!usedVars.contains(v)) {
                        it.remove();
                        localsToRemove.add(v);
                    }
                }
            }
            func.getLocals().removeAll(localsToRemove);


        }

        private Set<ImVar> getUsedVars() {
            Set<ImVar> usedVars = new HashSet<>();
            imBlock.accept(new de.peeeq.wurstscript.jassIm.Element.DefaultVisitor() {


                @Override
                public void visit(ImVarAccess va) {
                    super.visit(va);
                    if (!va.isUsedAsLValue()) {
                        usedVars.add(va.getVar());
                    }
                }
            });
            return usedVars;
        }
    }
}
