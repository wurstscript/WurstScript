package de.peeeq.wurstscript.attributes;

import de.peeeq.wurstscript.ast.Element;
import de.peeeq.wurstscript.utils.Utils;

public class Description {

  public static String description(Element e) {
    String html = e.descriptionHtml();
    if (html == null) {
      return null;
    }
    return Utils.stripHtml(html);
  }
}
