package srv;

import java.io.IOException;
import java.util.logging.*;

public class LoggerView implements Loggable {

    private static final Logger logger;

    static {
        logger = Logger.getLogger(LoggerView.class.getName());
        try {
            logger.setUseParentHandlers(false);
            logger.setLevel(Level.ALL);
            Handler fh = new FileHandler("server.log", 1000, 2, true);
            Formatter fm = new Formatter() {
                @Override
                public String format(LogRecord record) {
                    return record.getMessage();
                }
            };
            fh.setFormatter(fm);
            logger.addHandler(fh);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    synchronized public void printMessage(String name, String msg) {
        System.out.printf( "[%s]: %s\n", name, msg );
        logger.info("Message received from " + name + "\n");
    }

    synchronized public void printError(String name, String msg) {
        System.out.printf( "[%s]: %s\n", name, msg );
        logger.info("[" + name + "] : " + msg + "\n");
    }

    synchronized public void printInfo(String name, String msg) {
        System.out.printf( "[%s]: %s\n", name, msg );
        logger.info("[" + name + "] : " + msg + "\n");
    }

    synchronized public void printInfo( String msg ) {
        System.out.println( msg );
        logger.info( msg + "\n");
    }
}
