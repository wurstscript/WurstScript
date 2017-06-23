package de.peeeq.wurstscript;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WLoggerDefault implements WLoggerI {

    private Logger logger;

    {
        logger = Logger.getLogger("wurstlog");
        logger.setLevel(Level.OFF); // adjust level for debugging
    }

    /* (non-Javadoc)
     * @see de.peeeq.wurstscript.WLoggerI#info(java.lang.String)
     */
    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    /* (non-Javadoc)
     * @see de.peeeq.wurstscript.WLoggerI#warning(java.lang.String)
     */
    @Override
    public void warning(String msg) {
        logger.log(Level.WARNING, msg);
    }

    /* (non-Javadoc)
     * @see de.peeeq.wurstscript.WLoggerI#severe(java.lang.String)
     */
    @Override
    public void severe(String msg) {
        logger.log(Level.SEVERE, msg);
    }

    /* (non-Javadoc)
     * @see de.peeeq.wurstscript.WLoggerI#severe(java.lang.Throwable)
     */
    @Override
    public void severe(Throwable t) {
        t.printStackTrace();
        logger.log(Level.SEVERE, "Error", t);
    }

    /* (non-Javadoc)
     * @see de.peeeq.wurstscript.WLoggerI#info(java.lang.Throwable)
     */
    @Override
    public void info(Throwable e) {
        logger.log(Level.INFO, "Error", e);

    }

    /* (non-Javadoc)
     * @see de.peeeq.wurstscript.WLoggerI#setHandler(java.util.logging.Handler)
     */
    @Override
    public void setHandler(Handler handler) {
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
    }

    /* (non-Javadoc)
     * @see de.peeeq.wurstscript.WLoggerI#setLevel(java.util.logging.Level)
     */
    @Override
    public void setLevel(Level level) {
        logger.setLevel(level);
    }

}
