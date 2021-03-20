package de.peeeq.wurstio.mpq;

import java.io.File;
import java.util.Optional;
import javax.annotation.Nullable;

public class MpqEditorFactory {

  public static @Nullable MpqEditor getEditor(Optional<File> f) throws Exception {
    if (!f.isPresent()) {
      return null;
    }
    return new Jmpq3BasedEditor(f.get());
  }
}
