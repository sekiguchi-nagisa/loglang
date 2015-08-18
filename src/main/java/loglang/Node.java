package loglang;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        this.type = Objects.requireNonNull(type);
    }

    /**
     * must call after type checking.
     * @return
     */
    public boolean hasReturnValue() {
        return !this.type.equals(void.class);
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
            this.value = Objects.requireNonNull(value);
        }

        @Override
        public <T, P> T accept(NodeVisitor<T, P> visitor, P param) {
            return visitor.visitStringLiteralNode(this, param);
        }

        public String getValue() {
            return this.value;
        }
    }

    public static class CaseNode extends Node {
        private final List<StateDeclNode> stateDeclNodes = new ArrayList<>();
        private final BlockNode blockNode = new BlockNode();

        @Override
        public <T, P> T accept(NodeVisitor<T, P> visitor, P param) {
            return visitor.visitCaseNode(this, param);
        }

        public BlockNode getBlockNode() {
            return this.blockNode;
        }

        public List<StateDeclNode> getStateDeclNodes() {
            return stateDeclNodes;
        }

        public void addStmtNode(Node node) {
            this.blockNode.addNode(node);
        }

        public void addStateDeclNode(StateDeclNode node) {
            this.stateDeclNodes.add(node);
        }
    }

    public static class BlockNode extends Node {
        private final List<Node> nodes = new ArrayList<>();

        @Override
        public <T, P> T accept(NodeVisitor<T, P> visitor, P param) {
            return visitor.visitBlockNode(this, param);
        }

        public List<Node> getNodes() {
            return nodes;
        }

        public void addNode(Node node) {
            this.nodes.add(Objects.requireNonNull(node));
        }
    }

    public static class StateDeclNode extends Node {
        private final String name;
        private Node initValueNode;

        public StateDeclNode(String name, Node initValueNode) {
            this.name = Objects.requireNonNull(name);
            this.initValueNode = Objects.requireNonNull(initValueNode);
        }

        @Override
        public <T, P> T accept(NodeVisitor<T, P> visitor, P param) {
            return visitor.visitStateDeclNode(this, param);
        }

        public void setInitValueNode(Node initValueNode) {
            this.initValueNode = Objects.requireNonNull(initValueNode);
        }

        public Node getInitValueNode() {
            return initValueNode;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * pseudo node for stack operation.
     * pop stack top.
     */
    public static class PopNode extends Node {
        private final Node exprNode;

        /**
         *
         * @param exprNode
         * must be type checked.
         */
        public PopNode(Node exprNode) {
            this.exprNode = Objects.requireNonNull(exprNode);
            Objects.requireNonNull(exprNode.getType());
            this.setType(void.class);
        }

        @Override
        public <T, P> T accept(NodeVisitor<T, P> visitor, P param) {
            return visitor.visitPopNode(this, param);
        }

        public Node getExprNode() {
            return exprNode;
        }
    }

    public static class RootNode extends Node {
        private final List<CaseNode> caseNodes = new ArrayList<>();


        @Override
        public <T, P> T accept(NodeVisitor<T, P> visitor, P param) {
            return visitor.visitRootNode(this, param);
        }

        public List<CaseNode> getCaseNodes() {
            return caseNodes;
        }

        public void addCaseNode(CaseNode node) {
            this.caseNodes.add(node);
        }
    }
}
