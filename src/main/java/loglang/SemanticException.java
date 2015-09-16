package loglang;

import loglang.misc.LongRange;
import loglang.type.TypeException;

import java.util.Objects;

/**
 * Created by skgchxngsxyz-osx on 15/08/18.
 */
public class SemanticException extends RuntimeException {
    private final LongRange range;

    /**
     *
     * @param range
     * not null
     * @param message
     */
    public SemanticException(LongRange range, String message) {
        super(message);
        this.range = Objects.requireNonNull(range);
    }

    public SemanticException(LongRange range, TypeException e) {
        super(e.getMessage(), e);
        this.range = Objects.requireNonNull(range);
    }

    public LongRange getRange() {
        return range;
    }

    public static void semanticError(LongRange range, String message) {
        throw new SemanticException(range, message);
    }
}
