package loglang.misc;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Random;

/**
 * Created by skgchxngsxyz-osx on 15/08/13.
 */
public class Utils {
    private Utils() {}

    public static RuntimeException propagate(Exception e) {
        if(e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new RuntimeException(e);
    }

    private final static String[] enableKeywords = {
            "on", "true", "enable",
    };

    private final static String[] disableKeywords = {
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

    private final static Random rnd = new Random(System.currentTimeMillis());

    public static int getRandomNum() {
        return rnd.nextInt();
    }

    public static int setFlag(int bitset, int flag) {
        return bitset | flag;
    }

    public static int unsetFlag(int bitset, int flag) {
        return bitset & (~flag);
    }

    public static boolean hasFlag(int bitset, int flag) {
        return (bitset & flag) == flag;
    }

    public static <T> void push(ArrayList<T> array, T value) {
        array.add(value);
    }

    public static <T> T pop(ArrayList<T> array) {
        int lastIndex = array.size() - 1;
        if(lastIndex < 0) {
            throw new EmptyStackException();
        }
        return array.remove(lastIndex);
    }

    public static <T> T peek(ArrayList<T> array) {
        int lastIndex = array.size() - 1;
        if(lastIndex < 0) {
            throw new EmptyStackException();
        }
        return array.get(lastIndex);
    }
}
