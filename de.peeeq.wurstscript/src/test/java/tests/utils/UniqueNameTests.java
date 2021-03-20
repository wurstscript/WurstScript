package tests.utils;

import static org.testng.AssertJUnit.assertEquals;

import de.peeeq.wurstscript.utils.Utils;
import org.testng.annotations.Test;

/** */
public class UniqueNameTests {

  @Test
  public void test() {
    String a = Utils.makeUniqueName("blub", s -> s.length() > 8);
    assertEquals("blub_1000", a);
  }
}
