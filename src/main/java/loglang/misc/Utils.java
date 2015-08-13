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
}
