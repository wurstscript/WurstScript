package de.peeeq.pscript.generator;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.peeeq.pscript.PscriptRuntimeModule;
import de.peeeq.pscript.intermediateLang.ILStatement;
import de.peeeq.pscript.intermediateLang.ILconst;
import de.peeeq.pscript.intermediateLang.ILexitwhen;
import de.peeeq.pscript.intermediateLang.ILfunction;
import de.peeeq.pscript.intermediateLang.ILfunctionCall;
import de.peeeq.pscript.intermediateLang.ILif;
import de.peeeq.pscript.intermediateLang.ILloop;
import de.peeeq.pscript.intermediateLang.ILprog;
import de.peeeq.pscript.intermediateLang.ILreturn;
import de.peeeq.pscript.intermediateLang.ILsetBinary;
import de.peeeq.pscript.intermediateLang.ILsetVar;
import de.peeeq.pscript.intermediateLang.ILvar;
import de.peeeq.pscript.intermediateLang.IlbuildinFunctionCall;
import de.peeeq.pscript.intermediateLang.Iloperator;
import de.peeeq.pscript.intermediateLang.IlsetConst;
import de.peeeq.pscript.intermediateLang.IlsetUnary;
import de.peeeq.pscript.intermediateLang.IntermediateCodeGenerator;
import de.peeeq.pscript.types.PscriptType;
import java.util.List;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.generator.IFileSystemAccess;
import org.eclipse.xtext.generator.IGenerator;
import org.eclipse.xtext.xbase.lib.BooleanExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xtend2.lib.StringConcatenation;

@SuppressWarnings("all")
public class PscriptGenerator implements IGenerator {
  
  public void doGenerate(final Resource resource, final IFileSystemAccess fsa) {
    {
      PscriptRuntimeModule _pscriptRuntimeModule = new PscriptRuntimeModule();
      Injector _createInjector = Guice.createInjector(_pscriptRuntimeModule);
      final Injector injector = _createInjector;
      IntermediateCodeGenerator _instance = injector.<IntermediateCodeGenerator>getInstance(de.peeeq.pscript.intermediateLang.IntermediateCodeGenerator.class);
      final IntermediateCodeGenerator iLconverter = _instance;
      ILprog _translateProg = iLconverter.translateProg(resource);
      final ILprog iLprog = _translateProg;
      StringConcatenation _printProg = this.printProg(iLprog);
      fsa.generateFile("output.j", _printProg);
    }
  }
  
  public StringConcatenation printProg(final ILprog prog) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("globals");
    _builder.newLine();
    {
      List<ILvar> _globals = prog.getGlobals();
      for(ILvar g : _globals) {
        StringConcatenation _printGlobal = this.printGlobal(g, prog);
        _builder.append(_printGlobal, "");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.newLine();
    _builder.append("endglobals");
    _builder.newLine();
    {
      List<ILfunction> _functions = prog.getFunctions();
      for(ILfunction f : _functions) {
        StringConcatenation _printFunction = this.printFunction(f, prog);
        _builder.append(_printFunction, "");
        _builder.newLineIfNotEmpty();
      }
    }
    return _builder;
  }
  
  public StringConcatenation printGlobal(final ILvar g, final ILprog prog) {
    StringConcatenation _builder = new StringConcatenation();
    PscriptType _type = g.getType();
    StringConcatenation _printType = this.printType(_type, prog);
    _builder.append(_printType, "");
    _builder.append(" ");
    String _name = g.getName();
    _builder.append(_name, "");
    _builder.append("\t");
    _builder.newLineIfNotEmpty();
    return _builder;
  }
  
  public StringConcatenation printType(final PscriptType type, final ILprog prog) {
    StringConcatenation _xifexpression = null;
    boolean _operator_equals = ObjectExtensions.operator_equals(type, null);
    if (_operator_equals) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("nothing");
      _xifexpression = _builder;
    } else {
      StringConcatenation _xifexpression_1 = null;
      if ((type instanceof de.peeeq.pscript.types.PScriptTypeVoid)) {
        StringConcatenation _builder_1 = new StringConcatenation();
        _builder_1.append("nothing");
        _xifexpression_1 = _builder_1;
      } else {
        StringConcatenation _builder_2 = new StringConcatenation();
        String _lookupNativeTranslation = prog.lookupNativeTranslation(type);
        _builder_2.append(_lookupNativeTranslation, "");
        _xifexpression_1 = _builder_2;
      }
      _xifexpression = _xifexpression_1;
    }
    return _xifexpression;
  }
  
  public StringConcatenation printFunction(final ILfunction f, final ILprog prog) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("function ");
    String _name = f.getName();
    _builder.append(_name, "");
    _builder.append(" takes ");
    StringConcatenation _printParams = this.printParams(f, prog);
    _builder.append(_printParams, "");
    _builder.append(" returns ");
    PscriptType _returnType = f.getReturnType();
    StringConcatenation _printType = this.printType(_returnType, prog);
    _builder.append(_printType, "");
    _builder.newLineIfNotEmpty();
    {
      List<ILvar> _locals = f.getLocals();
      for(ILvar l : _locals) {
        _builder.append("\t");
        _builder.append("local ");
        PscriptType _type = l.getType();
        StringConcatenation _printType_1 = this.printType(_type, prog);
        _builder.append(_printType_1, "	");
        _builder.append(" ");
        String _name_1 = l.getName();
        _builder.append(_name_1, "	");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.append("\t");
    List<ILStatement> _body = f.getBody();
    StringConcatenation _printStatements = this.printStatements(_body, prog);
    _builder.append(_printStatements, "	");
    _builder.newLineIfNotEmpty();
    _builder.append("endfunction");
    _builder.newLine();
    return _builder;
  }
  
  public StringConcatenation printParams(final ILfunction f, final ILprog prog) {
    StringConcatenation _xifexpression = null;
    boolean _operator_or = false;
    List<ILvar> _params = f.getParams();
    boolean _operator_equals = ObjectExtensions.operator_equals(_params, null);
    if (_operator_equals) {
      _operator_or = true;
    } else {
      List<ILvar> _params_1 = f.getParams();
      int _size = _params_1.size();
      boolean _operator_equals_1 = ObjectExtensions.operator_equals(((Integer)_size), ((Integer)0));
      _operator_or = BooleanExtensions.operator_or(_operator_equals, _operator_equals_1);
    }
    if (_operator_or) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("nothing");
      _xifexpression = _builder;
    } else {
      List<ILvar> _params_2 = f.getParams();
      StringConcatenation _printParams = this.printParams(_params_2, prog);
      _xifexpression = _printParams;
    }
    return _xifexpression;
  }
  
  public StringConcatenation printParams(final List<ILvar> params, final ILprog prog) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("\t");
    {
      boolean hasAnyElements = false;
      for(ILvar p : params) {
        if (!hasAnyElements) {
          hasAnyElements = true;
        } else {
          _builder.appendImmediate(", ", "	");
        }
        PscriptType _type = p.getType();
        StringConcatenation _printType = this.printType(_type, prog);
        _builder.append(_printType, "	");
        _builder.append(" ");
        String _name = p.getName();
        _builder.append(_name, "	");
      }
    }
    _builder.append(" ");
    return _builder;
  }
  
  public StringConcatenation printStatements(final List<ILStatement> statements, final ILprog prog) {
    StringConcatenation _builder = new StringConcatenation();
    {
      for(ILStatement s : statements) {
        StringConcatenation _printStatement = this.printStatement(s, prog);
        _builder.append(_printStatement, "");
        _builder.newLineIfNotEmpty();
      }
    }
    return _builder;
  }
  
  protected StringConcatenation _printStatement(final ILStatement s, final ILprog prog) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("// not implemented: ");
    _builder.append(s, "");
    _builder.newLineIfNotEmpty();
    return _builder;
  }
  
  protected StringConcatenation _printStatement(final ILsetBinary s, final ILprog prog) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("set ");
    ILvar _resultVar = s.getResultVar();
    String _name = _resultVar.getName();
    _builder.append(_name, "");
    _builder.append(" = ");
    ILvar _left = s.getLeft();
    String _name_1 = _left.getName();
    _builder.append(_name_1, "");
    _builder.append(" ");
    Iloperator _op = s.getOp();
    StringConcatenation _printOp = this.printOp(_op);
    _builder.append(_printOp, "");
    _builder.append(" ");
    ILvar _right = s.getRight();
    String _name_2 = _right.getName();
    _builder.append(_name_2, "");
    _builder.newLineIfNotEmpty();
    return _builder;
  }
  
  protected StringConcatenation _printStatement(final IlbuildinFunctionCall s, final ILprog prog) {
    StringConcatenation _xifexpression = null;
    ILvar _resultVar = s.getResultVar();
    boolean _operator_equals = ObjectExtensions.operator_equals(_resultVar, null);
    if (_operator_equals) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("call ");
      String _funcName = s.getFuncName();
      _builder.append(_funcName, "");
      _builder.append("(");
      List<ILvar> _args = s.getArgs();
      StringConcatenation _printArguments = this.printArguments(_args, prog);
      _builder.append(_printArguments, "");
      _builder.append(")");
      _builder.newLineIfNotEmpty();
      _xifexpression = _builder;
    } else {
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("set ");
      ILvar _resultVar_1 = s.getResultVar();
      String _name = _resultVar_1.getName();
      _builder_1.append(_name, "");
      _builder_1.append(" = ");
      String _funcName_1 = s.getFuncName();
      _builder_1.append(_funcName_1, "");
      _builder_1.append("(");
      List<ILvar> _args_1 = s.getArgs();
      StringConcatenation _printArguments_1 = this.printArguments(_args_1, prog);
      _builder_1.append(_printArguments_1, "");
      _builder_1.append(")");
      _builder_1.newLineIfNotEmpty();
      _xifexpression = _builder_1;
    }
    return _xifexpression;
  }
  
  public StringConcatenation printArguments(final List<ILvar> args, final ILprog prog) {
    StringConcatenation _builder = new StringConcatenation();
    {
      boolean hasAnyElements = false;
      for(ILvar a : args) {
        if (!hasAnyElements) {
          hasAnyElements = true;
        } else {
          _builder.appendImmediate(", ", "");
        }
        String _name = a.getName();
        _builder.append(_name, "");
      }
    }
    return _builder;
  }
  
  protected StringConcatenation _printStatement(final ILsetVar s, final ILprog prog) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("set ");
    ILvar _resultVar = s.getResultVar();
    String _name = _resultVar.getName();
    _builder.append(_name, "");
    _builder.append(" = ");
    ILvar _var = s.getVar();
    String _name_1 = _var.getName();
    _builder.append(_name_1, "");
    _builder.newLineIfNotEmpty();
    return _builder;
  }
  
  protected StringConcatenation _printStatement(final ILexitwhen s, final ILprog prog) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("exitwhen ");
    ILvar _var = s.getVar();
    String _name = _var.getName();
    _builder.append(_name, "");
    _builder.newLineIfNotEmpty();
    return _builder;
  }
  
  protected StringConcatenation _printStatement(final ILfunctionCall s, final ILprog prog) {
    StringConcatenation _xifexpression = null;
    ILvar _resultVar = s.getResultVar();
    boolean _operator_equals = ObjectExtensions.operator_equals(_resultVar, null);
    if (_operator_equals) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("call ");
      String _name = s.getName();
      _builder.append(_name, "");
      _builder.append("(");
      List<ILvar> _args = s.getArgs();
      StringConcatenation _printArguments = this.printArguments(_args, prog);
      _builder.append(_printArguments, "");
      _builder.append(")");
      _builder.newLineIfNotEmpty();
      _xifexpression = _builder;
    } else {
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("set ");
      ILvar _resultVar_1 = s.getResultVar();
      String _name_1 = _resultVar_1.getName();
      _builder_1.append(_name_1, "");
      _builder_1.append(" = ");
      String _name_2 = s.getName();
      _builder_1.append(_name_2, "");
      _builder_1.append("(");
      List<ILvar> _args_1 = s.getArgs();
      StringConcatenation _printArguments_1 = this.printArguments(_args_1, prog);
      _builder_1.append(_printArguments_1, "");
      _builder_1.append(")");
      _builder_1.newLineIfNotEmpty();
      _xifexpression = _builder_1;
    }
    return _xifexpression;
  }
  
  protected StringConcatenation _printStatement(final ILif s, final ILprog prog) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("if ");
    ILvar _cond = s.getCond();
    String _name = _cond.getName();
    _builder.append(_name, "");
    _builder.append(" then");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    List<ILStatement> _thenBlock = s.getThenBlock();
    StringConcatenation _printStatements = this.printStatements(_thenBlock, prog);
    _builder.append(_printStatements, "	");
    _builder.newLineIfNotEmpty();
    _builder.append("else");
    _builder.newLine();
    _builder.append("\t");
    List<ILStatement> _elseBlock = s.getElseBlock();
    StringConcatenation _printStatements_1 = this.printStatements(_elseBlock, prog);
    _builder.append(_printStatements_1, "	");
    _builder.newLineIfNotEmpty();
    _builder.append("endif");
    _builder.newLine();
    return _builder;
  }
  
  protected StringConcatenation _printStatement(final ILloop s, final ILprog prog) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("loop");
    _builder.newLine();
    _builder.append("\t");
    List<ILStatement> _loopBody = s.getLoopBody();
    StringConcatenation _printStatements = this.printStatements(_loopBody, prog);
    _builder.append(_printStatements, "	");
    _builder.newLineIfNotEmpty();
    _builder.append("endloop");
    _builder.newLine();
    return _builder;
  }
  
  protected StringConcatenation _printStatement(final ILreturn s, final ILprog prog) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("return ");
    String _xifexpression = null;
    ILvar _var = s.getVar();
    boolean _operator_notEquals = ObjectExtensions.operator_notEquals(_var, null);
    if (_operator_notEquals) {
      ILvar _var_1 = s.getVar();
      String _name = _var_1.getName();
      _xifexpression = _name;
    }
    _builder.append(_xifexpression, "");
    _builder.newLineIfNotEmpty();
    return _builder;
  }
  
  protected StringConcatenation _printStatement(final IlsetConst s, final ILprog prog) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("set ");
    ILvar _resultVar = s.getResultVar();
    String _name = _resultVar.getName();
    _builder.append(_name, "");
    _builder.append(" = ");
    ILconst _constant = s.getConstant();
    String _translateConstant = this.translateConstant(_constant);
    _builder.append(_translateConstant, "");
    _builder.newLineIfNotEmpty();
    return _builder;
  }
  
  public String translateConstant(final ILconst c) {
    String _print = c.print();
    return _print;
  }
  
  protected StringConcatenation _printStatement(final IlsetUnary s, final ILprog prog) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("set ");
    ILvar _resultVar = s.getResultVar();
    String _name = _resultVar.getName();
    _builder.append(_name, "");
    _builder.append(" = ");
    Iloperator _op = s.getOp();
    StringConcatenation _printOp = this.printOp(_op);
    _builder.append(_printOp, "");
    _builder.append("  ");
    ILvar _right = s.getRight();
    String _name_1 = _right.getName();
    _builder.append(_name_1, "");
    _builder.newLineIfNotEmpty();
    return _builder;
  }
  
  public StringConcatenation printOp(final Iloperator op) {
    StringConcatenation _xifexpression = null;
    boolean _operator_equals = ObjectExtensions.operator_equals(op, Iloperator.OR);
    if (_operator_equals) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("or");
      _xifexpression = _builder;
    } else {
      StringConcatenation _xifexpression_1 = null;
      boolean _operator_equals_1 = ObjectExtensions.operator_equals(op, Iloperator.AND);
      if (_operator_equals_1) {
        StringConcatenation _builder_1 = new StringConcatenation();
        _builder_1.append("and");
        _xifexpression_1 = _builder_1;
      } else {
        StringConcatenation _xifexpression_2 = null;
        boolean _operator_equals_2 = ObjectExtensions.operator_equals(op, Iloperator.NOT);
        if (_operator_equals_2) {
          StringConcatenation _builder_2 = new StringConcatenation();
          _builder_2.append("not");
          _xifexpression_2 = _builder_2;
        } else {
          StringConcatenation _xifexpression_3 = null;
          boolean _operator_equals_3 = ObjectExtensions.operator_equals(op, Iloperator.PLUS);
          if (_operator_equals_3) {
            StringConcatenation _builder_3 = new StringConcatenation();
            _builder_3.append("+");
            _xifexpression_3 = _builder_3;
          } else {
            StringConcatenation _xifexpression_4 = null;
            boolean _operator_equals_4 = ObjectExtensions.operator_equals(op, Iloperator.MINUS);
            if (_operator_equals_4) {
              StringConcatenation _builder_4 = new StringConcatenation();
              _builder_4.append("-");
              _xifexpression_4 = _builder_4;
            } else {
              StringConcatenation _xifexpression_5 = null;
              boolean _operator_equals_5 = ObjectExtensions.operator_equals(op, Iloperator.MULT);
              if (_operator_equals_5) {
                StringConcatenation _builder_5 = new StringConcatenation();
                _builder_5.append("*");
                _xifexpression_5 = _builder_5;
              } else {
                StringConcatenation _xifexpression_6 = null;
                boolean _operator_equals_6 = ObjectExtensions.operator_equals(op, Iloperator.DIV_INT);
                if (_operator_equals_6) {
                  StringConcatenation _builder_6 = new StringConcatenation();
                  _builder_6.append("/");
                  _xifexpression_6 = _builder_6;
                } else {
                  StringConcatenation _xifexpression_7 = null;
                  boolean _operator_equals_7 = ObjectExtensions.operator_equals(op, Iloperator.DIV_REAL);
                  if (_operator_equals_7) {
                    StringConcatenation _builder_7 = new StringConcatenation();
                    _builder_7.append("/");
                    _xifexpression_7 = _builder_7;
                  } else {
                    StringConcatenation _xifexpression_8 = null;
                    boolean _operator_equals_8 = ObjectExtensions.operator_equals(op, Iloperator.MOD_INT);
                    if (_operator_equals_8) {
                      StringConcatenation _builder_8 = new StringConcatenation();
                      _builder_8.append("MOD");
                      _xifexpression_8 = _builder_8;
                    } else {
                      StringConcatenation _xifexpression_9 = null;
                      boolean _operator_equals_9 = ObjectExtensions.operator_equals(op, Iloperator.MOD_REAL);
                      if (_operator_equals_9) {
                        StringConcatenation _builder_9 = new StringConcatenation();
                        _builder_9.append("MOD");
                        _xifexpression_9 = _builder_9;
                      } else {
                        StringConcatenation _xifexpression_10 = null;
                        boolean _operator_equals_10 = ObjectExtensions.operator_equals(op, Iloperator.EQUALITY);
                        if (_operator_equals_10) {
                          StringConcatenation _builder_10 = new StringConcatenation();
                          _builder_10.append("==");
                          _xifexpression_10 = _builder_10;
                        } else {
                          StringConcatenation _xifexpression_11 = null;
                          boolean _operator_equals_11 = ObjectExtensions.operator_equals(op, Iloperator.UNEQUALITY);
                          if (_operator_equals_11) {
                            StringConcatenation _builder_11 = new StringConcatenation();
                            _builder_11.append("!=");
                            _xifexpression_11 = _builder_11;
                          } else {
                            StringConcatenation _xifexpression_12 = null;
                            boolean _operator_equals_12 = ObjectExtensions.operator_equals(op, Iloperator.LESS);
                            if (_operator_equals_12) {
                              StringConcatenation _builder_12 = new StringConcatenation();
                              _builder_12.append("<");
                              _xifexpression_12 = _builder_12;
                            } else {
                              StringConcatenation _xifexpression_13 = null;
                              boolean _operator_equals_13 = ObjectExtensions.operator_equals(op, Iloperator.LESS_EQ);
                              if (_operator_equals_13) {
                                StringConcatenation _builder_13 = new StringConcatenation();
                                _builder_13.append("<=");
                                _xifexpression_13 = _builder_13;
                              } else {
                                StringConcatenation _xifexpression_14 = null;
                                boolean _operator_equals_14 = ObjectExtensions.operator_equals(op, Iloperator.GREATER);
                                if (_operator_equals_14) {
                                  StringConcatenation _builder_14 = new StringConcatenation();
                                  _builder_14.append(">");
                                  _xifexpression_14 = _builder_14;
                                } else {
                                  StringConcatenation _xifexpression_15 = null;
                                  boolean _operator_equals_15 = ObjectExtensions.operator_equals(op, Iloperator.GREATER_EQ);
                                  if (_operator_equals_15) {
                                    StringConcatenation _builder_15 = new StringConcatenation();
                                    _builder_15.append(">=");
                                    _xifexpression_15 = _builder_15;
                                  } else {
                                    Error _error = new Error("unknown operator");
                                    throw _error;
                                  }
                                  _xifexpression_14 = _xifexpression_15;
                                }
                                _xifexpression_13 = _xifexpression_14;
                              }
                              _xifexpression_12 = _xifexpression_13;
                            }
                            _xifexpression_11 = _xifexpression_12;
                          }
                          _xifexpression_10 = _xifexpression_11;
                        }
                        _xifexpression_9 = _xifexpression_10;
                      }
                      _xifexpression_8 = _xifexpression_9;
                    }
                    _xifexpression_7 = _xifexpression_8;
                  }
                  _xifexpression_6 = _xifexpression_7;
                }
                _xifexpression_5 = _xifexpression_6;
              }
              _xifexpression_4 = _xifexpression_5;
            }
            _xifexpression_3 = _xifexpression_4;
          }
          _xifexpression_2 = _xifexpression_3;
        }
        _xifexpression_1 = _xifexpression_2;
      }
      _xifexpression = _xifexpression_1;
    }
    return _xifexpression;
  }
  
  public StringConcatenation printStatement(final ILStatement s, final ILprog prog) {
    if ((s instanceof ILfunctionCall)
         && (prog instanceof ILprog)) {
      return _printStatement((ILfunctionCall)s, (ILprog)prog);
    } else if ((s instanceof ILsetBinary)
         && (prog instanceof ILprog)) {
      return _printStatement((ILsetBinary)s, (ILprog)prog);
    } else if ((s instanceof ILsetVar)
         && (prog instanceof ILprog)) {
      return _printStatement((ILsetVar)s, (ILprog)prog);
    } else if ((s instanceof IlbuildinFunctionCall)
         && (prog instanceof ILprog)) {
      return _printStatement((IlbuildinFunctionCall)s, (ILprog)prog);
    } else if ((s instanceof IlsetConst)
         && (prog instanceof ILprog)) {
      return _printStatement((IlsetConst)s, (ILprog)prog);
    } else if ((s instanceof IlsetUnary)
         && (prog instanceof ILprog)) {
      return _printStatement((IlsetUnary)s, (ILprog)prog);
    } else if ((s instanceof ILexitwhen)
         && (prog instanceof ILprog)) {
      return _printStatement((ILexitwhen)s, (ILprog)prog);
    } else if ((s instanceof ILif)
         && (prog instanceof ILprog)) {
      return _printStatement((ILif)s, (ILprog)prog);
    } else if ((s instanceof ILloop)
         && (prog instanceof ILprog)) {
      return _printStatement((ILloop)s, (ILprog)prog);
    } else if ((s instanceof ILreturn)
         && (prog instanceof ILprog)) {
      return _printStatement((ILreturn)s, (ILprog)prog);
    } else if ((s instanceof ILStatement)
         && (prog instanceof ILprog)) {
      return _printStatement((ILStatement)s, (ILprog)prog);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        java.util.Arrays.<Object>asList(s, prog).toString());
    }
  }
}