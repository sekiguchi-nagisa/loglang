package loglang;

import java.lang.reflect.Type;

/**
 * Created by skgchxngsxyz-osx on 15/08/18.
 */
public class Types {
    private Types() { }

    public static boolean isSameOrBaseOf(Type left, Type right) {   //FIXME:
        return left.equals(right);
    }

    public static Class<?> actualClass(Type type) {  //FIXME:
        return (Class<?>) type;
    }
}
