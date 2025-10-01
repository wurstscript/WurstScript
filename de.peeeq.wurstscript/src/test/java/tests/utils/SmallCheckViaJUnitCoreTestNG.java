package tests.utils;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.stream.Collectors;

public class SmallCheckViaJUnitCoreTestNG {

  @Test
  public void runGraphInterpreterTestsSC() {
    Result r = JUnitCore.runClasses(GraphInterpreterTestsSC.class);
    if (!r.wasSuccessful()) {
      String msg = r.getFailures().stream()
          .map(Failure::toString)
          .collect(Collectors.joining("\n\n"));
      Assert.fail("SmallCheck failures:\n" + msg);
    }
  }
}
