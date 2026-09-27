package de.peeeq.wurstio;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class CompiletimeFunctionRunnerTests {

    @Test
    public void throwableWithoutMessageUsesItsType() {
        assertEquals(CompiletimeFunctionRunner.describeFailure(new StackOverflowError()), "StackOverflowError");
    }

    @Test
    public void throwableMessageIsPreserved() {
        assertEquals(CompiletimeFunctionRunner.describeFailure(new RuntimeException("details")), "details");
    }
}
