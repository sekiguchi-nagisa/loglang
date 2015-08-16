package loglang.misc;

/**
 * Created by skgchxngsxyz-osx on 15/08/13.
 */
public class Utils {
    private Utils() {}

    public static void propagate(Exception e) {
        if(e instanceof RuntimeException) {
            throw (RuntimeException) e;
        }
        throw new RuntimeException(e);
    }

    private static String[] enableKeywords = {
            "on", "true", "enable",
    };

    private static String[] disableKeywords = {
            "off", "false", "disable",
    };

    public static boolean checkProperty(final String propertyName, final boolean defaultValue) {
        String property = System.getProperty(propertyName);

        for(String keyword : enableKeywords) {
            if(keyword.equalsIgnoreCase(property)) {
                return true;
            }
        }

        for(String keyword : disableKeywords) {
            if(keyword.equalsIgnoreCase(property)) {
                return false;
            }
        }
        return defaultValue;
    }

    public static void fatal(String message) {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        System.err.println("fatal: " + message);
        for(int i = 2; i < elements.length; i++) {
            StackTraceElement element = elements[i];
            System.err.println("\tat " + element);
        }
        System.exit(1);
    }
}
