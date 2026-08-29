package de.peeeq.wurstio.compilationserver;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class WurstServerTests {

    @Test
    public void stoppedServerCanStartAndExitWithoutAcceptingRequests() {
        WurstServer server = new WurstServer(0);
        List<String> messages = new ArrayList<>();
        server.setPrinter(messages::add);

        server.stop();
        server.start();

        assertEquals(messages, List.of("Server started.", "Server stopped."));
    }
}
