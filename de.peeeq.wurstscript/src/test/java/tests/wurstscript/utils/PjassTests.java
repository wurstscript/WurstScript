package tests.wurstscript.utils;

import de.peeeq.wurstio.Pjass;
import de.peeeq.wurstio.Pjass.Result;
import de.peeeq.wurstio.gui.WurstGuiImpl;
import de.peeeq.wurstscript.attributes.CompileError;
import org.junit.Test;

import java.io.File;

public class PjassTests {

    @Test
    public void test() {
        Result result = Pjass.runPjass(new File("./testscripts/invalid/fail.j"));
        System.out.println(result.getMessage());


        WurstGuiImpl gui = new WurstGuiImpl();

        for (CompileError err : result.getErrors()) {
            System.out.println(err);
            System.out.println(err.getSource().getLeftPos());
            gui.sendError(err);
        }

    }

    public static void main(String[] args) {

        WurstGuiImpl gui = new WurstGuiImpl();
        Result result = Pjass.runPjass(new File("./testscripts/invalid/fail.j"));
        System.out.println(result.getMessage());


        for (CompileError err : result.getErrors()) {
            System.out.println(err);
            System.out.println(err.getSource().getLeftPos());
            gui.sendError(err);
        }
        gui.sendFinished();
    }

}
