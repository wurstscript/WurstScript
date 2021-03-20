package de.peeeq.wurstscript.attributes.prettyPrint;

public class DefaultSpacer implements Spacer {

  @Override
  public void addSpace(StringBuilder sb) {
    sb.append(" ");
  }
}
