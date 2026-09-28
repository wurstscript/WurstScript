package de.peeeq.wurstscript.intermediatelang.interpreter;

import de.peeeq.wurstscript.intermediatelang.ILconst;

import java.io.PrintStream;

public interface NativesProvider extends AutoCloseable {

    ILconst invoke(String funcname, ILconst[] args) throws NoSuchNativeException;

    void setOutStream(PrintStream outStream);

    /**
     * Optional lifecycle hook to release any resources (e.g. SQLite database connections, JDBC statements)
     * held by this provider when interpreter execution completes.
     */
    @Override
    default void close() {
        // Default implementation does nothing
    }

}
