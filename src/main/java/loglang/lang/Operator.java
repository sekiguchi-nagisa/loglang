package loglang.lang;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Objects;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Operator {
    Kind value() default Kind.DUMMY;

    enum Kind {
        DUMMY(""),

        // unary op
        NOT("!", 1),
        PLUS("+", 1),
        MINUS("-", 1),

        // binary op
        ADD("+"),
        SUB("-"),
        MUL("*"),
        DIV("/"),
        MOD("%"),
        EQ("=="),
        NE("!="),
        LT("<"),
        GT(">"),
        LE("<="),
        GE(">="),
        AND("&"),
        OR("|"),
        XOR("^"),
        ;

        private final String opName;

        /**
         * default is 2.
         */
        private final int paramSize;

        /**
         * equivalent to Kind(opName, 2)
         * @param opName
         */
        Kind(String opName) {
            this(opName, 2);
        }

        Kind(String opName, int paramSize) {
            this.opName = opName;
            this.paramSize = paramSize;
        }

        public String getOpName() {
            return this.opName;
        }

        public int getParamSize() {
            return paramSize;
        }

        public static String toActualName(String opName, int paramSize) {
            return Objects.requireNonNull(opName) + paramSize;
        }

        public String toActualName() {
            return toActualName(this.opName, this.paramSize);
        }
    }
}
