package loglang.misc;

/**
 * Created by skgchxngsxyz-osx on 15/08/13.
 */
public class Utils {
    private Utils() {}

    public static void propagate(Throwable t) {
        if(t instanceof RuntimeException) {
            throw (RuntimeException) t;
        }
        throw new RuntimeException(t);
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
}
