package loglang;

/**
 * Created by skgchxngsxyz-osx on 15/09/03.
 */
public class TypeException extends Exception {
    public TypeException(String message) {
        super(message);
    }

    public static void typeError(String message) throws TypeException {
        throw new TypeException(message);
    }
}
