package loglang;

import loglang.symbol.MemberRef;
import nez.peg.tpeg.LongRange;
import nez.peg.tpeg.type.LType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public static class VarDeclNode extends Node {
        private final String name;
        private Node initValueNode;
        private MemberRef.FieldRef entry;

        public VarDeclNode(LongRange range, String name, Node initValueNode) {
            super(range);
            this.name = name;
            this.initValueNode = initValueNode;
        }

        public String getName() {
            return name;
        }

        public void setInitValueNode(Node initValueNode) {
            this.initValueNode = initValueNode;
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
            this.varName = varName;
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
