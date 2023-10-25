package de.peeeq.wurstscript;


public class ErrorReporting {

    public static ErrorReporting instance = new ErrorReporting();


    public void handleSevere(Throwable t, String sourcecode) {
        System.err.println(t.toString());
        WLogger.severe(t);
    }

    public boolean sendErrorReport(Throwable t, String sourcecode) {
        return false;
    }

}
