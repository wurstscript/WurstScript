package de.peeeq.wurstscript.frotty.jassValidator;

import de.peeeq.wurstscript.jassAst.*;

public class JassValidator {

  @SuppressWarnings("null")
  public static void validate(JassProg p) {
    p.accept(
        new JassProg.DefaultVisitor() {

          @Override
          public void visit(JassExprVarAccess e) {
            super.visit(e);
            e.attrVariableDefinition();
          }

          @Override
          public void visit(JassExprVarArrayAccess e) {
            super.visit(e);
            e.attrVariableDefinition();
          }

          @Override
          public void visit(JassExprFunctionCall e) {
            super.visit(e);
            e.attrFunctionCall();
          }

          @Override
          public void visit(JassExprFuncRef e) {
            super.visit(e);
            e.attrFuncRef();
          }

          @Override
          public void visit(JassStmtCall e) {
            super.visit(e);
            e.attrFunctionCall();
          }

          @Override
          public void visit(JassStmtSet e) {
            super.visit(e);
            e.attrVariableDefinition();
          }

          @Override
          public void visit(JassStmtSetArray e) {
            super.visit(e);
            e.attrVariableDefinition();
          }
        });
  }
}
