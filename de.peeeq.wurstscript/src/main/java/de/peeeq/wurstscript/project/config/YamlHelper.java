package de.peeeq.wurstscript.project.config;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import kotlin.Metadata;
import kotlin.Unit;
import kotlin.io.CloseableKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import mu.KLogger;
import mu.KotlinLogging;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Metadata(mv = {1, 6, 0}, k = 1, xi = 48, d1 = {"\000.\n\002\030\002\n\002\020\000\n\002\b\002\n\002\030\002\n\000\n\002\030\002\n\000\n\002\020\016\n\000\n\002\030\002\n\002\b\002\n\002\030\002\n\002\b\002\b\002\030\0002\0020\001:\001\016B\007\b\002\006\002\020\002J\016\020\007\032\0020\b2\006\020\t\032\0020\nJ\016\020\013\032\0020\n2\006\020\f\032\0020\rR\016\020\003\032\0020\004X\004\006\002\n\000R\016\020\005\032\0020\006X\016\006\002\n\000\006\017"}, d2 = {"Lde/peeeq/wurstscript/project/config/YamlHelper;", "", "()V", "log", "Lmu/KLogger;", "mapper", "Lcom/fasterxml/jackson/databind/ObjectMapper;", "dumpProjectConfig", "", "configData", "Lde/peeeq/wurstscript/project/config/WurstProjectConfigData;", "loadProjectConfig", "path", "Ljava/nio/file/Path;", "YamlException", "wurstscript"})
public final class YamlHelper {
    @NotNull
    public static final YamlHelper INSTANCE = new YamlHelper();
    @NotNull
    private static ObjectMapper mapper;

    static {
        YAMLFactory yamlFactory = new YAMLFactory();
        yamlFactory.enable(YAMLGenerator.Feature.MINIMIZE_QUOTES);
        yamlFactory.enable(JsonParser.Feature.ALLOW_MISSING_VALUES);
        mapper = new ObjectMapper((JsonFactory) yamlFactory);
        mapper.registerModule((Module) new KotlinModule(0, false, false, false, null, false));
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @NotNull
    public final WurstProjectConfigData loadProjectConfig(@NotNull Path path) throws IOException {
        BufferedReader bufferedReader2;
        Intrinsics.checkNotNullParameter(path, "path");
        BufferedReader bufferedReader1 = Files.newBufferedReader(path);
        try (var it = Files.newBufferedReader(path)) {
            return mapper.readValue(it, WurstProjectConfigData.class);
        } catch (Exception e) {
            throw new YamlException("The project's wurst.build file could not be read. Input malformed or corrupt.");
        }
    }

    @NotNull
    public final String dumpProjectConfig(@NotNull WurstProjectConfigData configData) throws JsonProcessingException {
        Intrinsics.checkNotNullParameter(configData, "configData");
        String str = mapper.writeValueAsString(configData);
        Intrinsics.checkNotNullExpressionValue(str, "mapper.writeValueAsString(configData)");
        return str;
    }

    public static class YamlException extends RuntimeException {

        public YamlException(String s) {
            super(s);
        }
    }

}
