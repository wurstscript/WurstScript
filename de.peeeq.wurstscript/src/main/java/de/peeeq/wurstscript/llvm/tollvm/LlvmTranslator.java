package de.peeeq.wurstscript.llvm.tollvm;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.peeeq.wurstscript.jassIm.*;
import de.peeeq.wurstscript.llvm.ast.*;
import de.peeeq.wurstscript.translation.imtranslation.FunctionFlagEnum;
import de.peeeq.wurstscript.translation.imtranslation.GetAForB;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class LlvmTranslator {


    private ImProg program;
    private TypeTranslator typeTranslator = new TypeTranslator(this);
    private StmtTranslator stmtTranslator = new StmtTranslator(this);
    private ExprTranslator exprTranslator = new ExprTranslator(this);
    private ExprLTranslator exprLTranslator = new ExprLTranslator(this);
    private Map<ImVar, Global> globals = new HashMap<>();
    private Map<ImClass, TypeDef> structFor = new HashMap<>();
    private Table<ImClass, ImVar, Integer> fieldIndex = HashBasedTable.create();
    private Map<String, Global> globalStringConstant = new HashMap<>();


    private Map<ImFunction, Proc> procFor = new HashMap<ImFunction, Proc>();

    private BasicBlock currentBlock;
    private Proc currentProc;
    private Map<ImVar, TemporaryVar> localVars = new HashMap<>();
    private Prog prog;

    private Type translateType(ImType t) {
        return t.match(typeTranslator);
    }

    private Parameter translateParameter(ImVar imVar) {
        return Ast.Parameter(translateType(imVar.getType()), imVar.getName());
    }

    public LlvmTranslator(ImProg program) {
        this.program = program;
    }

    public Prog translateProg() {
        prog = Ast.Prog(Ast.TypeDefList(), Ast.GlobalList(), Ast.ProcList());
        initClasses();
        initProcs();

        for (ImFunction function : program.getFunctions()) {
            translateFunction(function);
        }
        return prog;
    }

    private void initProcs() {
        for (ImFunction function : program.getFunctions()) {
            BasicBlockList blocks = Ast.BasicBlockList();
            Proc proc = Ast.Proc(
                    function.getName(),
                    function.isNative(),
                    translateType(function.getReturnType()),
                    function.getParameters()
                            .stream()
                            .map(LlvmTranslator.this::translateParameter)
                            .collect(Collectors.toCollection(Ast::ParameterList)),
                    blocks
            );
            prog.getProcedures().add(proc);
            procFor.put(function, proc);
        }
    }

    private void initClasses() {
        for (ImClass c : program.getClasses()) {
            StructFieldList fields = Ast.StructFieldList();
            structFor.put(c, Ast.TypeDef(c.getName(), false, fields));
        }

        for (ImClass c : program.getClasses()) {
            StructFieldList fields = structFor.get(c).getFields();
            addFields(c, c, fields);
        }
    }

    private void addFields(ImClass c, ImClass superClass, StructFieldList fields) {
        for (ImClass sc : superClass.getSuperClasses()) {
            addFields(c, sc, fields);
        }
        for (ImVar v : superClass.getFields()) {
            StructField f = Ast.StructField(translateType(v.getType()), c.getName() + "_" + v.getName());
            fieldIndex.put(c, v, fields.size());
            fields.add(f);
        }
    }


    private void translateFunction(ImFunction function) {

        Proc p = procFor.get(function);
        if (function.hasFlag(FunctionFlagEnum.IS_NATIVE)) {
            // do not translate natives
            return;
        }


        currentProc = p;
        currentBlock = Ast.BasicBlock();
        currentBlock.setName("entry");
        startBlock(currentBlock);

        localVars.clear();
        // load parameters into alloca vars
        for (int i = 0; i < function.getParameters().size(); i++) {
            ImVar var = function.getParameters().get(i);
            Parameter param = p.getParameters().get(i);
            TemporaryVar v = Ast.TemporaryVar(var.getName());
            addInstruction(Ast.Assign(v, Ast.Alloca(translateType(var.getType()))));
            addInstruction(Ast.Store(Ast.VarRef(v), Ast.VarRef(param)));
            localVars.put(var, v);
        }
        // add alloca instructions for locals
        for (ImVar var : function.getLocals()) {
            TemporaryVar v = Ast.TemporaryVar(var.getName());
            addInstruction(Ast.Assign(v, Ast.Alloca(translateType(var.getType()))));
            localVars.put(var, v);
        }
        translateStmts(function.getBody());

        // can add return void here (if it was not void, then return would have been added before)
        addInstruction(Ast.ReturnVoid());

        // TODO add terminating instruction
    }

    void translateStmts(ImStmts stmts) {
        for (ImStmt stmt : stmts) {
            translateStmt(stmt);
        }
    }

    void translateStmt(ImStmt stmt) {
        stmt.match(stmtTranslator);
    }

    Operand translateExpr(ImExpr e) {
        return e.match(exprTranslator);
    }

    public Operand translateExprL(ImLExpr e) {
        return e.match(exprLTranslator);
    }

    void addInstruction(Instruction ins) {
        if (currentBlock == null) {
            // after return or otherwise unreachable
            return;
        }
        currentBlock.add(ins);
    }

    public void startBlock(BasicBlock b) {
        currentProc.getBasicBlocks().add(b);
        currentBlock = b;
    }

    public void finishCurrentBlock() {
        currentBlock = null;
    }

    public Operand getVarLocation(ImVar v) {
        TemporaryVar tv = localVars.get(v);
        if (tv != null) {
            return Ast.VarRef(tv);
        }
        Global glob = globals.get(v);
        if (glob != null) {
            return Ast.GlobalRef(glob);
        }
        throw new RuntimeException("Variable " + v + " not found!");
    }

    public void addType(TypeDef type) {
        prog.getTypeDefs().add(type);
    }

    public Proc getProcFor(ImFunction func) {
        return procFor.get(func);
    }

    public TypeDef getStructFor(ImClass clazz) {
        return structFor.get(clazz);
    }


    enum BuiltinProc {
        free {
            @Override
            public Proc init(LlvmTranslator tr) {
                Proc proc = Ast.Proc("free", true, Ast.TypePointer(Ast.TypeByte()), Ast.ParameterList(Ast.Parameter(Ast.TypePointer(Ast.TypeByte()), "ptr")), Ast.BasicBlockList());
                tr.prog.getProcedures().add(0, proc);
                return proc;
            }
        }, alloc {
            @Override
            public Proc init(LlvmTranslator tr) {
                Proc proc = Ast.Proc("alloc", true, Ast.TypePointer(Ast.TypeByte()), Ast.ParameterList(Ast.Parameter(Ast.TypeInt(), "size")), Ast.BasicBlockList());
                tr.prog.getProcedures().add(0, proc);
                return proc;
            }
        };

        public  abstract Proc init(LlvmTranslator tr);

    }

    private HashMap<BuiltinProc, Proc> buitinProcs = new HashMap<>();

    public Proc builtinProc(BuiltinProc proc) {
        return buitinProcs.computeIfAbsent(proc, p -> p.init(this));
    }

    public Global getGlobalStringConstant(String str) {
        return globalStringConstant.computeIfAbsent(str, s -> {
            Type stringType = ((TypePointer) translateType(JassIm.ImSimpleType("string"))).getTo();
            Global global = Ast.Global(stringType, str, true, Ast.External());
            prog.getGlobals().add(global);
            return global;
        });
    }
}
