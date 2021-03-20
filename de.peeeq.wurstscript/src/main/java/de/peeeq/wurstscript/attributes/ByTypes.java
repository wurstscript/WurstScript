package de.peeeq.wurstscript.attributes;

import com.google.common.collect.Lists;
import de.peeeq.wurstscript.ast.ClassDef;
import de.peeeq.wurstscript.ast.WPackage;
import java.util.List;

public class ByTypes {
  public final List<ClassDef> classes = Lists.newArrayList();
  public final List<WPackage> packageDefs = Lists.newArrayList();
}
