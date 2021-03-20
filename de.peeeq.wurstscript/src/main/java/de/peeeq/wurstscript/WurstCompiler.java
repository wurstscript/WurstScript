package de.peeeq.wurstscript;

import de.peeeq.wurstscript.ast.WurstModel;
import java.io.File;
import org.eclipse.jdt.annotation.Nullable;

public interface WurstCompiler {

  void loadFiles(String... filenames);

  @Nullable
  WurstModel parseFiles();

  void loadFiles(File... files);

  void runCompiletime();
}
