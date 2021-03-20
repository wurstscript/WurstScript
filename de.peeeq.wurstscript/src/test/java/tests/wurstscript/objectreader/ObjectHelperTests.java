package tests.wurstscript.objectreader;

import static org.testng.Assert.assertEquals;

import de.peeeq.wurstio.objectreader.ObjectHelper;
import org.testng.annotations.Test;

public class ObjectHelperTests {

  @Test
  public void example1a() {
    assertEquals(ObjectHelper.objectIdStringToInt("Hfoo"), 1214672751);
  }

  @Test
  public void example2() {
    assertEquals(ObjectHelper.objectIdIntToString(1214672751), "Hfoo");
  }

  @Test
  public void ver1() {
    verify1("Abcd");
    verify1("Dcba");
  }

  private void verify1(String id) {
    assertEquals(id, ObjectHelper.objectIdIntToString(ObjectHelper.objectIdStringToInt(id)));
  }
}
