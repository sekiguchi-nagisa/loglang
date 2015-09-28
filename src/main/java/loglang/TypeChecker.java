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
import static nez.peg.tpeg.SemanticException.*;

/**
 * Created by skgchxngsxyz-osx on 15/08/18.
 */
public class TypeChecker implements NodeVisitor<Node, Void> {
    private final TypeEnv env;
    private final SymbolTable symbolTable = new SymbolTable();

    /**
     * for currently processing class.
     */
    private ClassScope classScope = null;

    public TypeChecker(TypeEnv env) {
        this.env = Objects.requireNonNull(env);
    }

    public Node checkType(LType requiredType, Node targetNode, LType unacceptableType) {  //FIXME: coercion
        if(targetNode.getType() == null) {
            this.visit(targetNode);
        }

        LType type = targetNode.getType();
        if(type == null) {
            semanticError(targetNode.getRange(), "broken node");
        }

        if(requiredType == null) {
            if(unacceptableType != null && unacceptableType.isSameOrBaseOf(type)) {
                semanticError(targetNode.getRange(), "unacceptable type: " + type);
            }
            return targetNode;
        }

        if(requiredType.isSameOrBaseOf(type)) {
            return targetNode;
        }

        semanticError(targetNode.getRange(), "require: " + requiredType + ", but is: " + type);
        return null;
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
     * if resoleved type is not void, wrap popNode
     * @param targetNode
     * @return
     */
    public Node checkTypeAsStatement(Node targetNode) {
        Node node = this.checkType(null, targetNode, null);
        return node.hasReturnValue() ? new PopNode(node) : node;
    }

    public void checkTypeWithCurrentScope(BlockNode blockNode) {
        this.checkTypeAsStatement(blockNode);
    }

    public void checkTypeWithNewScope(BlockNode blockNode) {
        this.classScope.entryScope();
        this.checkTypeWithCurrentScope(blockNode);
        this.classScope.exitScope();
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
    public Node visitCaseNode(CaseNode node, Void param) {  //FIXME: case parameter
        // create new ClassScope
        try {
            this.classScope = this.symbolTable.newCaseScope(this.env, node.getLabelName());
        } catch(TypeException e) {
            throw new SemanticException(node.getRange(), e);
        }
        this.classScope.enterMethod();

        // register state entry
        for(StateDeclNode child : node.getStateDeclNodes()) {
            this.checkTypeAsStatement(child);
        }

        // register prefix tree field entry
//        String name = TypeEnv.getAnonymousPrefixTypeName();
//        LType stype = this.env.get

        this.checkTypeWithCurrentScope(node.getBlockNode());

        node.setLocalSize(this.classScope.getMaximumLocalSize());
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
        node.setInitValueNode(this.checkType(node.getInitValueNode()));

        LType type = node.getInitValueNode().getType();
        MemberRef.FieldRef entry = this.classScope.newStateEntry(node.getName(), type, false);
        if(entry == null) {
            semanticError(node.getRange(), "already defined state variable: " + node.getName());
        }

        node.setType(LType.voidType);
        return node;
    }

    @Override
    public Node visitVarDeclNode(VarDeclNode node, Void param) {
        node.setInitValueNode(this.checkType(node.getInitValueNode()));

        LType type = node.getInitValueNode().getType();
        MemberRef.FieldRef entry = this.classScope.newLocalEntry(node.getName(), type, false);
        if(entry == null) {
            semanticError(node.getRange(), "already defined local variable: " + node.getName());
        }

        node.setEntry(entry);
        node.setType(LType.voidType);
        return node;
    }

    @Override
    public Node visitVarNode(VarNode node, Void param) {
        MemberRef.FieldRef entry = this.classScope.findEntry(node.getVarName());
        if(entry == null) {
            semanticError(node.getRange(), "undefined variable: " + node.getVarName());
        }

        node.setEntry(entry);
        node.setType(entry.getFieldType());
        return node;
    }

    @Override
    public Node visitPopNode(PopNode node, Void param) {
        throw new UnsupportedOperationException();    // not call it.
    }

    @Override
    public Node visitRootNode(RootNode node, Void param) {
        for(CaseNode caseNode : node.getCaseNodes()) {
            this.checkTypeAsStatement(caseNode);
        }
        node.setType(LType.voidType);
        return node;
    }
}
