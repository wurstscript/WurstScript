package de.peeeq.wurstscript.frotty.jassValidator;

import com.google.common.collect.Lists;
import java.util.List;

public class JassErrors {

  private static final List<String> errors = Lists.newArrayList();

  public static void addError(String error, int line) {
    errors.add("Line : " + line + " - " + error);
  }

  public static List<String> getErrors() {
    return Lists.newArrayList(errors);
  }
}
