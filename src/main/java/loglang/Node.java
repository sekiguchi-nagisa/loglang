package loglang;

import loglang.symbol.MemberRef;
import nez.peg.tpeg.LongRange;
import nez.peg.tpeg.type.LType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * Created by skgchxngsxyz-osx on 15/08/18.
 */
public abstract class Node {
    private final LongRange range;
    private LType type = null;


    public Node(LongRange range) {
        this.range = range;
    }

    public LongRange getRange() {
        return range;
    }

    /**
     *
     * @return
     * if not performed type checking, return null
     */
    public LType getType() {
        return this.type;
    }

    public void setType(LType type) {
        this.type = Objects.requireNonNull(type);
    }

    /**
     * must call after type checking.
     * @return
     */
    public boolean hasReturnValue() {
        return !this.type.isVoid();
    }

    public abstract <T, P> T accept(NodeVisitor<T, P> visitor, P param);

    public static class IntLiteralNode extends Node {
        private final int value;

        public IntLiteralNode(LongRange range, int value) {
            super(range);
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

        public FloatLiteralNode(LongRange range, float value) {
            super(range);
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

        public BoolLiteralNode(LongRange range, boolean value) {
            super(range);
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

        public StringLiteralNode(LongRange range, String value) {
            super(range);
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

    public static class TernaryNode extends Node {
        private Node condNode;

        private Node leftNode;
        private Node rightNode;

        public TernaryNode(LongRange range, Node condNode, Node leftNode, Node rightNode) {
            super(range);
            this.condNode = Objects.requireNonNull(condNode);
            this.leftNode = Objects.requireNonNull(leftNode);
            this.rightNode = Objects.requireNonNull(rightNode);
        }

        public Node getCondNode() {
            return condNode;
        }

        public Node getLeftNode() {
            return leftNode;
        }

        public void replaceLeftNode(UnaryOperator<Node> op) {
            this.leftNode = Objects.requireNonNull(op.apply(this.leftNode));
        }

        public Node getRightNode() {
            return rightNode;
        }

        public void replaceRightNode(UnaryOperator<Node> op) {
            this.rightNode = Objects.requireNonNull(op.apply(this.rightNode));
        }

        @Override
        public <T, P> T accept(NodeVisitor<T, P> visitor, P param) {
            return visitor.visitTernaryNode(this, param);
        }
    }

    public static class CondOpNode extends Node {
        private final boolean isAnd;

        private Node leftNode;
        private Node rightNode;

        CondOpNode(LongRange range, Node leftNode, Node rightNode, boolean isAnd) {
            super(range);
            this.leftNode = Objects.requireNonNull(leftNode);
            this.rightNode = Objects.requireNonNull(rightNode);
            this.isAnd = isAnd;
        }

        public static CondOpNode newAndNode(LongRange range, Node leftNode, Node rightNode) {
            return new CondOpNode(range, leftNode, rightNode, true);
        }

        public static CondOpNode newOrNode(LongRange range, Node leftNode, Node rightNode) {
            return new CondOpNode(range, leftNode, rightNode, false);
        }

        public Node getLeftNode() {
            return leftNode;
        }

        public Node getRightNode() {
            return rightNode;
        }

        public boolean isAnd() {
            return isAnd;
        }

        @Override
        public <T, P> T accept(NodeVisitor<T, P> visitor, P param) {
            return visitor.visitCondOpNode(this, param);
        }
    }

    public static class CaseNode extends Node {
        int caseIndex = -1;

        private final List<StateDeclNode> stateDeclNodes = new ArrayList<>();
        private final BlockNode blockNode;

        /**
         * may be null if has no label
         */
        private final String labelName;

        /**
         * maximum number of local variable(for byte code generation)
         */
        private int localSize = 0;

        /**
         * represent case context
         */
        private LType.AbstractStructureType thisType = null;

        public CaseNode(LongRange range, String labelName) {
            super(range);
            this.blockNode = new BlockNode(range);
            this.labelName = labelName;
        }

        @Override
        public <T, P> T accept(NodeVisitor<T, P> visitor, P param) {
            return visitor.visitCaseNode(this, param);
        }

        public int getCaseIndex() {
            return this.caseIndex;
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

        /**
         *
         * @return
         * may be null
         */
        public String getLabelName() {
            return labelName;
        }

        public void setLocalSize(int size) {
            this.localSize = size;
        }

        public int getLocalSize() {
            return this.localSize;
        }

        public void setThisType(LType.AbstractStructureType thisType) {
            this.thisType = thisType;
        }

        public LType.AbstractStructureType getThisType() {
            return thisType;
        }
    }

    public static class BlockNode extends Node {
        private final List<Node> nodes = new ArrayList<>();

        public BlockNode(LongRange range) {
            super(range);
        }

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

        public StateDeclNode(LongRange range, String name, Node initValueNode) {
            super(range);
            this.name = Objects.requireNonNull(name);
            this.initValueNode = Objects.requireNonNull(initValueNode);
        }

        @Override
        public <T, P> T accept(NodeVisitor<T, P> visitor, P param) {
            return visitor.visitStateDeclNode(this, param);
        }

        public void replaceInitValueNode(UnaryOperator<Node> op) {
            this.initValueNode = Objects.requireNonNull(op.apply(this.initValueNode));
        }


        public Node getInitValueNode() {
            return initValueNode;
        }

        public String getName() {
            return name;
        }
    }

    public static class VarDeclNode extends Node {
        private final String name;
        private Node initValueNode;
        private MemberRef.FieldRef entry;

        public VarDeclNode(LongRange range, String name, Node initValueNode) {
            super(range);
            this.name = Objects.requireNonNull(name);
            this.initValueNode = Objects.requireNonNull(initValueNode);
        }

        public String getName() {
            return name;
        }

        public void replaceInitValueNode(UnaryOperator<Node> op) {
            this.initValueNode = Objects.requireNonNull(op.apply(this.initValueNode));
        }

        public Node getInitValueNode() {
            return initValueNode;
        }

        public void setEntry(MemberRef.FieldRef entry) {
            this.entry = entry;
        }

        public MemberRef.FieldRef getEntry() {
            return entry;
        }

        @Override
        public <T, P> T accept(NodeVisitor<T, P> visitor, P param) {
            return visitor.visitVarDeclNode(this, param);
        }
    }

    public static class VarNode extends Node {
        private final String varName;
        private MemberRef.FieldRef entry;

        public VarNode(LongRange range, String varName) {
            super(range);
            this.varName = Objects.requireNonNull(varName);
            this.entry = null;
        }

        public String getVarName() {
            return this.varName;
        }

        public void setEntry(MemberRef.FieldRef entry) {
            this.entry = Objects.requireNonNull(entry);
        }

        /**
         *
         * @return
         * return null before call setEntry
         */
        public MemberRef.FieldRef getEntry() {
            return this.entry;
        }

        @Override
        public <T, P> T accept(NodeVisitor<T, P> visitor, P param) {
            return visitor.visitVarNode(this, param);
        }
    }

    /**
     * for printing
     */
    public static class PrintNode extends Node {
        private Node exprNode;

        public PrintNode(LongRange range, Node exprNode) {
            super(range);
            this.exprNode = Objects.requireNonNull(exprNode);
        }

        public Node getExprNode() {
            return exprNode;
        }

        @Override
        public <T, P> T accept(NodeVisitor<T, P> visitor, P param) {
            return visitor.visitPrintNode(this, param);
        }
    }

    public static class AssertNode extends Node {
        private Node condNode;
        private Optional<Node> msgNode;

        public AssertNode(LongRange range, Node condNode, Node msgNode) {
            super(range);
            this.condNode = Objects.requireNonNull(condNode);
            this.msgNode = Optional.ofNullable(msgNode);
        }

        public Node getCondNode() {
            return condNode;
        }

        public Optional<Node> getMsgNode() {
            return msgNode;
        }

        @Override
        public <T, P> T accept(NodeVisitor<T, P> visitor, P param) {
            return visitor.visitAssertNode(this, param);
        }
    }

    public static class WhileNode extends Node {
        private Node condNode;
        private BlockNode blockNode;

        public WhileNode(LongRange range, Node condNode, BlockNode blockNode) {
            super(range);
            this.condNode = Objects.requireNonNull(condNode);
            this.blockNode = Objects.requireNonNull(blockNode);
        }

        public Node getCondNode() {
            return condNode;
        }

        public BlockNode getBlockNode() {
            return blockNode;
        }

        @Override
        public <T, P> T accept(NodeVisitor<T, P> visitor, P param) {
            return visitor.visitWhileNode(this, param);
        }
    }

    public static class IfNode extends Node {
        private Node condNode;
        private Node thenNode;
        private Optional<Node> elseNode;

        /**
         *
         * @param range
         * @param condNode
         * @param thenNode
         * @param elseNode
         * may be null if has no else
         */
        public IfNode(LongRange range, Node condNode, Node thenNode, Node elseNode) {
            super(range);
            this.condNode = Objects.requireNonNull(condNode);
            this.thenNode = Objects.requireNonNull(thenNode);
            this.elseNode = Optional.ofNullable(elseNode);
        }

        public Node getCondNode() {
            return condNode;
        }

        public Node getThenNode() {
            return thenNode;
        }

        public void replaceThenNode(UnaryOperator<Node> op) {
            this.thenNode = Objects.requireNonNull(op.apply(this.getThenNode()));
        }

        public Optional<Node> getElseNode() {
            return elseNode;
        }

        public void repalceElseNodeIfExist(UnaryOperator<Node> op) {
            this.elseNode = this.elseNode.map(op::apply);
        }

        @Override
        public <T, P> T accept(NodeVisitor<T, P> visitor, P param) {
            return visitor.visitIfNode(this, param);
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
            super(exprNode.range);
            this.exprNode = Objects.requireNonNull(exprNode);
            Objects.requireNonNull(exprNode.getType());
            this.setType(LType.voidType);
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

        public RootNode(LongRange range) {
            super(range);
        }

        @Override
        public <T, P> T accept(NodeVisitor<T, P> visitor, P param) {
            return visitor.visitRootNode(this, param);
        }

        public List<CaseNode> getCaseNodes() {
            return caseNodes;
        }

        public void addCaseNode(CaseNode node) {
            node.caseIndex = this.caseNodes.size();
            this.caseNodes.add(node);
        }
    }
}
