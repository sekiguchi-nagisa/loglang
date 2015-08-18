package loglang;

/**
 * Created by skgchxngsxyz-osx on 15/08/18.
 */
public class SemanticException extends RuntimeException {

    public SemanticException(String message) {
        super(message);
    }

    public static void semanticError(String message) {
        throw new SemanticException(message);
    }
}
