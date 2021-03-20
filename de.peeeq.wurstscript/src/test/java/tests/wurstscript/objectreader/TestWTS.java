package tests.wurstscript.objectreader;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import net.moonlightflower.wc3libs.txt.WTS;
import org.testng.annotations.Test;

public class TestWTS {

  @Test
  public void testWTS() throws IOException {
    File wts = new File("testscripts/mpq/war3map.wts");
    WTS result = new WTS(wts);
    assertEquals("Player 1", result.getEntry(1));
    assertEquals("Force 1", result.getEntry(2));
    assertEquals("BlubLol", result.getEntry(9));
  }
}
