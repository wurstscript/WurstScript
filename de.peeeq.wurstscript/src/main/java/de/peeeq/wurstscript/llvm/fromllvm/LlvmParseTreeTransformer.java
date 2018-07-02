package de.peeeq.wurstscript.llvm.fromllvm;

import de.peeeq.wurstscript.llvm.antlr.LlvmParser;
import de.peeeq.wurstscript.llvm.antlr.LlvmParser.FunctionDeclContext;
import de.peeeq.wurstscript.llvm.antlr.LlvmParser.FunctionDefContext;
import de.peeeq.wurstscript.llvm.antlr.LlvmParser.GlobalDeclContext;
import de.peeeq.wurstscript.llvm.antlr.LlvmParser.GlobalDefContext;
import de.peeeq.wurstscript.llvm.ast.*;
import org.antlr.v4.runtime.RuleContext;

import java.util.*;

/**
 *
 */
public class LlvmParseTreeTransformer {

    // TODO first create these maps before starting transformation
    private Map<String, TypeDef> typeDefs = new LinkedHashMap<>();
    private Map<String, GlobalDef> globalDefs = new LinkedHashMap<>();
    private Map<String, Variable> localVariables = new LinkedHashMap<>();

    public Prog transformModule(LlvmParser.ModuleContext module) {
        TypeDefList typeDefs = Ast.TypeDefList();
        GlobalList globals = Ast.GlobalList();
        ProcList procedures = Ast.ProcList();

        // setup named elements
        for (LlvmParser.TopLevelEntityContext tle : module.topLevelEntity()) {
            if (tle.globalDecl() != null) {
                Global g = Ast.Global(Ast.TypeVoid(), id(tle.globalDecl().globalIdent()), true, Ast.External());
                this.globalDefs.put(g.getName(), g);
            } else if (tle.typeDef() != null) {
                TypeDef t = Ast.TypeDef(id(tle.typeDef().localIdent()), true, Ast.StructFieldList());
                this.typeDefs.put(t.getName(), t);
            } else if (tle.functionDef() != null) {
                Proc proc = Ast.Proc(id(tle.functionDef().functionHeader().globalIdent()), false, Ast.TypeVoid(), Ast.ParameterList(), Ast.BasicBlockList());
                this.globalDefs.put(proc.getName(), proc);
            } else if (tle.functionDecl() != null) {
                Proc proc = Ast.Proc(id(tle.functionDecl().functionHeader().globalIdent()), false, Ast.TypeVoid(), Ast.ParameterList(), Ast.BasicBlockList());
                this.globalDefs.put(proc.getName(), proc);
            }
        }


        for (LlvmParser.TopLevelEntityContext tle : module.topLevelEntity()) {
            if (tle.globalDecl() != null) {
                Global g = transformGobalDecl(tle.globalDecl());
                globals.add(g);
            } else if (tle.globalDef() != null) {
                globals.add(transformGlobalDef(tle.globalDef()));
            } else if (tle.functionDecl() != null) {
                procedures.add(transformFunctionDecl(tle.functionDecl()));
            } else if (tle.functionDef() != null) {
                procedures.add(transformFunctionDef(tle.functionDef()));
            } else if (tle.sourceFilename() != null) {
                System.err.println("source filename = " + tle.sourceFilename().stringLit().getText());
            } else if (tle.typeDef() != null) {
                TypeDef t = transformTypeDef(tle.typeDef());
                this.typeDefs.put(t.getName(), t);
                typeDefs.add(t);
            } else if (tle.attrGroupDef() != null) {
                System.err.println("unhandled case: " + str(tle));
            } else {
                throw new RuntimeException("unhandled case " + str(tle));

            }

        }

        return Ast.Prog(typeDefs, globals, procedures);
    }

    private TypeDef transformTypeDef(LlvmParser.TypeDefContext t) {
        String name = id(t.localIdent());
        TypeDef td = lookupType(name);
        if (t.opaqueType() != null) {
            td.setOpaque(true);
        } else if (t.type() != null) {
            throw new RuntimeException("unhandled case " + str(t));
        }
        return td;
    }

    private TypeDef lookupType(String name) {
        TypeDef t = typeDefs.get(name);
        if (t == null) {
            throw new RuntimeException("Cannot find type " + name);
        }
        return t;
    }


    private String str(RuleContext tle) {
        return tle.toStringTree(Arrays.asList(LlvmParser.ruleNames));
    }

    private Proc transformFunctionDef(FunctionDefContext f) {
        localVariables.clear();
        // TODO fill localvar map before translation


        String name = id(f.functionHeader().globalIdent());
        Proc proc = (Proc) lookupGlobal(name);
        proc.setIsExtern(false);
        proc.setReturnType(transformType(f.functionHeader().type()));
        proc.getBasicBlocks().addAll(transformFunctionBody(f.functionBody()));
        return proc;
    }

    private List<BasicBlock> transformFunctionBody(LlvmParser.FunctionBodyContext b) {
        List<BasicBlock> blocks = new ArrayList<>();
        for (LlvmParser.BasicBlockContext bb : b.basicBlockList().basicBlock()) {
            blocks.add(transformBasicBlock(bb));
        }
        return blocks;
    }

    private BasicBlock transformBasicBlock(LlvmParser.BasicBlockContext bb) {
        BasicBlock block = Ast.BasicBlock();
        block.setName(id(bb.optLabelIdent()));
        for (LlvmParser.InstructionContext ins : bb.instructions().instruction()) {
            block.add(transformInstruction(ins));
        }
        return block;
    }

    private Instruction transformInstruction(LlvmParser.InstructionContext ins) {
        if (ins.valueInstruction() != null) {
            ValueInstruction vi = transformValueInstruction(ins.valueInstruction());
            if (ins.localIdent() == null) {
                Call c = (Call) vi;
                Operand f = c.getFunction();
                f.setParent(null);
                OperandList args = c.getArguments();
                args.setParent(null);
                return Ast.CallVoid(f, args);
            } else {
                TemporaryVar v = Ast.TemporaryVar(id(ins.localIdent()));
                localVariables.put(v.getName(), v);
                return Ast.Assign(v,
                        vi);
            }

        }

        return Ast.CommentInstr("todo " + str(ins));
//        throw new RuntimeException("todo " + str(ins));
    }

    private ValueInstruction transformValueInstruction(LlvmParser.ValueInstructionContext i) {
        if (i.callInst() != null) {
            return transformCallInstr(i.callInst());
        } else if (i.shlInst() != null) {
            LlvmParser.ShlInstContext instr = i.shlInst();
            Operand left = transformValue(instr.value().get(0));
            Operand right = transformValue(instr.value().get(1));
            return Ast.BinaryOperation(left, Ast.Shl(), right);
        } else if (i.addInst() != null) {
            LlvmParser.AddInstContext instr = i.addInst();
            Operand left = transformValue(instr.value().get(0));
            Operand right = transformValue(instr.value().get(1));
            return Ast.BinaryOperation(left, Ast.Add(), right);
        } else if (i.orInst() != null) {
            LlvmParser.OrInstContext instr = i.orInst();
            Operand left = transformValue(instr.value().get(0));
            Operand right = transformValue(instr.value().get(1));
            return Ast.BinaryOperation(left, Ast.Or(), right);
        } else if (i.iCmpInst() != null) {
            LlvmParser.ICmpInstContext instr = i.iCmpInst();
            Operand left = transformValue(instr.value().get(0));
            Operand right = transformValue(instr.value().get(1));
            Operator op;
            switch (instr.iPred().getText()) {
                case "eq":
                    op = Ast.Eq();
                    break;
                case "ne":
                    op = Ast.NotEq();
                    break;
                case "sge":
                    op = Ast.Sge();
                    break;
                case "sgt":
                    op = Ast.Sgt();
                    break;
                case "sle":
                    op = Ast.Sle();
                    break;
                case "slt":
                    op = Ast.Slt();
                    break;
//                case "uge":
//                    op = Ast.
//                    break;
//                case "ugt":
//                    op = Ast.
//                    break;
//                case "ule":
//                    op = Ast.
//                    break;
//                case "ult":
//                    op = Ast.
//                    break;
                default:
                    throw new RuntimeException("unhandled case: " + str(instr.iPred()));
            }

            return Ast.BinaryOperation(left, Ast.Add(), right);
        } else if (i.addInst() != null) {
            LlvmParser.AddInstContext instr = i.addInst();
            Operand left = transformValue(instr.value().get(0));
            Operand right = transformValue(instr.value().get(1));
            return Ast.BinaryOperation(left, Ast.Add(), right);
        } else if (i.addInst() != null) {
            LlvmParser.AddInstContext instr = i.addInst();
            Operand left = transformValue(instr.value().get(0));
            Operand right = transformValue(instr.value().get(1));
            return Ast.BinaryOperation(left, Ast.Add(), right);
        } else if (i.addInst() != null) {
            LlvmParser.AddInstContext instr = i.addInst();
            Operand left = transformValue(instr.value().get(0));
            Operand right = transformValue(instr.value().get(1));
            return Ast.BinaryOperation(left, Ast.Add(), right);
        } else if (i.addInst() != null) {
            LlvmParser.AddInstContext instr = i.addInst();
            Operand left = transformValue(instr.value().get(0));
            Operand right = transformValue(instr.value().get(1));
            return Ast.BinaryOperation(left, Ast.Add(), right);
        } else if (i.addInst() != null) {
            LlvmParser.AddInstContext instr = i.addInst();
            Operand left = transformValue(instr.value().get(0));
            Operand right = transformValue(instr.value().get(1));
            return Ast.BinaryOperation(left, Ast.Add(), right);
        } else if (i.addInst() != null) {
            LlvmParser.AddInstContext instr = i.addInst();
            Operand left = transformValue(instr.value().get(0));
            Operand right = transformValue(instr.value().get(1));
            return Ast.BinaryOperation(left, Ast.Add(), right);
        } else if (i.addInst() != null) {
            LlvmParser.AddInstContext instr = i.addInst();
            Operand left = transformValue(instr.value().get(0));
            Operand right = transformValue(instr.value().get(1));
            return Ast.BinaryOperation(left, Ast.Add(), right);
        } else if (i.addInst() != null) {
            LlvmParser.AddInstContext instr = i.addInst();
            Operand left = transformValue(instr.value().get(0));
            Operand right = transformValue(instr.value().get(1));
            return Ast.BinaryOperation(left, Ast.Add(), right);
        } else if (i.addInst() != null) {
            LlvmParser.AddInstContext instr = i.addInst();
            Operand left = transformValue(instr.value().get(0));
            Operand right = transformValue(instr.value().get(1));
            return Ast.BinaryOperation(left, Ast.Add(), right);
        } else if (i.addInst() != null) {
            LlvmParser.AddInstContext instr = i.addInst();
            Operand left = transformValue(instr.value().get(0));
            Operand right = transformValue(instr.value().get(1));
            return Ast.BinaryOperation(left, Ast.Add(), right);
        } else if (i.addInst() != null) {
            LlvmParser.AddInstContext instr = i.addInst();
            Operand left = transformValue(instr.value().get(0));
            Operand right = transformValue(instr.value().get(1));
            return Ast.BinaryOperation(left, Ast.Add(), right);
        } else if (i.addInst() != null) {
            LlvmParser.AddInstContext instr = i.addInst();
            Operand left = transformValue(instr.value().get(0));
            Operand right = transformValue(instr.value().get(1));
            return Ast.BinaryOperation(left, Ast.Add(), right);
        } else if (i.addInst() != null) {
            LlvmParser.AddInstContext instr = i.addInst();
            Operand left = transformValue(instr.value().get(0));
            Operand right = transformValue(instr.value().get(1));
            return Ast.BinaryOperation(left, Ast.Add(), right);
        } else if (i.addInst() != null) {
            LlvmParser.AddInstContext instr = i.addInst();
            Operand left = transformValue(instr.value().get(0));
            Operand right = transformValue(instr.value().get(1));
            return Ast.BinaryOperation(left, Ast.Add(), right);
        } else if (i.addInst() != null) {
            LlvmParser.AddInstContext instr = i.addInst();
            Operand left = transformValue(instr.value().get(0));
            Operand right = transformValue(instr.value().get(1));
            return Ast.BinaryOperation(left, Ast.Add(), right);
        } else if (i.addInst() != null) {
            LlvmParser.AddInstContext instr = i.addInst();
            Operand left = transformValue(instr.value().get(0));
            Operand right = transformValue(instr.value().get(1));
            return Ast.BinaryOperation(left, Ast.Add(), right);
        } else if (i.addInst() != null) {
            LlvmParser.AddInstContext instr = i.addInst();
            Operand left = transformValue(instr.value().get(0));
            Operand right = transformValue(instr.value().get(1));
            return Ast.BinaryOperation(left, Ast.Add(), right);
        } else if (i.addInst() != null) {
            LlvmParser.AddInstContext instr = i.addInst();
            Operand left = transformValue(instr.value().get(0));
            Operand right = transformValue(instr.value().get(1));
            return Ast.BinaryOperation(left, Ast.Add(), right);
        } else if (i.addInst() != null) {
            LlvmParser.AddInstContext instr = i.addInst();
            Operand left = transformValue(instr.value().get(0));
            Operand right = transformValue(instr.value().get(1));
            return Ast.BinaryOperation(left, Ast.Add(), right);
        } else if (i.addInst() != null) {
            LlvmParser.AddInstContext instr = i.addInst();
            Operand left = transformValue(instr.value().get(0));
            Operand right = transformValue(instr.value().get(1));
            return Ast.BinaryOperation(left, Ast.Add(), right);
        } else if (i.addInst() != null) {
            LlvmParser.AddInstContext instr = i.addInst();
            Operand left = transformValue(instr.value().get(0));
            Operand right = transformValue(instr.value().get(1));
            return Ast.BinaryOperation(left, Ast.Add(), right);
        } else if (i.addInst() != null) {
            LlvmParser.AddInstContext instr = i.addInst();
            Operand left = transformValue(instr.value().get(0));
            Operand right = transformValue(instr.value().get(1));
            return Ast.BinaryOperation(left, Ast.Add(), right);
        } else if (i.addInst() != null) {
            LlvmParser.AddInstContext instr = i.addInst();
            Operand left = transformValue(instr.value().get(0));
            Operand right = transformValue(instr.value().get(1));
            return Ast.BinaryOperation(left, Ast.Add(), right);
        }

        throw new RuntimeException("todo " + str(i));
    }

    private Call transformCallInstr(LlvmParser.CallInstContext c) {
        return Ast.Call(transformValue(c.value()), transformArgs(c.args()));
    }

    private OperandList transformArgs(LlvmParser.ArgsContext context) {
        OperandList res = Ast.OperandList();
        if (context.argList() != null) {
            processArgs(context.argList(), res);
        }
        return res;
    }

    private void processArgs(LlvmParser.ArgListContext l, OperandList res) {
        if (l.argList() != null) {
            processArgs(l.argList(), res);
        }
        if (l.arg() != null) {
            res.add(transformValue(l.arg().value()));
        }
    }

    private Operand transformValue(LlvmParser.ValueContext v) {
        if (v.constant() != null) {
            return transformContant(v.constant());
        } else if (v.localIdent() != null) {
            return Ast.VarRef(lookupLocal(id(v.localIdent())));
        }
        throw new RuntimeException("todo " + str(v));
    }

    private Variable lookupLocal(String id) {
        Variable v = localVariables.get(id);
        if (v == null) {
            throw new RuntimeException("Could not find variable " + id + "\n" + localVariables);
        }
        return v;
    }

    private Operand transformContant(LlvmParser.ConstantContext c) {
        if (c.globalIdent() != null) {
            String globalName = id(c.globalIdent());
            return Ast.GlobalRef(lookupGlobal(globalName));
        } else if (c.constantExpr() != null) {
            return transformConstantExpr(c.constantExpr());
        } else if (c.intConst() != null) {
            return transformIntConst(c.intConst());
        }

        throw new RuntimeException("todo " + str(c));
    }

    private Operand transformIntConst(LlvmParser.IntConstContext i) {
        // maybe parse different lind of int literals?
        return Ast.ConstInt(Integer.parseInt(i.INT_LIT().getText()));
    }

    private Operand transformConstantExpr(LlvmParser.ConstantExprContext c) {
        throw new RuntimeException("todo " + str(c));
    }

    private GlobalDef lookupGlobal(String globalName) {
        GlobalDef res = this.globalDefs.get(globalName);
        if (res == null) {
            throw new RuntimeException("Could not find global var " + globalName);
        }
        return res;
    }

    private String id(LlvmParser.OptLabelIdentContext o) {
        if (o.labelIdent() != null) {
            String txt = o.labelIdent().getText();
            txt = txt.substring(0, txt.length() - 1);
            return txt;
        }
        for (int i = 0; i < o.getParent().getChildCount(); i++) {
            if (o.getParent().getChild(i) == o) {
                return "block" + i;
            }
        }
        return "block";
    }


    private Proc transformFunctionDecl(FunctionDeclContext f) {

        String name = id(f.functionHeader().globalIdent());
        Proc proc = (Proc) lookupGlobal(name);
        proc.setIsExtern(true);
        proc.setReturnType(transformType(f.functionHeader().type()));
        return proc;
    }

    private Global transformGlobalDef(GlobalDefContext g) {
        throw new RuntimeException("todo " + str(g));
    }

    private Global transformGobalDecl(GlobalDeclContext g) {
        Global glob = (Global) globalDefs.get(id(g.globalIdent()));
        if (g.externLinkage() != null) {
            glob.setIsConstant(g.immutable() != null);
        }
        glob.setType(transformType(g.type()));
        return glob;
//        throw new RuntimeException("todo " + str(g));
    }

    private Type transformType(LlvmParser.TypeContext t) {
        if (t.namedType() != null) {
            return Ast.TypeRef(lookupType(id(t.namedType().localIdent())));
        } else if (t.voidType() != null) {
            return Ast.TypeVoid();
        } else if (t.intType() != null) {
            // TODO distinguish i32 etc
            return Ast.TypeInt();
        } else if (t.pointertype != null) {
            return Ast.TypePointer(transformType(t.type()));
        }
        throw new RuntimeException("todo " + str(t));
    }

    private String id(LlvmParser.GlobalIdentContext g) {
        return parseId(g.getText());
    }

    private String id(LlvmParser.LocalIdentContext i) {
        return parseId(i.getText());
    }

    private String parseId(String i) {
        i = i.substring(1);
        if (i.startsWith("\"")) {
            i = i.substring(1, i.length() - 1);
        }
        return i;
    }


}
