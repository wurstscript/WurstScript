package tests.prettyprint;

import de.peeeq.wurstscript.ast.WurstModel;
import de.peeeq.wurstscript.attributes.prettyPrint.MaxOneSpacer;
import de.peeeq.wurstscript.attributes.prettyPrint.PrettyPrinter;
import org.testng.annotations.Test;
import tests.wurstscript.tests.WurstScriptTest;

public class PrettyPrintTest extends WurstScriptTest {

  @Test
  public void testPrettyPrint() {
    String input =
        "package Test\n"
            + "constant     int   i =  0\n"
            + "int   i2=0\n"
            + "int i3\n"
            + "constant i4 = 5\n"
            + "public constant r = 23.524362\n"
            + "\n"
            + "enum E\n"
            + "\tSOME_VAL\n"
            + "class Test\n"
            + "\tprivate string s\n"
            + "\tconstruct(string name)\n"
            + "\t\tthis. s =name\n"
            + "\tfunction foo(boolean bar) returns boolean\n"
            + "\t\t\treturn not bar\n"
            + "class Boo extends Test\n"
            + "\tconstruct()\n"
            + "\t\tsuper(\"boo\")\n"
            + "\tfunction doIt()\n"
            + "\t\tlet tst = new Test(\"fork\") castTo Boo";
    WurstModel model = testScript("pretty", false, input);
    StringBuilder stringBuilder = new StringBuilder();
    PrettyPrinter.prettyPrint(model, new MaxOneSpacer(), stringBuilder, 0);
    System.out.println("Pretty:\n\n" + stringBuilder.toString());

    testAssertOk("prettyTest", false, stringBuilder.toString());
  }
}
