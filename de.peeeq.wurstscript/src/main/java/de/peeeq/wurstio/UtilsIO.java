package de.peeeq.wurstio;

public class UtilsIO {

    public static void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    /**
     * Get the method name for a depth in call stack. <br />
     * Utility function
     *
     * @param depth depth in the call stack (0 means current method, 1 means call
     *              method, ...)
     * @return method name
     */
    public static String getMethodName(final int depth) {
        StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        return ste[depth + 2].getMethodName();
    }


}
