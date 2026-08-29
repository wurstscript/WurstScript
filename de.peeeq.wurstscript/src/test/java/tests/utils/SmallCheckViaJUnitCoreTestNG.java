package tests.utils;

import tests.wurstscript.tests.CompilerFuzzTestsSC;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.stream.Collectors;

public class SmallCheckViaJUnitCoreTestNG {

  @Test
  public void runSmallCheckSuite() {
    Result r = JUnitCore.runClasses(GraphInterpreterTestsSC.class);
    assertNoFailures(r, "GraphInterpreter");

    Result compilerFuzz = JUnitCore.runClasses(CompilerFuzzTestsSC.class);
    assertNoFailures(compilerFuzz, "CompilerFuzz");
  }

  private void assertNoFailures(Result r, String suiteName) {
    if (!r.wasSuccessful()) {
      String msg = r.getFailures().stream()
          .map(Failure::getTrace)
          .collect(Collectors.joining("\n\n"));
      Assert.fail("SmallCheck failures (" + suiteName + "):\n" + msg);
    }
  }
}
