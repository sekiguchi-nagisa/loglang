package loglang;

import loglang.symbol.ClassScope;
import loglang.symbol.MemberRef;
import loglang.symbol.SymbolTable;
import nez.peg.tpeg.SemanticException;
import nez.peg.tpeg.type.LType;
import nez.peg.tpeg.type.TypeEnv;
import nez.peg.tpeg.type.TypeException;

import java.util.Objects;

import static loglang.Node.*;

/**
 * Created by skgchxngsxyz-osx on 15/08/18.
 */
public class TypeChecker implements NodeVisitor<Node, Void> {
    private final TypeEnv env;
    private final SymbolTable symbolTable;

    /**
     * for currently processing class.
     */
    private ClassScope classScope = null;

    public TypeChecker(TypeEnv env) {
        this.env = Objects.requireNonNull(env);
        this.symbolTable = new SymbolTable(this.env);
    }

    public Node checkType(LType requiredType, Node targetNode, LType unacceptableType) {  //FIXME: coercion
        if(targetNode.getType() == null) {
            this.visit(targetNode);
        }

        LType type = targetNode.getType();
        if(type == null) {
            throw new SemanticException(targetNode.getRange(), "broken node");
        }

        if(requiredType == null) {
            if(unacceptableType != null && unacceptableType.isSameOrBaseOf(type)) {
                throw new SemanticException(targetNode.getRange(), "unacceptable type: " + type);
            }
            return targetNode;
        }

        if(requiredType.isSameOrBaseOf(type)) {
            return targetNode;
        }

        throw new SemanticException(targetNode.getRange(), "require: " + requiredType + ", but is: " + type);
    }

    /**
     * not allow void type
     * @param targetNode
     * @return
     */
    public Node checkType(Node targetNode) {
        return this.checkType(null, targetNode, LType.voidType);
    }

    /**
     * not allow void type
     * @param requiredType
     * @param targetNode
     * @return
     */
    public Node checkType(LType requiredType, Node targetNode) {
        return this.checkType(requiredType, targetNode, null);
    }

    /**
     * allow void type.
     * if resolved type is not void, wrap popNode.
     * if targetNode if BlockNode, check type with new scope
     * @param targetNode
     * @return
     */
    public Node checkTypeAsStatement(Node targetNode) {
        if(targetNode instanceof BlockNode) {
            this.classScope.enterScope();
            this.checkTypeWithCurrentScope((BlockNode) targetNode);
            this.classScope.exitScope();
            return targetNode;
        }

        Node node = this.checkType(null, targetNode, null);
        return node.hasReturnValue() ? new PopNode(node) : node;
    }

    public void checkTypeWithCurrentScope(BlockNode blockNode) {
        this.checkType(this.env.getVoidType(), blockNode);
    }

    @Override
    public Node visitIntLiteralNode(IntLiteralNode node, Void param) {
        node.setType(this.env.getIntType());
        return node;
    }

    @Override
    public Node visitFloatLiteralNode(FloatLiteralNode node, Void param) {
        node.setType(this.env.getFloatType());
        return node;
    }

    @Override
    public Node visitBoolLiteralNode(BoolLiteralNode node, Void param) {
        node.setType(this.env.getBoolType());
        return node;
    }

    @Override
    public Node visitStringLiteralNode(StringLiteralNode node, Void param) {
        node.setType(this.env.getStringType());
        return node;
    }

    @Override
    public Node visitTernaryNode(TernaryNode node, Void param) {
        this.checkType(this.env.getBoolType(), node.getCondNode());
        node.replaceLeftNode(this::checkType);

        LType leftType = node.getLeftNode().getType();
        node.replaceRightNode(t -> this.checkType(leftType, t));

        node.setType(leftType);
        return node;
    }

    @Override
    public Node visitCondOpNode(CondOpNode node, Void param) {
        this.checkType(this.env.getBoolType(), node.getLeftNode());
        this.checkType(this.env.getBoolType(), node.getRightNode());
        node.setType(this.env.getBoolType());
        return node;
    }

    @Override
    public Node visitCaseNode(CaseNode node, Void param) {  //FIXME: case parameter
        // create new ClassScope
        try {
            this.classScope = this.symbolTable.newCaseScope(this.env, node.getLabelName());
        } catch(TypeException e) {
            throw new SemanticException(node.getRange(), e);
        }

        /**
         * representing CaseContext#invoke()
         * first entry is this
         * second entry and third entry are method parameters.
         */
        this.classScope.enterMethod(3);

        // register state entry
        node.getStateDeclNodes().forEach(this::checkTypeAsStatement);

        // register prefix tree field entry
//        String name = TypeEnv.getAnonymousPrefixTypeName();
//        LType stype = this.env.get

        this.checkTypeWithCurrentScope(node.getBlockNode());

        node.setLocalSize(this.classScope.getMaximumLocalSize());
        node.setThisType(this.classScope.getOwnerType());
        this.classScope.exitMethod();

        node.setType(LType.voidType);
        return node;
    }

    @Override
    public Node visitBlockNode(BlockNode node, Void param) {
        node.getNodes().replaceAll(this::checkTypeAsStatement);
        node.setType(LType.voidType);
        return node;
    }

    @Override
    public Node visitStateDeclNode(StateDeclNode node, Void param) {
        node.replaceInitValueNode(this::checkType);

        LType type = node.getInitValueNode().getType();
        MemberRef.FieldRef entry = this.classScope.newStateEntry(node.getName(), type, false);
        if(entry == null) {
            throw new SemanticException(node.getRange(), "already defined state variable: " + node.getName());
        }

        node.setType(LType.voidType);
        return node;
    }

    @Override
    public Node visitVarDeclNode(VarDeclNode node, Void param) {
        node.replaceInitValueNode(this::checkType);

        LType type = node.getInitValueNode().getType();
        MemberRef.FieldRef entry = this.classScope.newLocalEntry(node.getName(), type, false);
        if(entry == null) {
            throw new SemanticException(node.getRange(), "already defined local variable: " + node.getName());
        }

        node.setEntry(entry);
        node.setType(LType.voidType);
        return node;
    }

    @Override
    public Node visitVarNode(VarNode node, Void param) {
        MemberRef.FieldRef entry = this.classScope.findEntry(node.getVarName());
        if(entry == null) {
            throw new SemanticException(node.getRange(), "undefined variable: " + node.getVarName());
        }

        node.setEntry(entry);
        node.setType(entry.getFieldType());
        return node;
    }

    @Override
    public Node visitPrintNode(PrintNode node, Void param) {
        this.checkType(node.getExprNode());
        node.setType(this.env.getVoidType());
        return node;
    }

    @Override
    public Node visitAssertNode(AssertNode node, Void param) {
        this.checkType(this.env.getBoolType(), node.getCondNode());
        node.getMsgNode().ifPresent(t -> this.checkType(this.env.getStringType(), t));
        node.setType(this.env.getVoidType());
        return node;
    }

    @Override
    public Node visitWhileNode(WhileNode node, Void param) {
        this.checkType(this.env.getBoolType(), node.getCondNode());
        this.checkTypeAsStatement(node.getBlockNode());
        node.setType(this.env.getVoidType());
        return node;
    }

    @Override
    public Node visitIfNode(IfNode node, Void param) {
        this.checkType(this.env.getBoolType(), node.getCondNode());
        node.replaceThenNode(this::checkTypeAsStatement);
        node.repalceElseNodeIfExist(this::checkTypeAsStatement);
        node.setType(this.env.getVoidType());
        return node;
    }

    @Override
    public Node visitPopNode(PopNode node, Void param) {
        throw new UnsupportedOperationException();    // not call it.
    }

    @Override
    public Node visitRootNode(RootNode node, Void param) {
        node.getCaseNodes().forEach(this::checkTypeAsStatement);
        node.setType(LType.voidType);
        return node;
    }
}
