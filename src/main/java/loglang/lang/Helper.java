package loglang.lang;

/**
 * Created by skgchxngsxyz-osx on 15/10/02.
 */
public class Helper {
    public static void print(Object value) {
        System.out.println(value);
    }

    /**
     *
     * @param result
     * @param message
     * may be null
     */
    public static void checkAssertion(boolean result, String message) {
        if(!result) {
            new AssertionError(message != null ? message : "").printStackTrace();
            System.exit(1);
        }
    }
}
