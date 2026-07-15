package tests.wurstscript.tests;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.testng.annotations.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.testng.Assert.*;

public class Wc3KnowledgeBaseTest {

    @Test
    public void packagedKnowledgeBaseContainsMergedObjectsAndSchemas() throws Exception {
        try (var stream = getClass().getClassLoader().getResourceAsStream("wc3-knowledge-base.json")) {
            assertNotNull(stream, "knowledge base must be packaged as a compiler resource");
            JsonObject root = JsonParser.parseReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();

            assertEquals(root.get("schemaVersion").getAsInt(), 1);
            assertTrue(root.getAsJsonArray("heroBaseIds").size() > 0);
            assertTrue(root.getAsJsonArray("buildingBaseIds").size() > 0);

            var unitFields = root.getAsJsonObject("fieldSchemas").getAsJsonArray("unit");
            boolean foundIconField = false;
            for (var field : unitFields) {
                if ("uico".equals(field.getAsJsonObject().get("id").getAsString())) {
                    foundIconField = true;
                    break;
                }
            }
            assertTrue(foundIconField);

            var hhou = root.getAsJsonObject("objects").getAsJsonObject("unit").getAsJsonObject("hhou");
            assertEquals(hhou.get("Art").getAsString(),
                "ReplaceableTextures\\CommandButtons\\BTNFarm.blp");
        }
    }
}
