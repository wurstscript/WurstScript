package de.peeeq.wurstscript;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

public class WLoggerDefault implements WLoggerI {

  private org.slf4j.Logger logger;

  public WLoggerDefault(String loggerName) {
    logger = LoggerFactory.getLogger(loggerName);
  }

  /**
   * (non-Javadoc)
   *
   * @see de.peeeq.wurstscript.WLoggerI#trace(String)
   */
  @Override
  public void trace(String msg) {
    logger.trace(msg);
  }

  /**
   * (non-Javadoc)
   *
   * @see de.peeeq.wurstscript.WLoggerI#info(java.lang.String)
   */
  @Override
  public void info(String msg) {
    logger.info(msg);
  }

  /**
   * (non-Javadoc)
   *
   * @see de.peeeq.wurstscript.WLoggerI#warning(java.lang.String)
   */
  @Override
  public void warning(String msg) {
    logger.warn(msg);
  }

  /**
   * (non-Javadoc)
   *
   * @see de.peeeq.wurstscript.WLoggerI#warning(java.lang.String, Throwable)
   */
  @Override
  public void warning(String msg, Throwable e) {
    logger.warn(msg, e);
  }

  /**
   * (non-Javadoc)
   *
   * @see de.peeeq.wurstscript.WLoggerI#severe(java.lang.String)
   */
  @Override
  public void severe(String msg) {
    logger.error(msg);
  }

  /**
   * (non-Javadoc)
   *
   * @see de.peeeq.wurstscript.WLoggerI#severe(java.lang.Throwable)
   */
  @Override
  public void severe(Throwable t) {
    t.printStackTrace();
    logger.error("Error", t);
  }

  /**
   * (non-Javadoc)
   *
   * @see de.peeeq.wurstscript.WLoggerI#info(java.lang.Throwable)
   */
  @Override
  public void info(Throwable e) {
    logger.info("Error", e);
  }

  /** (non-Javadoc) */
  @Override
  public void setLevel(Level level) {
    if (logger instanceof Logger) {
      ((Logger) logger).setLevel(level);
    }
  }
}
