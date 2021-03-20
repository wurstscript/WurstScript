package de.peeeq.wurstscript.frotty.jassAttributes;

import de.peeeq.wurstscript.frotty.jassValidator.JassErrors;
import de.peeeq.wurstscript.jassAst.*;
import org.eclipse.jdt.annotation.Nullable;

public class FunctionCall {

  public static @Nullable JassFunction get(JassExprFunctionCall f) {
    String funcName = f.getFuncName();
    Element node = f.getParent();
    while (node != null) {
      if (node instanceof JassProgs) {
        JassProgs jassProgs = (JassProgs) node;
        JassFunction v = jassProgs.getFunction(funcName);
        if (v != null) {
          return v;
        }
      }
      node = node.getParent();
    }
    JassErrors.addError("Could not find function '" + funcName + "'.", f.getLine());
    return null;
  }

  public static @Nullable JassFunction get(JassStmtCall f) {
    String funcName = f.getFuncName();
    Element node = f.getParent();
    while (node != null) {
      if (node instanceof JassProgs) {
        JassProgs jassProgs = (JassProgs) node;
        JassFunction v = jassProgs.getFunction(funcName);
        if (v != null) {
          return v;
        }
      }
      node = node.getParent();
    }
    JassErrors.addError("Could not find function '" + funcName + "'.", f.getLine());
    return null;
  }
}
