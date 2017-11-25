package de.peeeq.wurstio.languageserver.requests;

import com.google.common.io.Files;
import de.peeeq.wurstio.gui.WurstGuiImpl;
import de.peeeq.wurstio.languageserver.ModelManager;
import de.peeeq.wurstio.languageserver.WFile;
import de.peeeq.wurstio.mpq.MpqEditor;
import de.peeeq.wurstio.mpq.MpqEditorFactory;
import de.peeeq.wurstscript.RunArgs;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.ast.CompilationUnit;
import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import net.moonlightflower.wc3libs.bin.app.W3I;
import org.eclipse.lsp4j.MessageType;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.FieldProperty;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.beans.IntrospectionException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

/**
 * Created by peter on 16.05.16.
 */
public class BuildMap extends MapRequest {
    private static final ByteBuffer MAP_NAME_MAGIC_START = ByteBuffer.wrap(new byte[]{'H', 'M', '3', 'W', 0x00, 0x00, 0x00, 0x00})
            .order(ByteOrder.LITTLE_ENDIAN);
    private static final ByteBuffer MAP_NAME_MAGIC_END = ByteBuffer.wrap(new byte[]{0x00, 'x', (byte) 0xE4, 0x01, 0x00, 0x08, 0x00, 0x00})
            .order(ByteOrder.LITTLE_ENDIAN);

    static class WurstProjectConfig {
        private static final String FILE_NAME = "wurst.build";

        public String projectName = "DefaultName";
        public List<String> dependencies = new ArrayList<>();

        public String buildMapName = "DefaultName";
        public String buildMapFileName = "DefaultFileName";
        public String buildMapAuthor = "DefaultAuthor";

        public WurstProjectConfig() {
        }

        private static final DumperOptions options = new DumperOptions();
        public static final Yaml yaml;

        private static class UnsortedPropertyUtils extends PropertyUtils {
            @Override
            protected Set<Property> createPropertySet(Class<? extends Object> type, BeanAccess bAccess) throws IntrospectionException {
                Set<Property> properties = new LinkedHashSet<>();
                Collection<Property> props = getPropertiesMap(type, bAccess).values();
                for (Property property : props) {
                    if ((property.isReadable() || property.isWritable()) && property instanceof FieldProperty) {
                        properties.add(property);
                    }
                }
                return properties;
            }
        }

        static {
            options.setTags(Collections.emptyMap());
            Representer representer = new Representer();
            representer.setPropertyUtils(new UnsortedPropertyUtils());
            representer.getPropertyUtils().setSkipMissingProperties(true);
            representer.addClassTag(WurstProjectConfig.class, Tag.MAP);
            yaml = new Yaml(new Constructor(WurstProjectConfig.class), representer, options);
        }
    }


    /**
     * makes the compilation slower, but more safe by discarding results from the editor and working on a copy of the model
     */
    private SafetyLevel safeCompilation = SafetyLevel.KindOfSafe;

    enum SafetyLevel {
        QuickAndDirty, KindOfSafe
    }

    public BuildMap(WFile workspaceRoot, File map, List<String> compileArgs) {
        super(map, compileArgs, workspaceRoot);
    }


    @Override
    public Object execute(ModelManager modelManager) throws IOException {
        if (modelManager.hasErrors()) {
            throw new RequestFailedException(MessageType.Error, "Fix errors in your code before building a release.");
        }

        File buildFile = new File(workspaceRoot.getFile(), WurstProjectConfig.FILE_NAME);
        if (!buildFile.exists()) {
            throw new RequestFailedException(MessageType.Error, WurstProjectConfig.FILE_NAME + " file doesn't exist");
        }
        String input = new String(java.nio.file.Files.readAllBytes(buildFile.toPath()));

        WurstProjectConfig wurstProjectConfig = WurstProjectConfig.yaml.loadAs(input, WurstProjectConfig.class);

        // TODO use normal compiler for this, avoid code duplication
        WLogger.info("buildMap " + map.getAbsolutePath() + " " + compileArgs);
        WurstGui gui = new WurstGuiImpl(workspaceRoot.getFile().getAbsolutePath());
        try {
            if (!map.exists()) {
                throw new RequestFailedException(MessageType.Error, map.getAbsolutePath() + " does not exist.");
            }

            gui.sendProgress("Copying map");

            // first we copy in same location to ensure validity
            File buildDir = getBuildDir();
            File targetMap = new File(buildDir, wurstProjectConfig.buildMapFileName + ".w3x");
            if (targetMap.exists()) {
                boolean deleteOk = targetMap.delete();
                if (!deleteOk) {
                    throw new RequestFailedException(MessageType.Error, "Could not delete old mapfile: " + targetMap);
                }
            }
            Files.copy(map, targetMap);

            // first compile the script:
            File compiledScript = compileScript(gui, modelManager, compileArgs, targetMap, map);

            WurstModel model = modelManager.getModel();
            if (model == null || model.stream().noneMatch((CompilationUnit cu) -> cu.getFile().endsWith("war3map.j"))) {
                println("No 'war3map.j' file could be found inside the map nor inside the wurst folder");
                println("If you compile the map with WurstPack once, this file should be in your wurst-folder. ");
                println("We will try to start the map now, but it will probably fail. ");
            }


            // then inject the script into the map
            gui.sendProgress("Injecting mapscript");
            try (MpqEditor mpqEditor = MpqEditorFactory.getEditor(targetMap)) {
                mpqEditor.deleteFile("war3map.j");
                mpqEditor.insertFile("war3map.j", Files.toByteArray(compiledScript));
            }

            gui.sendProgress("Applying Map Config...");
            applyProjectConfig(wurstProjectConfig, targetMap);
            gui.sendProgress("Done.");
        } catch (CompileError e) {
            throw new RequestFailedException(MessageType.Error, "There was an error when compiling the map: " + e.getMessage());
        } catch (RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (gui.getErrorCount() == 0) {
                gui.sendFinished();
            }
        }
        return "ok"; // TODO
    }

    private void applyProjectConfig(WurstProjectConfig wurstProjectConfig, File targetMap) throws IOException {
        if (wurstProjectConfig.buildMapFileName == null || wurstProjectConfig.buildMapFileName.isEmpty()) {
            wurstProjectConfig.buildMapFileName = targetMap.getName();
        }

        if (wurstProjectConfig.buildMapName != null && wurstProjectConfig.buildMapName.length() > 0) {
            try (MpqEditor mpq = MpqEditorFactory.getEditor((targetMap))) {
                W3I w3I = new W3I(new ByteArrayInputStream(mpq.extractFile("war3map.w3i")));
                w3I.setMapName(wurstProjectConfig.buildMapName);
                w3I.setMapAuthor(wurstProjectConfig.buildMapAuthor);
                File w3iFile = new File("w3iFile");
                w3I.write(w3iFile);

                mpq.deleteFile("war3map.w3i");
                mpq.insertFile("war3map.w3i", java.nio.file.Files.readAllBytes(w3iFile.toPath()));
                w3iFile.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try (MpqEditor mpq = MpqEditorFactory.getEditor(targetMap)) {
            mpq.setKeepHeaderOffset(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] mapBytes = java.nio.file.Files.readAllBytes(targetMap.toPath());
        ByteBuffer mapNameBuffer = ByteBuffer.allocate(512).order(ByteOrder.LITTLE_ENDIAN);
        mapNameBuffer.put(MAP_NAME_MAGIC_START);
        mapNameBuffer.put(wurstProjectConfig.buildMapName.getBytes());
        mapNameBuffer.put(MAP_NAME_MAGIC_END);

        mapNameBuffer.rewind();
        try (FileOutputStream dest = new FileOutputStream(targetMap)) {
            byte[] bytes = new byte[512 + mapBytes.length];
            mapNameBuffer.get(bytes, 0, 512);
            System.arraycopy(mapBytes, 0, bytes, 512, mapBytes.length);
            dest.write(bytes);
        }
    }

    private File compileScript(WurstGui gui, ModelManager modelManager, List<String> compileArgs, File mapCopy, File origMap) throws Exception {
        gui.sendProgress("Compiling mapscript");
        RunArgs runArgs = new RunArgs(compileArgs);
        print("Compile Script : ");
        for (File dep : modelManager.getDependencyWurstFiles()) {
            WLogger.info("dep: " + dep.getPath());
        }
        print("Dependencies done.");
        processMapScript(runArgs, gui, modelManager, mapCopy);
        print("Processed mapscript");
        if (safeCompilation != SafetyLevel.QuickAndDirty) {
            // it is safer to rebuild the project, instead of taking the current editor state
            gui.sendProgress("Cleaning project");
            modelManager.clean();
            gui.sendProgress("Building project");
            modelManager.buildProject();
        }

        if (modelManager.hasErrors()) {
            for (CompileError compileError : modelManager.getParseErrors()) {
                gui.sendError(compileError);
            }
            throw new RequestFailedException(MessageType.Warning, "Cannot run code with syntax errors.");
        }

        WurstModel model = modelManager.getModel();
        if (safeCompilation != SafetyLevel.QuickAndDirty) {
            // compilation will alter the model (e.g. remove unused imports), 
            // so it is safer to create a copy
            model = model.copy();
        }

        return compileMap(gui, mapCopy, origMap, runArgs, model);
    }

}
