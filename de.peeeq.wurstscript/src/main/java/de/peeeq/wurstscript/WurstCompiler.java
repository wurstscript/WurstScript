package de.peeeq.wurstscript;

import config.WurstProjectConfigData;
import de.peeeq.wurstscript.ast.WurstModel;
import org.eclipse.jdt.annotation.Nullable;

import java.io.File;

public interface WurstCompiler {


    void loadFiles(String... filenames);

    @Nullable WurstModel parseFiles();

    void loadFiles(File... files);

    void runCompiletime(WurstProjectConfigData projectConfigData, boolean isProd);
}
