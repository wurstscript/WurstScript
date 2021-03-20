package de.peeeq.wurstio.jassinterpreter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Implements {
  String[] funcNames();
}
