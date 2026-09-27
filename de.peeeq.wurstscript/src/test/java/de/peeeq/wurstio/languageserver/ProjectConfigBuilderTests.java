package de.peeeq.wurstio.languageserver;

import net.moonlightflower.wc3libs.bin.app.W3I;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.testng.Assert.assertEquals;

public class ProjectConfigBuilderTests {

    @Test
    public void pinnedPatchSelectsNewestSupportedW3iFormat() throws Exception {
        assertW3IVersion("1.30", W3I.EncodingFormat.W3I_0x19.getVersion());
        assertW3IVersion("1.31", W3I.EncodingFormat.W3I_0x1C.getVersion());
        assertW3IVersion("1.32", W3I.EncodingFormat.W3I_0x1F.getVersion());
        assertW3IVersion("2.0", W3I.EncodingFormat.W3I_0x21.getVersion());
        assertW3IVersion("3.0", W3I.EncodingFormat.W3I_0x27.getVersion());
    }

    @Test
    public void noPinnedPatchPreservesSourceW3iVersion() throws Exception {
        W3I w3i = new W3I();
        w3i.setFileVersion(W3I.EncodingFormat.W3I_0x27.getVersion());

        ProjectConfigBuilder.applyW3IVersion(WurstBuildConfig.empty(), w3i, false);

        assertEquals(w3i.getFileVersion(), W3I.EncodingFormat.W3I_0x27.getVersion());
    }

    @Test
    public void readsW3iFromSourceMapInsteadOfDowngradedCache() throws Exception {
        Path sourceMap = Files.createTempDirectory("source-map");
        Path sourceW3i = sourceMap.resolve("war3map.w3i");
        W3I original = new W3I();
        original.setFileVersion(W3I.EncodingFormat.W3I_0x27.getVersion());
        original.write(sourceW3i.toFile());

        W3I loaded = ProjectConfigBuilder.readW3I(sourceMap.toFile());
        ProjectConfigBuilder.applyW3IVersion(WurstBuildConfig.empty(), loaded, false);

        assertEquals(loaded.getFileVersion(), W3I.EncodingFormat.W3I_0x27.getVersion());
    }

    @Test
    public void luaKeepsScriptLanguageFieldInOlderSourceW3i() throws Exception {
        W3I w3i = new W3I();
        w3i.setFileVersion(W3I.EncodingFormat.W3I_0x19.getVersion());

        ProjectConfigBuilder.applyW3IVersion(WurstBuildConfig.empty(), w3i, true);

        assertEquals(w3i.getFileVersion(), W3I.EncodingFormat.W3I_0x1F.getVersion());
        assertEquals(w3i.getScriptLang(), W3I.ScriptLang.LUA);
    }

    private static void assertW3IVersion(String patch, int expected) throws Exception {
        Path project = Files.createTempDirectory("w3i-version");
        Files.writeString(project.resolve(ProjectConfigBuilder.FILE_NAME), "wc3Patch: " + patch + "\n");
        WurstBuildConfig config = WurstBuildConfig.fromWorkspaceRoot(WFile.create(project.toFile()));
        W3I w3i = new W3I();
        w3i.setFileVersion(W3I.EncodingFormat.W3I_0x27.getVersion());

        ProjectConfigBuilder.applyW3IVersion(config, w3i, false);

        assertEquals(w3i.getFileVersion(), expected, "unexpected W3I format for patch " + patch);
    }
}
