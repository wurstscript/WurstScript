package de.peeeq.wurstscript.llvm.fromllvm;

import de.peeeq.wurstscript.llvm.antlr.LlvmParser;
import de.peeeq.wurstscript.llvm.antlr.LlvmParser.FunctionDeclContext;
import de.peeeq.wurstscript.llvm.antlr.LlvmParser.FunctionDefContext;
import de.peeeq.wurstscript.llvm.antlr.LlvmParser.GlobalDeclContext;
import de.peeeq.wurstscript.llvm.antlr.LlvmParser.GlobalDefContext;
import de.peeeq.wurstscript.llvm.ast.*;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.TokenStream;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 */
public class LlvmParseTreeTransformer {

    // TODO first create these maps before starting transformation
    private Map<String, TypeDef> typeDefs = new LinkedHashMap<>();
    private Map<String, Global> globalDefs = new LinkedHashMap<>();

    public Prog transformModule(LlvmParser.ModuleContext module) {
        TypeDefList typeDefs = Ast.TypeDefList();
        GlobalList globals = Ast.GlobalList();
        ProcList procedures = Ast.ProcList();

        for (LlvmParser.TopLevelEntityContext tle : module.topLevelEntity()) {
            if (tle.globalDecl() != null) {
                Global g = transformGobalDecl(tle.globalDecl());
                this.globalDefs.put(g.getName(), g);
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
        if (t.opaqueType() != null) {
            return Ast.TypeOpaque(id(t.localIdent()));
        }
        throw new RuntimeException("todo " + str(t));
    }



    private String str(RuleContext tle) {
        return tle.toStringTree(Arrays.asList(LlvmParser.ruleNames));
    }

    private Proc transformFunctionDef(FunctionDefContext f) {
        String name = id(f.functionHeader().globalIdent());
        boolean isExtern = false;
        Type returnType = transformType(f.functionHeader().type());
        ParameterList parameters = Ast.ParameterList();
        BasicBlockList basicBlocks = transformFunctionBody(f.functionBody());

        return Ast.Proc(name, isExtern, returnType, parameters, basicBlocks);
    }

    private BasicBlockList transformFunctionBody(LlvmParser.FunctionBodyContext b) {
        BasicBlockList blocks = Ast.BasicBlockList();
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
            return Ast.Assign(Ast.TemporaryVar(id(ins.localIdent())),
                    transformValueInstruction(ins.valueInstruction()));

        }

        return Ast.CommentInstr("todo " + str(ins));
//        throw new RuntimeException("todo " + str(ins));
    }

    private ValueInstruction transformValueInstruction(LlvmParser.ValueInstructionContext i) {
        if (i.callInst() != null) {
            return transformCallInstr(i.callInst());
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
        }
        throw new RuntimeException("todo " + str(v));
    }

    private Operand transformContant(LlvmParser.ConstantContext c) {
        if (c.globalIdent() != null) {
            String globalName = id(c.globalIdent());
            return Ast.GlobalRef(lookupGlobal(globalName));
        }

        throw new RuntimeException("todo " + str(c));
    }

    private Global lookupGlobal(String globalName) {
        Global res = this.globalDefs.get(globalName);
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
        boolean isExtern = true;
        Type returnType = transformType(f.functionHeader().type());
        ParameterList parameters = Ast.ParameterList();
        BasicBlockList basicBlocks = Ast.BasicBlockList();
        return Ast.Proc(name, isExtern, returnType, parameters, basicBlocks);
    }

    private Global transformGlobalDef(GlobalDefContext g) {
        throw new RuntimeException("todo " + str(g));
    }

    private Global transformGobalDecl(GlobalDeclContext g) {
        if (g.externLinkage() != null) {
            return Ast.Global(transformType(g.type()), id(g.globalIdent()), g.immutable() != null, Ast.External());
        }
        throw new RuntimeException("todo " + str(g));
    }

    private Type transformType(LlvmParser.TypeContext t) {
        if (t.namedType() != null) {
            return Ast.TypeRef(typeDefs.get(id(t.namedType().localIdent())));
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
