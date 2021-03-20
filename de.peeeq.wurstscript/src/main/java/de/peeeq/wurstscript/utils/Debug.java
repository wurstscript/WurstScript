package de.peeeq.wurstscript.utils;

import de.peeeq.wurstscript.WLogger;

public class Debug {

  private static final boolean DEBUG = false;

  public static void println(String string) {
    if (DEBUG) {
      WLogger.info(string);
    }
  }
}
