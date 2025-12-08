package de.peeeq.wurstscript;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;


public abstract class WLogger {

    private static WLoggerI instance = new WLoggerDefault("default");

    public static void info(Throwable e) {
        instance.info(e);
    }

    public static void trace(String msg) {
        instance.trace(msg);
    }

    public static void info(String msg) {
        instance.info(msg);
    }

    public static void debug(String s) {
        instance.debug(s);
    }

    public static void setLevel(Level level) {
        instance.setLevel(level);
    }

    public static void severe(Throwable e) {
        instance.severe(e);
    }

    public static void severe(String msg) {
        instance.severe(msg);
    }

    public static void warning(String msg) {
        instance.warning(msg);
    }

    public static void warning(String msg, Throwable e) {
        instance.warning(msg, e);
    }

    public static String getLog() {
        File clientLogFile;
        FileAppender<?> fileAppender = null;
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        for (Logger logger : context.getLoggerList()) {
            for (Iterator<Appender<ILoggingEvent>> index = logger.iteratorForAppenders();
                 index.hasNext(); ) {
                Object enumElement = index.next();
                if (enumElement instanceof FileAppender) {
                    fileAppender = (FileAppender<?>) enumElement;
                }
            }
        }

        if (fileAppender != null) {
            clientLogFile = new File(fileAppender.getFile());
        } else {
            clientLogFile = null;
        }
        if (clientLogFile != null) {
            try {
                return getLast100Lines(clientLogFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "no logs...";
    }

    private static String getLast100Lines(File file) throws IOException {
        FileChannel channel;
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            channel = fileInputStream.getChannel();
            ByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            buffer.position((int) channel.size());
            int count = 0;
            StringBuilder builder = new StringBuilder();
            for (long i = channel.size() - 1; i >= 0; i--) {
                char c = (char) buffer.get((int) i);
                builder.append(c);
                if (c == '\n') {
                    if (count == 100) break;
                    count++;
                }
            }
            return builder.toString();
        }

    }


    public static void setLogger(String loggerName) {
        WLogger.instance = new WLoggerDefault(loggerName);
    }


}
