package loglang.misc;

/**
 * Created by skgchxngsxyz-osx on 15/10/05.
 */
public class FatalError extends Error {
    private static final long serialVersionUID = 8017043677701089665L;

    public FatalError(String message) {
        super(message);
    }
}
