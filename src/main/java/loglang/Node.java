package loglang;

import java.lang.reflect.Type;

/**
 * Created by skgchxngsxyz-osx on 15/08/18.
 */
public abstract class Node {
    private int lineNum = -1;   //FIXME:
    private Type type = null;

    /**
     *
     * @return
     * if not performed type checking, return null
     */
    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public abstract <T, P> T accept(NodeVisitor<T, P> visitor, P param);

    public static class IntLiteralNode extends Node {
        private final int value;

        public IntLiteralNode(int value) {
            this.value = value;
        }

        @Override
        public <T, P> T accept(NodeVisitor<T, P> visitor, P param) {
            return visitor.visitIntLiteralNode(this, param);
        }

        public int getValue() {
            return this.value;
        }
    }

    public static class FloatLiteralNode extends Node {
        private final float value;

        public FloatLiteralNode(float value) {
            this.value = value;
        }

        @Override
        public <T, P> T accept(NodeVisitor<T, P> visitor, P param) {
            return visitor.visitFloatLiteralNode(this, param);
        }

        public float getValue() {
            return this.value;
        }
    }

    public static class BoolLiteralNode extends Node {
        private final boolean value;

        public BoolLiteralNode(boolean value) {
            this.value = value;
        }

        @Override
        public <T, P> T accept(NodeVisitor<T, P> visitor, P param) {
            return visitor.visitBoolLiteralNode(this, param);
        }

        public boolean getValue() {
            return this.value;
        }
    }

    public static class StringLiteralNode extends Node {
        private final String value;

        public StringLiteralNode(String value) {
            this.value = value;
        }

        @Override
        public <T, P> T accept(NodeVisitor<T, P> visitor, P param) {
            return visitor.visitStringLiteralNode(this, param);
        }

        public String getValue() {
            return this.value;
        }
    }
}
