package de.peeeq.wurstio;

import java.io.File;
import java.util.Arrays;

public class UtilsIO {

    public static void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    public static String getTestName() {
        StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        for (int i = 0; i < ste.length; i++) {
            if (ste[i].getClassName().equals("sun.reflect.NativeMethodAccessorImpl")) {
                return ste[i-1].getMethodName();
            }
        }
        throw new RuntimeException("Could not get test name");
    }


    public static void mkdirs(File dir) {
        boolean res = dir.mkdirs();
        if (!res) {
            throw new RuntimeException("Directory " + dir + " could not be created.");
        }
    }
}
